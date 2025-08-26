package controller;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebFilter(filterName = "AuthFilter", urlPatterns = {
    "/admin/*",
    "/employer/*",
    "/jobseeker/*",
    "/secured/*"
})
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PATHS = {
        "/auth/login",
        "/auth/register",
        "/assets/"
    };

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // Bypass filter for public resources
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Redirect to login if not authenticated
        if (user == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?error=Please login first");
            return;
        }

        // Check role-based access
        if (!hasAccess(user.getRole(), path)) {
            handleUnauthorizedAccess(httpRequest,httpResponse, user);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAccess(String role, String path) {
        if (path.startsWith("/admin/")) {
            return "admin".equals(role);
        }
        if (path.startsWith("/employer/")) {
            return "employer".equals(role);
        }
        if (path.startsWith("/jobseeker/")) {
            return "jobseeker".equals(role);
        }
        return true; // Allow access to other secured paths
    }

    private void handleUnauthorizedAccess(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
    	String redirectPath;
    	switch (user.getRole()) {
    	    case "admin":
    	        redirectPath = "/admin/dashboard";
    	        break;
    	    case "employer":
    	        redirectPath = "/employer/dashboard";
    	        break;
    	    case "jobseeker":
    	        redirectPath = "/jobseeker/dashboard";
    	        break;
    	    default:
    	        redirectPath = "/auth/login";
    	        break;
    	}

        response.sendRedirect(response.encodeRedirectURL(
            request.getContextPath() + redirectPath + "?error=Unauthorized access"));
    }

    public void init(FilterConfig config) throws ServletException {
        // Initialization code if needed
    }

    public void destroy() {
        // Cleanup code if needed
    }
}