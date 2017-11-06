package au.edu.unsw.soacourse.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import au.edu.unsw.soacourse.accessor.OfficerAccessor;
import au.edu.unsw.soacourse.container.Officer;

/**
 * Servlet implementation class OfficerLogin
 */
@WebServlet({ "/OfficerLoginServlet", "/", "/login" })
public class OfficerLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OfficerLoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String username = (String) request.getAttribute("username");

		if (request.getSession().getAttribute("username") == null) {
			request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(
					request, response);
			return;
		} else {
			response.sendRedirect(request.getContextPath() + "/expiring");
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		if (username == null) {
			//no username supplied
			request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(request, response);
		} else {
			OfficerAccessor accessor = new OfficerAccessor();
			Officer o = null;
			// check if username and password is a real account
			try {
				o = accessor.getOfficer(username);
			} catch (SQLException e) {
				// database error
				e.printStackTrace();
				request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(request, response);
				try {
					accessor.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				return;
			}
			
			try {
				accessor.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			if (o == null) {
				// user doesn't exist
				System.out.println("INCORRECT USERNAME");
				request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(request, response);
				return;
			} else {
				// do password check
				if (!password.equals(o.getPassword())) {
					System.out.println("INCORRECT PASSWORD");
					request.getRequestDispatcher("WEB-INF/jsp/login.jsp").forward(request, response);
					return;
				} else {
					//login successful
					//place username in session
					System.out.println("LOGIN SUCCESS");
					request.getSession().setAttribute("username", username);
					response.sendRedirect(request.getContextPath() + "/expiring");
					return;
				}
			}
		}
	}
	
}
