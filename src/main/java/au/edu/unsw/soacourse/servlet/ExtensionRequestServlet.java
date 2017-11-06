package au.edu.unsw.soacourse.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import au.edu.unsw.soacourse.renewal.Renewal;
import au.edu.unsw.soacourse.renewal.Status;

/**
 * Servlet implementation class ExtensionRequestServlet
 */
public class ExtensionRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER_KEY = "DRIVER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ExtensionRequestServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getSession().getAttribute("renewalID") == null) {
			return;
		}
		String renewalID = (String) request.getSession().getAttribute(
				"renewalID");
		Renewal renewal = getRenewal(renewalID);
		String status = renewal.getStatus();

		if (status.equals(Status.COMPLETED.toString())) {
			return;
		} else if (status.equals(Status.EVALUATING_EXTENSION.toString())) {
			request.getRequestDispatcher("WEB-INF/jsp/waitingreview.jsp")
					.forward(request, response);
			return;
		} else if (status.equals(Status.VALIDATION_ERROR.toString())) {
			request.getRequestDispatcher("WEB-INF/jsp/waitingreview.jsp")
					.forward(request, response);
			return;
		//} else if (status.equals(Status.EXTENSION_REQUESTED.toString())) {
		//	request.getRequestDispatcher("WEB-INF/jsp/waitingreview.jsp")
		//			.forward(request, response);
		//	return;
		} else if (status.equals(Status.EXTENSION_APPROVED.toString())){
			response.sendRedirect(request.getContextPath() + "/payment");
			return;
		}

		// check action
		// if requestExtension
		// if pay
		String action = (String) request.getParameter("action");
		if (action != null) {
			if (action.equals("requestExtension")) {
				// update renewal notice
				Renewal r = getRenewal(renewalID);
				r.setStatus(Status.EXTENSION_REQUESTED.toString());
				updateRenewalNotice(r);
				request.getRequestDispatcher(
						"WEB-INF/jsp/successfulrequest.jsp").forward(request,
						response);
				return;
			} else if (action.equals("pay")) {
				response.sendRedirect(request.getContextPath() + "/payment");
				return;
			}
		}

		request.getRequestDispatcher("WEB-INF/jsp/requestextension.jsp")
				.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private Renewal getRenewal(String renewalID) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/"
						+ renewalID);
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", DRIVER_KEY).get();
		Renewal renewalNotice = null;
		try {
			renewalNotice = restResponse.readEntity(Renewal.class);
		} catch (Exception e) {
		}
		restResponse.close();
		return renewalNotice;
	}

	private void updateRenewalNotice(Renewal renewal) {
		ResteasyClient client = new ResteasyClientBuilder().build();

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/"
						+ String.valueOf(renewal.getId()));
		Form form = new Form();
		form.param("address", renewal.getAddress())
				.param("email", renewal.getEmail())
				.param("status", renewal.getStatus())
				.param("ownedBy", renewal.getOwnedBy());
		Entity<Form> entity = Entity.form(form);

		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", DRIVER_KEY).put(entity);

		restResponse.close();
	}

}
