package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import comp9322.assignment1.CheckAddressRequest;
import comp9322.assignment1.CheckAddressResponse;
import comp9322.assignment1.CheckEmailAddressRequest;
import comp9322.assignment1.CheckEmailAddressResponse;
import comp9322.assignment1.EmployeeValidationService;
import comp9322.assignment1.EmployeeValidationServiceImplService;
import comp9322.assignment1.ObjectFactory;
import comp9322.assignment1.ValidationFaultMsg;
import au.edu.unsw.soacourse.licence.Licence;
import au.edu.unsw.soacourse.renewal.Renewal;
import au.edu.unsw.soacourse.renewal.Status;

/**
 * Servlet implementation class ValidateServlet
 */
public class ValidateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER_KEY = "DRIVER@#$";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ValidateServlet() {
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
		if (request.getParameter("renewalID") == null) {
			// System.out.println("SUCCESS");
			// 404
			return;
		}

		String renewalID = (String) request.getParameter("renewalID");
		Renewal renewal = getRenewal(renewalID);
		// check current status of renewal notice

		// if pending work as normal
		// if extension requested, do not allow them access to servlets outside
		// of validate
		// if completed don't show anything
		// if evaluating extension or validation error tell them it's being
		// evaluated
		// if extension approved, forward to payment page

		String status = renewal.getStatus();

		if (status.equals(Status.COMPLETED.toString())) {
			return;
		} else if (status.equals(Status.EVALUATING_EXTENSION.toString())) {
			request.getRequestDispatcher("WEB-INF/jsp/waitingreview.jsp")
					.forward(request, response);
			return;
		} else if (status.equals(Status.VALIDATION_ERROR.toString())) {
			request.getRequestDispatcher("WEB-INF/jsp/validationfail.jsp")
					.forward(request, response);
			return;
		}

		if (request.getParameter("action") != null) {
			String action = (String) request.getParameter("action");
			if (action.equals("validate")) {
				// validate details here
				// if fails give them fail page
				// update renewal notice to be validation error
				//String newAddress = (String) request.getParameter("newAddress");
				String preStreet = (String) request.getParameter("preStreet");
				String streetName = (String) request.getParameter("streetName");
				String streetType = (String) request.getParameter("streetType");
				String suburb = (String) request.getParameter("suburb");
				String state = (String) request.getParameter("state");
				String newEmail = (String) request.getParameter("newEmail");
				// validateAddress
				// validateEmail
				// String validatedEmail = validateEmail(newEmail);
				// if (validatedEmail == null){
				// System.out.println("FAILED EMAIL");
				// }
				
				String oldAddress = preStreet+" "+streetName+" "+streetType+" "+suburb+" "+state;
				oldAddress = oldAddress.trim().replaceAll(" +", " ");
				String checkedAddress = validateAddress(preStreet, streetName, streetType, suburb, state);

				// update renewal notice
				renewal.setAddress(oldAddress);
				renewal.setEmail(newEmail);
				updateRenewalNotice(renewal);

				Integer numFail = new Integer(0);
				if (request.getSession().getAttribute("numFail") != null) {
					numFail = (Integer) request.getSession().getAttribute(
							"numFail");
					System.out.println("NUMFAIL attribute: " + numFail);
				}
				System.out.println("FAILED " + numFail);

				// if fails validation
				if (checkedAddress == null || !validateEmail(newEmail)) {
					numFail++;
					request.getSession().setAttribute("numFail", numFail);
					System.out.println("FAILED VALIDATION NUMFAIL " + numFail);
					String errorMessage = "";
					if (!validateEmail(newEmail)){
						errorMessage = errorMessage+"Email is invalid<br>";
					}
					if (checkedAddress == null){
						errorMessage = errorMessage+"Address is invalid<br>";
					}
					request.getSession().setAttribute("errorMessage", errorMessage);
					
					if (numFail < 4) {
						response.sendRedirect(request.getContextPath()
								+ "/validate?renewalID=" + renewalID);
						return;
					} else {
						renewal.setOwnedBy("");
						renewal.setStatus(Status.VALIDATION_ERROR.toString());
						updateRenewalNotice(renewal);
						request.getRequestDispatcher(
								"WEB-INF/jsp/validationfail.jsp").forward(
								request, response);
						return;
					}

				} else {
					request.getSession().setAttribute("numFail", 0);
					request.getSession().removeAttribute("errorMessage");
				}

				request.getSession().setAttribute("renewalID", renewalID);
				// determine next page based on status
				if (status.equals(Status.EXTENSION_APPROVED.toString())) {
					response.sendRedirect(request.getContextPath() + "/payment");
					return;
				}
				else {
					response.sendRedirect(request.getContextPath()
							+ "/extensionrequest");
					return;
				}
			}
		}

		Licence licence = getLicence(String.valueOf(renewal.getLicenceId()));
		request.setAttribute("currentLicence", licence);
		request.setAttribute("oldAddress", renewal.getAddress());
		request.setAttribute("oldEmail", renewal.getEmail());
		request.setAttribute("renewalID", renewalID);

