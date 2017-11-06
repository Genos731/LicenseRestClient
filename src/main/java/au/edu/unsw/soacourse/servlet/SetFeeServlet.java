package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import au.edu.unsw.soacourse.payment.Payment;
import au.edu.unsw.soacourse.renewal.Renewal;
import au.edu.unsw.soacourse.renewal.Status;

/**
 * Servlet implementation class SetFeeServlet
 */
public class SetFeeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String OFFICER_KEY = "OFFICER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SetFeeServlet() {
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
			if (action.equals("setNewFee")) {
				String newValue = (String) request.getParameter("newValue");
				
				Renewal notice = getRenewal(renewalID);
				Payment payment = getPayment(String.valueOf(notice.getPaymentId()));
				payment.setAmount(Double.parseDouble(newValue));
				//if old status was validation error set to pending
				if (notice.getStatus().equals(Status.VALIDATION_ERROR.toString())){
					notice.setOwnedBy("");
					notice.setStatus(Status.PENDING.toString());
				}
				
				//if old status was extensionrequested
				//set to extension approved
				else if (notice.getStatus().equals(Status.EVALUATING_EXTENSION.toString())){
					notice.setOwnedBy("");
					notice.setStatus(Status.EXTENSION_APPROVED.toString());
				}
				
				//update notice and payment
				updateRenewalNotice(notice);
				updatePayment(payment);
				
				// send email informing that fee is set
				sendEmail(String.valueOf(notice.getId()));
				response.sendRedirect(request.getContextPath() + "/review");
				return;
			}
		}
		
		Renewal notice = getRenewal(renewalID);
		Payment payment = getPayment(String.valueOf(notice.getPaymentId()));
		request.setAttribute("payment", payment);
		
		request.getRequestDispatcher("WEB-INF/jsp/setfee.jsp").forward(request,
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

	private Payment getPayment(String paymentID) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/payments/"
						+ paymentID);
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).get();
		Payment p = null;
		try {
			p = restResponse.readEntity(Payment.class);
		} catch (Exception e) {
		}
		restResponse.close();
		return p;
	}
	
	private void updatePayment(Payment payment) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		
		//turn date into string of DDMMYYYY
		String paymentDate = formatDate(payment.getPaidDate());
		
		// GET expiring licences from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/payments/"
						+ String.valueOf(payment.getId()));
		Form form = new Form();
		form.param("id", String.valueOf(payment.getId()))
				.param("amount", String.valueOf(payment.getAmount()))
				.param("paidDate", paymentDate);
		Entity<Form> entity = Entity.form(form);

		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", OFFICER_KEY).put(entity);

		restResponse.close();
		
	}
	
	private String formatDate(Date date) {
		if (date == null){
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		SimpleDateFormat format1 = new SimpleDateFormat("ddMMyyyy");
		return format1.format(cal.getTime());
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
	
	private void sendEmail(String renewalID) {
		Renewal renewal = getRenewal(renewalID);
		Licence licence = getLicence(String.valueOf(renewal.getLicenceId()));
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
			message.setSubject("Your drivers licence renewal is ready to be paid");
			message.setText("Please renew with the following link \n"
					+ renewalLink);

			// send the message
			Transport.send(message);
			System.out.println("Message sent successfully...");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}


}
