package au.edu.unsw.soacourse.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import au.edu.unsw.soacourse.licence.Licence;
import au.edu.unsw.soacourse.renewal.Renewal;

/**
 * Servlet implementation class LicenceServlet
 */
public class LicenceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String OFFICER_KEY = "OFFICER@#$";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LicenceServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getParameter("id") == null) {
			// System.out.println("SUCCESS");
			// 404
			return;
		}
		
		String licenceID = (String) request.getParameter("id");
		Licence l = getLicence(licenceID);
		request.setAttribute("licence", l);
		
		request.getRequestDispatcher("WEB-INF/jsp/licence.jsp").forward(request,
				response);
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
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
}
