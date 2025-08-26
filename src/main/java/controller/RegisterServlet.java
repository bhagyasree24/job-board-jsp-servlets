package controller;

import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Retrieve form data
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String role = req.getParameter("role");

            // Validate input
            if (name == null || email == null || password == null || role == null ||
                name.isEmpty() || email.isEmpty()|| password.length() < 6 || password.isEmpty() || role.isEmpty()) {
            	req.setAttribute("error", "Invalid input. Please check all fields.");
                req.getRequestDispatcher("auth/register.jsp").forward(req, resp);
                return;
            }

            // Create User object
            User user = new User(name, email, password, role);
            UserDAO userDAO = new UserDAO();

            // Attempt registration
            boolean isRegistered = userDAO.register(user);

            if (isRegistered) {
                resp.sendRedirect("auth/login.jsp?success=1");
            } else {
                resp.sendRedirect("auth/register.jsp?error=Email+already+exists");
            }

        } catch (Exception e) {
            // Log the error
            e.printStackTrace();

            // Redirect to error page or show friendly message
            req.setAttribute("error", "Internal server error occurred.");
            req.getRequestDispatcher("auth/register.jsp").forward(req, resp);
        }
    }
}
