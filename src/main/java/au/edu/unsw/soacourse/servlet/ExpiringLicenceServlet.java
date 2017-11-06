package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.mail.*;
import javax.mail.internet.*;

import au.edu.unsw.soacourse.licence.Licence;
import au.edu.unsw.soacourse.renewal.Renewal;
import au.edu.unsw.soacourse.renewal.Status;

/**
 * Servlet implementation class LicenceServlet
 */
@WebServlet({ "/ExpiringLicenceServlet", "/expiring" })
public class ExpiringLicenceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String OFFICER_KEY = "OFFICER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ExpiringLicenceServlet() {
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

		// get date + 60 days
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 60);
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
		String expiryDate = sdf.format(calendar.getTime());

		String action = (String) request.getParameter("action");
		if (action != null) {
			if (action.equals("email")) {
				String[] selectedLicenceId = request
						.getParameterValues("renewSelection");
				for (String id : selectedLicenceId) {
					System.out.println(id);
					// create renewal notice
					// payment
					String renewalID = createRenewalNotice(id);

					// email each of these id's
					// send a link to renewal notice
					sendEmail(id, renewalID);
				}
			}
			// if action.equals("changedate")
		}

		// GET expiring licences from today
		List<Licence> expiredLicenceList = getExpiringLicence(expiryDate);

		request.setAttribute("licenceList", expiredLicenceList);

		request.getRequestDispatcher("WEB-INF/jsp/expiring.jsp").forward(
				request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void sendEmail(String licenceID, String renewalID) {
		Licence licence = getLicence(licenceID);
		String host = "smtp.gmail.com";
		final String email = "licencerestclient@gmail.com";
		final String password = "licencerest123";

		String to = licence.getEmail();
		String renewalLink = "http://localhost:8080/LicenceRestClient/validate?renewalID="+renewalID;

		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "587");

		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(email, password);
					}
				});

		// Compose the message
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject("Your drivers licence is going to expire");
			message.setText("Please renew with the following link \n"
					+ renewalLink);

			// send the message
			Transport.send(message);
			System.out.println("Message sent successfully...");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private String createRenewalNotice(String licenceID) {
		Licence licence = getLicence(licenceID);
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals");
		Form form = new Form();
		form.param("address", licence.getAddress())
				.param("email", licence.getEmail())
				.param("licenceId", licenceID);
		Entity<Form> entity = Entity.form(form);
		javax.ws.rs.core.Response response = target
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", OFFICER_KEY).post(entity);

		String value = response.readEntity(String.class);
		System.out.println("RESPONSE: " + value);

		// Get renewal ID in location
		String responseURI = response.getLocation().toString();
		String[] split = responseURI.split("/");
		String renewalID = split[split.length - 1];

		System.out.println("Renewal ID: " + renewalID);
		createPayment(renewalID);

		response.close();

		return renewalID;
	}

	private void createPayment(String renewalID) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/payments");
		Form form = new Form();
		form.param("renewalId", renewalID).param("amount", "160.0");
		Entity<Form> entity = Entity.form(form);
		javax.ws.rs.core.Response response = target
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", OFFICER_KEY).post(entity);

		String value = response.readEntity(String.class);
		System.out.println(value);
		response.close();

		return;
	}

	private Licence getLicence(String licenceID) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		// GET licence by id from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/licences/"
						+ licenceID);
		javax.ws.rs.core.Response response = target.request()
				.header("Authorization", OFFICER_KEY).get();
		Licence licence = response.readEntity(Licence.class);
		response.close();

		return licence;
	}

	private List<Licence> getExpiringLicence(String expiryDate) {
		ResteasyClient client = new ResteasyClientBuilder().build();

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/licences/expiring/"
						+ expiryDate);
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		GenericType<List<Licence>> genericType = new GenericType<List<Licence>>() {
		};
		List<Licence> expiredLicenceList = restResponse.readEntity(genericType);
		restResponse.close();

		// get all renewal notices here
		// for each licenceid in renewal notices
		// remove from expiring licences
		//List<Renewal> pendingList = g
		List<Renewal> pendingNotices = getPendingNotices();
		List<Renewal> validationErrorNotices = getValidationErrorNotices();
		List<Renewal> extensionRequestNotices = getExtensionRequestNotices();
		List<Renewal> evaluatingExtensionNotices = getEvaluatingExtensionNotices();
		List<Renewal> extensionApprovedNotices = getExtensionApprovedNotices();
		
		List<Renewal> incompleteNotices = pendingNotices;
		incompleteNotices.addAll(validationErrorNotices);
		incompleteNotices.addAll(extensionRequestNotices);
		incompleteNotices.addAll(evaluatingExtensionNotices);
		incompleteNotices.addAll(extensionApprovedNotices);
		
		//list of licence id's that have been sent renewal notices
		List<Integer> incompleteLicenceId = new ArrayList<Integer>();
		for (Renewal r : incompleteNotices){
			if (!incompleteLicenceId.contains(r.getLicenceId())){
				incompleteLicenceId.add(r.getLicenceId());
			}
		}
		
		Iterator<Licence> it = expiredLicenceList.iterator();
		while (it.hasNext()) {
		    if (incompleteLicenceId.contains(it.next().getId())) {
		        it.remove();
		    }
		}

		return expiredLicenceList;
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
		}
		catch (Exception e){
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
		}
		catch (Exception e){
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
		}
		catch (Exception e){
		}
		restResponse.close();
		return extensionRequestList;

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
		}
		catch (Exception e){
		}
		restResponse.close();
		return evaluatingExtensionList;
	}

	private List<Renewal> getCompletedNotices() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		Status status = Status.COMPLETED;
		
		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/status/"
						+ status.toString());
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		GenericType<List<Renewal>> genericType = new GenericType<List<Renewal>>() {
		};
		List<Renewal> completedList = new ArrayList<Renewal>();
		try {
			completedList = restResponse.readEntity(genericType);
		}
		catch (Exception e){
		}
		restResponse.close();
		return completedList;
	}
}
