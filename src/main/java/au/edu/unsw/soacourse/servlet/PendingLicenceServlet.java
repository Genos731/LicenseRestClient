package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.GenericType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import au.edu.unsw.soacourse.licence.Licence;
import au.edu.unsw.soacourse.renewal.Renewal;
import au.edu.unsw.soacourse.renewal.Status;

/**
 * Servlet implementation class PendingLicenceServlet
 */
public class PendingLicenceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String OFFICER_KEY = "OFFICER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PendingLicenceServlet() {
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
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		List<Renewal> pendingNotices = getPendingNotices();
		List<Renewal> validationErrorNotices = getValidationErrorNotices();
		List<Renewal> extensionRequestNotices = getExtensionRequestNotices();
		List<Renewal> evaluatingExtensionNotices = getEvaluatingExtensionNotices();
		List<Renewal> extensionApproved = getExtensionApprovedNotices();
		
		List<Renewal> noticeList = pendingNotices;
		noticeList.addAll(validationErrorNotices);
		noticeList.addAll(extensionRequestNotices);
		noticeList.addAll(evaluatingExtensionNotices);
		noticeList.addAll(extensionApproved);

		request.setAttribute("noticeList", noticeList);
		
		request.getRequestDispatcher("WEB-INF/jsp/pending.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private List<Renewal> getPendingNotices() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		Status status = Status.PENDING;

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/status/"
						+ status.toString());
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		GenericType<List<Renewal>> genericType = new GenericType<List<Renewal>>() {
		};
		List<Renewal> pendingList = new ArrayList<Renewal>();
		try {
			pendingList = restResponse.readEntity(genericType);
		} catch (Exception e) {
		}
		restResponse.close();
		return pendingList;
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

	private List<Renewal> getExtensionRequestNotices() {
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
		List<Renewal> extensionRequestList = new ArrayList<Renewal>();
		try {
			extensionRequestList = restResponse.readEntity(genericType);
		} catch (Exception e) {
		}
		restResponse.close();
		return extensionRequestList;
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
	
	private List<Renewal> getExtensionApprovedNotices() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		Status status = Status.EXTENSION_APPROVED;

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/status/"
						+ status.toString());
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		GenericType<List<Renewal>> genericType = new GenericType<List<Renewal>>() {
		};
		List<Renewal> extensionApprovedList = new ArrayList<Renewal>();
		try {
			extensionApprovedList = restResponse.readEntity(genericType);
		} catch (Exception e) {
		}
		restResponse.close();
		return extensionApprovedList;
	}
}
