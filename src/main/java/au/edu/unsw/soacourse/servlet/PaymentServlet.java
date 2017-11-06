package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
 * Servlet implementation class PaymentServlet
 */
public class PaymentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER_KEY = "DRIVER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PaymentServlet() {
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
		}

		// check action
		// if action = confirmPay, update licence resource and give that page
		String action = (String) request.getParameter("action");
		if (action != null) {
			if (action.equals("confirmPay")) {
				Renewal r = getRenewal(renewalID);
				Payment p = getPayment(String.valueOf(r.getPaymentId()));
				Licence l = getLicence(String.valueOf(r.getLicenceId()));
				
				r.setStatus(Status.COMPLETED.toString());
				p.setPaidDate(new Date());
				updateRenewalNotice(r);
				updatePayment(p);
				
				// update licence with values in renewal
				if (status.equals(Status.EXTENSION_APPROVED.toString())) {
					//update licence for 5 years
					updateLicence(renewal, l, 5);
				} else if (status.equals(Status.PENDING.toString())) {
					//update licence for 1 year
					updateLicence(renewal, l, 1);
				}
				
				//send them to licence resource page
				response.sendRedirect(request.getContextPath() + "/licence?id="+String.valueOf(l.getId()));
				return;
			}
		}

		Renewal notice = getRenewal(renewalID);
		Payment payment = getPayment(String.valueOf(notice.getPaymentId()));
		System.out.println("PAYMENT" + payment.getAmount());
		request.setAttribute("payment", payment);

		request.getRequestDispatcher("WEB-INF/jsp/payment.jsp").forward(
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

	private Payment getPayment(String paymentID) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/payments/"
						+ paymentID);
		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", DRIVER_KEY).get();
		Payment p = null;
		try {
			p = restResponse.readEntity(Payment.class);
		} catch (Exception e) {
		}
		restResponse.close();
		return p;
	}
	
	private Licence getLicence(String licenceID) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		// GET licence by id from today
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/licences/"
						+ licenceID);
		javax.ws.rs.core.Response response = target.request()
				.header("Authorization", DRIVER_KEY).get();
		Licence licence = response.readEntity(Licence.class);
		response.close();

		return licence;
	}
	
	//returns id of licence
	private void updateLicence(Renewal renewal, Licence licence, int years){
		//convert renewal date
		Date expiryDate = licence.getExpiryDate();
		//add years
		Calendar c = Calendar.getInstance();
		c.setTime(expiryDate);
		c.add(Calendar.YEAR, years);
		expiryDate = c.getTime();
		
		//convert to ddmmyyyy
		//ddmmyyy
		String fmtExpiryDate = formatDate(expiryDate);
		
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client
				.target("http://localhost:8080/LicenceRestService/licences/"
						+ String.valueOf(licence.getId()));
		Form form = new Form();
		form.param("address", renewal.getAddress())
				.param("email", renewal.getEmail())
				.param("expiryDate", fmtExpiryDate);
		Entity<Form> entity = Entity.form(form);

		javax.ws.rs.core.Response restResponse = target.request()
				.header("Authorization", DRIVER_KEY).put(entity);

		restResponse.close();
	}
	
	//update payment (paid by date)
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
				.header("Authorization", DRIVER_KEY).put(entity);

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
}