		if (request.getSession().getAttribute("numFail") == null) {
			request.getSession().setAttribute("numFail", new Integer(0));
		} else {
			Integer numFail = (Integer) request.getSession().getAttribute(
					"numFail");
			if (numFail >= 4) {
				request.getSession().setAttribute("numFail", new Integer(0));
			}
		}

		request.getRequestDispatcher("WEB-INF/jsp/validate.jsp").forward(
				request, response);
		return;
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

	private String validateAddress(String preStreet, String streetName,
			String streetType, String suburb, String state) {
		URL soapService = null;
		try {
			soapService = new URL(
					"http://192.168.99.100:8888/assignment1/EmployeeValidation?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		EmployeeValidationServiceImplService employeeValidationServiceImpl = new EmployeeValidationServiceImplService(
				soapService);
		EmployeeValidationService employeeValidationService = employeeValidationServiceImpl
				.getEmployeeValidationServiceImplPort();
		ObjectFactory o = new ObjectFactory();
		CheckAddressRequest checkAddressRequest = o.createCheckAddressRequest();
		checkAddressRequest.setPreStreet(preStreet);
		checkAddressRequest.setStreetName(streetName);
		checkAddressRequest.setStreetType(streetType);
		checkAddressRequest.setSuburb(suburb);
		checkAddressRequest.setState(state);
		
		try {
			CheckAddressResponse checkAddressResponse = employeeValidationService.checkAddress(checkAddressRequest);
			String exactAddress = checkAddressResponse.getExactAddress();
			return exactAddress;
		} catch (ValidationFaultMsg e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private boolean validateEmail(String newEmail) {
		URL soapService = null;
		try {
			soapService = new URL(
					"http://192.168.99.100:8888/assignment1/EmployeeValidation?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		EmployeeValidationServiceImplService employeeValidationServiceImpl = new EmployeeValidationServiceImplService(
				soapService);
		EmployeeValidationService employeeValidationService = employeeValidationServiceImpl
				.getEmployeeValidationServiceImplPort();
		ObjectFactory o = new ObjectFactory();
		CheckEmailAddressRequest checkEmailAddressRequest = o
				.createCheckEmailAddressRequest();
		checkEmailAddressRequest.setEmail(newEmail);

		CheckEmailAddressResponse checkEmailAddressResponse = employeeValidationService
				.checkEmailAddress(checkEmailAddressRequest);

		return checkEmailAddressResponse.isValue();
	}

	/*
	 * 
	 * private String validateAddress(String preStreet, String streetName,
	 * String streetType, String suburb, String state) throws
	 * MalformedURLException { EmployeeValidationServiceImplService
	 * employeeValidationServiceImpl = new EmployeeValidationServiceImplService(
	 * new URL(
	 * "http://192.168.99.100:8888/assignment1/EmployeeValidation?wsdl"));
	 * EmployeeValidationService employeeValidationService =
	 * employeeValidationServiceImpl .getEmployeeValidationServiceImplPort();
	 * ObjectFactory o = new ObjectFactory(); CheckAddressRequest
	 * checkAddressRequest = o.createCheckAddressRequest();
	 * 
	 * checkAddressRequest.setPreStreet(preStreet);
	 * checkAddressRequest.setStreetName(streetName);
	 * checkAddressRequest.setStreetType(streetType);
	 * checkAddressRequest.setSuburb(suburb);
	 * checkAddressRequest.setState(state);
	 * 
	 * String exactAddress = null;
	 * 
	 * try { CheckAddressResponse checkAddressResponse =
	 * employeeValidationService .checkAddress(checkAddressRequest);
	 * exactAddress = checkAddressResponse.getExactAddress(); } catch
	 * (ValidationFaultMsg e) { e.printStackTrace(); } return exactAddress; }
	 * 
	 * private String validateEmail(String email) throws MalformedURLException{
	 * EmployeeValidationServiceImplService employeeValidationServiceImpl = new
	 * EmployeeValidationServiceImplService( new URL(
	 * "http://192.168.99.100:8888/assignment1/EmployeeValidation?wsdl"));
	 * EmployeeValidationService employeeValidationService =
	 * employeeValidationServiceImpl .getEmployeeValidationServiceImplPort();
	 * ObjectFactory o = new ObjectFactory(); CheckEmailAddressRequest
	 * checkEmailAddressRequest = o.createCheckEmailAddressRequest();
	 * checkEmailAddressRequest.setEmail(email);
	 * 
	 * String retEmail = null;
	 * 
	 * try { CheckEmailAddressResponse checkEmailAddressResponse =
	 * employeeValidationService .checkEmailAddress(checkEmailAddressRequest);
	 * if (checkEmailAddressResponse.isValue()){ retEmail = email; } } catch
	 * (Exception e){ e.printStackTrace(); }
	 * 
	 * return retEmail; }
	 */

}
