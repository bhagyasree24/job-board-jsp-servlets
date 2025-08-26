package controller;

import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "AuthController", value = { "/login", "/register", "/logout" })
public class AuthController extends HttpServlet {

	private UserDAO userDao;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		userDao = new UserDAO();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String path = req.getServletPath();
		if (path.equals("/login")) {
			String email = req.getParameter("email");
			String password = req.getParameter("password");
			
			if(email==null||password==null||email.isEmpty()||password.isEmpty()) {
				req.setAttribute("error", "Invalid email or password format");
			    req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
			    return;
			}

			User user = userDao.login(email, password);
			if (user != null) {
				HttpSession session = req.getSession();
				session.setAttribute("user", user);

				switch (user.getRole()) {
				case "admin":
					resp.sendRedirect("admin/dashboard.jsp");
					break;
				case "employer":
					resp.sendRedirect("employer/dashboard.jsp");
					break;
				case "jobseeker":
					resp.sendRedirect("jobseeker/dashboard.jsp");
					break;
				default:
                    resp.sendRedirect(req.getContextPath() + "/auth/login.jsp"); 
                    break;
				}
				

			} else {
				req.setAttribute("error", "Invalid email or password");
				req.getRequestDispatcher("/auth/login.jsp").forward(req, resp);
			}
		} else if (path.equals("/register")) {
			String name = req.getParameter("name");
			String email = req.getParameter("email");
			String password = req.getParameter("password");
			String role = req.getParameter("role");

			User user = new User(name, email, password, role);

			if (userDao.register(user)) {
				resp.sendRedirect("auth/login.jsp?register=success");
			} else {
				req.setAttribute("error", "Registration failed. Email may already exist.");
				req.getRequestDispatcher("/auth/register.jsp").forward(req, resp);
			}

		}
		else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid request");
        }

	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String path = req.getServletPath();
		
		if(path.equals("/logout")) {
			HttpSession session = req.getSession(false);
			if (session!=null) {
				session.invalidate();
			}
			resp.sendRedirect(req.getContextPath()+"/auth/login.jsp");
		}
	}
	
}
