package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import au.edu.unsw.soacourse.renewal.Renewal;
import au.edu.unsw.soacourse.renewal.Status;

/**
 * Servlet implementation class ValidationErrorLicenceServlet
 */
public class ReviewNoticeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String OFFICER_KEY = "OFFICER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReviewNoticeServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// check if logged in
		if (request.getSession().getAttribute("username") == null) {
			System.out.println("SHOULDN'T BE HERE");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		String username = (String) request.getSession()
				.getAttribute("username");
		String action = (String) request.getParameter("action");
		if (action != null) {
			String renewalID = (String) request.getParameter("renewalID");
			Renewal renewal = getRenewal(renewalID);
			if (action.equals("own")) {
				// update renewal own to username
				renewal.setOwnedBy(username);
				// if renewal was extension requested
				if (renewal.getStatus().equals(Status.EXTENSION_REQUESTED.toString())){
					System.out.println("HERE");
					renewal.setStatus(Status.EVALUATING_EXTENSION.toString());
				}
				// change to evaluating extension
				updateRenewalNotice(renewal);
				response.sendRedirect(request.getContextPath() + "/review");
				return;
			} else if (action.equals("accept")) {
				// send to set fee page
				request.getSession().setAttribute("renewalID", renewalID);
				response.sendRedirect(request.getContextPath() + "/setFee");
				return;
				//set fee servlet
			} else if (action.equals("deny")) {
				// send to desc page
				request.getSession().setAttribute("renewalID", renewalID);
				//set description servlet
				response.sendRedirect(request.getContextPath() + "/setDescription");
				return;
			} else if (action.equals("disown")) {
				renewal.setOwnedBy("");
				if (renewal.getStatus().equals(Status.EVALUATING_EXTENSION.toString())){
					renewal.setStatus(Status.EXTENSION_REQUESTED.toString());
				}
				updateRenewalNotice(renewal);
				response.sendRedirect(request.getContextPath() + "/review");
				return;
			}
		}

		List<Renewal> allValidationNotices = new ArrayList<Renewal>();
		List<Renewal> validationNotices = getValidationErrorNotices();
		List<Renewal> extensionRequestedNotices = getExtensionRequestedNotices();
		List<Renewal> extensionEvaluatingNotices = getEvaluatingExtensionNotices();
		allValidationNotices.addAll(validationNotices);
		allValidationNotices.addAll(extensionRequestedNotices);
		allValidationNotices.addAll(extensionEvaluatingNotices);
		List<Renewal> unownedNotices = new ArrayList<Renewal>();
		List<Renewal> ownedNotices = new ArrayList<Renewal>();

		for (Renewal r : allValidationNotices) {
			if (r.getOwnedBy() == null) {
				unownedNotices.add(r);
			} else {
				if (r.getOwnedBy().equals("")) {
					unownedNotices.add(r);
				} else if (r.getOwnedBy().equals(username)) {
					ownedNotices.add(r);
				}
			}
		}

		request.setAttribute("unownedNotices", unownedNotices);
		request.setAttribute("ownedNotices", ownedNotices);

		request.getRequestDispatcher("WEB-INF/jsp/review.jsp").forward(request,
				response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private List<Renewal> getValidationErrorNotices() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		Status status = Status.VALIDATION_ERROR;

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/status/"
						+ status.toString());
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		GenericType<List<Renewal>> genericType = new GenericType<List<Renewal>>() {
		};
		List<Renewal> validationErrorList = new ArrayList<Renewal>();
		try {
			validationErrorList = restResponse.readEntity(genericType);
		} catch (Exception e) {
		}
		restResponse.close();
		return validationErrorList;

	}
	
	private List<Renewal> getExtensionRequestedNotices() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		Status status = Status.EXTENSION_REQUESTED;

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/status/"
						+ status.toString());
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		GenericType<List<Renewal>> genericType = new GenericType<List<Renewal>>() {
		};
		List<Renewal> extensionRequestedList = new ArrayList<Renewal>();
		try {
			extensionRequestedList = restResponse.readEntity(genericType);
		} catch (Exception e) {
		}
		restResponse.close();
		return extensionRequestedList;

	}

	private List<Renewal> getEvaluatingExtensionNotices() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		Status status = Status.EVALUATING_EXTENSION;

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/status/"
						+ status.toString());
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		GenericType<List<Renewal>> genericType = new GenericType<List<Renewal>>() {
		};
		List<Renewal> evaluatingExtensionList = new ArrayList<Renewal>();
		try {
			evaluatingExtensionList = restResponse.readEntity(genericType);
		} catch (Exception e) {
		}
		restResponse.close();
		return evaluatingExtensionList;
	}
	
	private Renewal getRenewal(String renewalID) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/"
						+ renewalID);
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
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
				.header("Authorization", OFFICER_KEY).put(entity);

		restResponse.close();
	}
}
