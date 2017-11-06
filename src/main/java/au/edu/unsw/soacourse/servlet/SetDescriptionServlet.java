package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import au.edu.unsw.soacourse.licence.Licence;
import au.edu.unsw.soacourse.renewal.Renewal;

/**
 * Servlet implementation class SetDescriptionServlet
 */
public class SetDescriptionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String OFFICER_KEY = "OFFICER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SetDescriptionServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getSession().getAttribute("username") == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}
		
		if (request.getSession().getAttribute("renewalID") == null){
			response.sendRedirect(request.getContextPath() + "/review");
			return;
		}
		String renewalID = (String) request.getSession().getAttribute("renewalID");
		String action = (String) request.getParameter("action");
		
		if (action != null) {
			if (action.equals("sendDescription")) {
				String description = (String) request.getParameter("description");
				//delete renewal notice
				deleteRenewalNotice(renewalID);
				sendEmail(renewalID, description);
				response.sendRedirect(request.getContextPath() + "/review");
				return;
			}
		}
		
		
		request.getRequestDispatcher("WEB-INF/jsp/setdescription.jsp").forward(
				request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private void sendEmail(String renewalID, String description) {
		Renewal renewal = getRenewal(renewalID);
		Licence licence = getLicence(String.valueOf(renewal.getLicenceId()));
		String host = "smtp.gmail.com";
		final String email = "licencerestclient@gmail.com";
		final String password = "licencerest123";

		String to = licence.getEmail();

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
			message.setSubject("Your renewal application has been denied");
			message.setText(description);

			// send the message
			Transport.send(message);
			System.out.println("Message sent successfully...");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
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
	
	private void deleteRenewalNotice(String renewalID) {
		ResteasyClient client = new ResteasyClientBuilder().build();

		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/renewals/"
						+ renewalID);

		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).delete();

		restResponse.close();
	}
	

}
