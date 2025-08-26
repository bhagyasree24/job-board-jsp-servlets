package controller;

import java.io.IOException;

import dao.JobDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "AdminController", value = {
    "/admin/dashboard",
    "/admin/users",
    "/admin/jobs",
    "/admin/deleteUser",
    "/admin/toggleJob"
})
public class AdminController extends HttpServlet {
    private UserDAO userDao;
    private JobDAO jobDao;

    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDAO();
        jobDao = new JobDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!isAdmin(session)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/dashboard":
                    showDashboard(req, resp);
                    break;
                case "/admin/users":
                    listAllUsers(req, resp);
                    break;
                case "/admin/jobs":
                    listAllJobs(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            handleError(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!isAdmin(session)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/deleteUser":
                    deleteUser(req, resp);
                    break;
                case "/admin/toggleJob":
                    toggleJobStatus(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            handleError(req, resp, e);
        }
    }

    private boolean isAdmin(HttpSession session) {
        if (session == null) return false;
        User user = (User) session.getAttribute("user");
        return user != null && "admin".equals(user.getRole());
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        int totalUsers = userDao.countUsersByRole(null);
        int totalJobs = jobDao.countAllActiveJobs();
        int activeEmployers = userDao.countUsersByRole("employer");
        
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalJobs", totalJobs);
        req.setAttribute("activeEmployers", activeEmployers);
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }

    private void listAllUsers(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String roleFilter = req.getParameter("role");
        req.setAttribute("users", userDao.getUsersByRole(roleFilter));
        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    private void listAllJobs(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String statusFilter = req.getParameter("status");
        req.setAttribute("jobs", jobDao.getAllJobs(statusFilter));
        req.getRequestDispatcher("/WEB-INF/views/admin/jobs.jsp").forward(req, resp);
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        int userId = Integer.parseInt(req.getParameter("id"));
        if (userDao.deleteUser(userId)) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?success=User+deleted");
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/users?error=Delete+failed");
        }
    }

    private void toggleJobStatus(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        int jobId = Integer.parseInt(req.getParameter("id"));
        String newStatus = jobDao.getJobStatus(jobId).equals("active") ? "inactive" : "active";
        
        if (jobDao.updateJobStatus(jobId, newStatus)) {
            resp.sendRedirect(req.getContextPath() + "/admin/jobs?success=Job+status+updated");
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/jobs?error=Update+failed");
        }
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, Exception e) 
            throws ServletException, IOException {
        e.printStackTrace();
        req.setAttribute("errorMessage", e.getMessage());
        req.getRequestDispatcher("/WEB-INF/views/errors/admin-error.jsp").forward(req, resp);
    }
}