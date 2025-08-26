package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import dao.ApplicationDAO;
import dao.JobDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.Application;
import model.Job;
import model.User;
import service.NotificationService;

@MultipartConfig
@WebServlet(name = "JobController", value = { "/jobs", "/jobs/post", "/jobs/edit", "/jobs/update", "/jobs/delete",
		"/jobs/search", "/jobs/view", "/jobs/apply" })

public class JobController extends HttpServlet {
	private JobDAO jobDao;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		jobDao = new JobDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = req.getServletPath();
		HttpSession session = req.getSession(false);

		if (session == null || session.getAttribute("user") == null) {
			resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
			return;
		}

		User user = (User) session.getAttribute("user");

		try {

			switch (path) {
			case "/jobs":
				handleViewAllJobs(req, resp);
				break;
			case "/jobs/view":
				handleViewJob(req, resp, user);
				break;
			case "/jobs/post":
				req.getRequestDispatcher("/employer/post-job.jsp").forward(req, resp);
				break;
			case "/jobs/edit":
				handleEditJob(req, resp, user);
				break;
			case "/jobs/search":
				handleSearchJobs(req, resp);
				break;

			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = req.getServletPath();
		HttpSession session = req.getSession(false);

		if (session == null || session.getAttribute("user") == null) {
			resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
			return;
		}

		User user = (User) session.getAttribute("user");

		try {
			switch (path) {
			case "/jobs/post":
				handlePostJob(req, resp, user);
				break;
			case "/jobs/update":
				handleUpdateJob(req, resp, user);
				break;
			case "/jobs/delete":
				handleDeleteJob(req, resp, user);
				break;
			case "/jobs/apply":
				handleApplyJobs(req, resp, user);
				break;
			case "/applications/updateStatus":
			    handleUpdateApplicationStatus(req, resp, user);
			    break;

			default:
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	private void handleViewAllJobs(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		List<Job> jobs = jobDao.getAllActiveJobs();
		req.setAttribute("jobs", jobs);
		req.getRequestDispatcher("/jobseeker/search.jsp").forward(req, resp);
	}

	private void handleViewJob(HttpServletRequest req, HttpServletResponse resp, User user)
			throws ServletException, IOException {
		int jobId = Integer.parseInt(req.getParameter("id"));

		Job job = jobDao.getJobById(jobId);

		if (job == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		req.setAttribute("job", job);
		if (user.getRole().equals("employer") && job.getEmployerId() == user.getUserId()) {
			req.getRequestDispatcher("/employer/job-details.jsp").forward(req, resp);
		} else {
			req.getRequestDispatcher("/jobseeker/job-details.jsp").forward(req, resp);
		}

	}

	private void handleEditJob(HttpServletRequest req, HttpServletResponse resp, User user)
			throws ServletException, IOException {
		if (!(user.getRole().equals("employer"))) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		int jobId = Integer.parseInt(req.getParameter("id"));
		Job job = jobDao.getJobById(jobId);

		if (job == null || job.getEmployerId() != user.getUserId()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		req.setAttribute("job", job);
		req.getRequestDispatcher("/employer/edit-job.jsp").forward(req, resp);
	}

	private void handlePostJob(HttpServletRequest req, HttpServletResponse resp, User user)
			throws ServletException, IOException {

		if (!(user.getRole().equals("employer"))) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		String title = req.getParameter("title");
		String description = req.getParameter("description");
		String category = req.getParameter("category");
		String salary = req.getParameter("salary");
		String location = req.getParameter("location");
		
		if (title == null || title.trim().length() < 3 ||
			    description == null || description.trim().length() < 10 ||
			    category == null || salary == null || location == null ||
			    title.isEmpty() || description.isEmpty() || category.isEmpty() ||
			    salary.isEmpty() || location.isEmpty()) {

			    req.setAttribute("error", "All fields must be filled in properly");
			    req.getRequestDispatcher("/employer/post-job.jsp").forward(req, resp);
			    return;
			}

		Job job = new Job();
		job.setEmployerId(user.getUserId());
		job.setTitle(title);
		job.setDescription(description);
		job.setCategory(category);
		job.setSalary(salary);
		job.setLocation(location);
		
		

		if (jobDao.postJob(job)) {
			resp.sendRedirect(req.getContextPath() + "/employer/dashboard.jsp?success=job_posted");
		} else {
			req.setAttribute("error", "Failed to post job");
			req.getRequestDispatcher("/employer/post-job.jsp").forward(req, resp);
		}
	}

	private void handleUpdateJob(HttpServletRequest req, HttpServletResponse resp, User user)
			throws ServletException, IOException {
		if (!(user.getRole().equals("employer"))) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		int jobId = Integer.parseInt(req.getParameter("id"));
		Job existingJob = jobDao.getJobById(jobId);

		if (existingJob == null || existingJob.getEmployerId() != user.getUserId()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Job job = new Job();
		job.setJobId(jobId);
		job.setCategory(req.getParameter("category"));
		job.setDescription(req.getParameter("description"));
		job.setEmployerId(user.getUserId());
		job.setLocation(req.getParameter("location"));
		job.setSalary(req.getParameter("salary"));
		job.setTitle(req.getParameter("title"));
		job.setStatus(existingJob.getStatus());

		if (jobDao.updateJob(job)) {
			resp.sendRedirect(req.getContextPath() + "/employer/dashboard.jsp?success=job_updated");
		} else {
			req.setAttribute("error", "Failed to update job");
			req.setAttribute("job", job);
			req.getRequestDispatcher("/employer/edit-job.jsp").forward(req, resp);
		}

	}

	private void handleDeleteJob(HttpServletRequest req, HttpServletResponse resp, User user)
			throws ServletException, IOException {
		if (!"employer".equals(user.getRole())) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		int jobId = Integer.parseInt(req.getParameter("id"));
		Job job = jobDao.getJobById(jobId);

		if (job == null || job.getEmployerId() != user.getUserId()) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		if (jobDao.deleteJob(jobId)) {
			resp.sendRedirect(req.getContextPath() + "/employer/dashboard.jsp?success=job_deleted");
		} else {
			resp.sendRedirect(req.getContextPath() + "/employer/dashboard.jsp?error=delete_failed");
		}
	}

	private void handleSearchJobs(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		String keyword = req.getParameter("keyword");
		List<Job> jobs = jobDao.searchJob(keyword);
		req.setAttribute("jobs", jobs);
		req.setAttribute("searchKeyword", keyword);
		req.getRequestDispatcher("/jobseeker/search.jsp").forward(req, resp);
	}

	private void handleApplyJobs(HttpServletRequest req, HttpServletResponse resp, User user)
			throws IOException, ServletException {
		if (!"jobseeker".equals(user.getRole())) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only job seekers can apply for jobs");
			return;
		}

		try {

			int jobId = Integer.parseInt(req.getParameter("jobId"));

			Job job = jobDao.getJobById(jobId);
			if (job == null || !job.getStatus().equals("active")) {
				req.setAttribute("error", "Job not available");
				resp.sendRedirect(req.getContextPath() + "/jobseeker/dashboard.jsp");
				return;
			}

			ApplicationDAO applicationDao = new ApplicationDAO();
			if (applicationDao.hasAlreadyApplied(user.getUserId(), jobId)) {
				req.setAttribute("error", "You have already applied for this job");
				resp.sendRedirect(req.getContextPath() + "/jobs/view?id=" + jobId);
				return;
			}
			
			
	        Part resumePart = req.getPart("resume");
	        if (resumePart == null || resumePart.getSize() == 0) {
	            resp.sendRedirect(req.getContextPath() + "/jobs/view?id=" + jobId + "&error=Resume is required");
	            return;
	        }

	        String originalFileName = Paths.get(resumePart.getSubmittedFileName()).getFileName().toString();
	        String newFileName = "resume_" + user.getUserId() + "_" + System.currentTimeMillis() + "_" + originalFileName;

	        String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads/resumes";
	        File uploadDir = new File(uploadPath);
	        if (!uploadDir.exists()) uploadDir.mkdirs();

	        resumePart.write(uploadPath + File.separator + newFileName);
	        String resumePath = "uploads/resumes/" + newFileName;

			Application application = new Application();
			application.setJobId(jobId);
			application.setJobseekerId(user.getUserId());
			application.setStatus("pending");
			application.setResumePath(resumePath);

			if (applicationDao.applyForJob(application)) {
				// Send notification (optional)
				sendApplicationConfirmation(user, job);

				resp.sendRedirect(
						req.getContextPath() + "/jobseeker/dashboard.jsp?success=Application submitted successfully");
			} else {
				req.setAttribute("error", "Failed to submit application");
				resp.sendRedirect(req.getContextPath() + "/jobs/view?id=" + jobId);
			}
		} catch (NumberFormatException e) {
			req.setAttribute("error", "Invalid job ID format");
			resp.sendRedirect(req.getContextPath() + "/jobseeker/dashboard.jsp");
		} catch (Exception e) {
			e.printStackTrace();
			req.setAttribute("error", "An error occurred while processing your application");
			resp.sendRedirect(req.getContextPath() + "/jobs/view?id=" + req.getParameter("jobId"));
		}
	}
	
	private void handleUpdateApplicationStatus(HttpServletRequest req, HttpServletResponse resp, User user)
	        throws IOException, ServletException {

	    if (!"employer".equals(user.getRole())) {
	        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	        return;
	    }

	    int applicationId = Integer.parseInt(req.getParameter("applicationId"));
	    String newStatus = req.getParameter("status");

	    ApplicationDAO dao = new ApplicationDAO();
	    if (dao.updateApplicationStatus(applicationId, newStatus)) {
	        resp.sendRedirect(req.getContextPath() + "/employer/applications.jsp?success=Status updated");
	    } else {
	        resp.sendRedirect(req.getContextPath() + "/employer/applications.jsp?error=Update failed");
	    }
	}


	private void sendApplicationConfirmation(User jobseeker, Job job) {
		try {
			NotificationService notificationService = new NotificationService();

			// Get employer details for the confirmation message
			UserDAO userDao = new UserDAO();
			User employer = userDao.getUserByID(job.getEmployerId());
			String companyName = employer != null ? employer.getName() : "the company";

			// Prepare confirmation message
			String subject = "Application Confirmation: " + job.getTitle();

			String message = "Dear " + jobseeker.getName() + ",\n\n" + "Thank you for applying for the position of "
					+ job.getTitle() + " at " + companyName + ".\n\n" + "Application Details:\n"
					+ "--------------------------------\n" + "Job Title: " + job.getTitle() + "\n" + "Company: "
					+ companyName + "\n" + "Location: " + job.getLocation() + "\n" + "Salary: " + job.getSalary() + "\n"
					+ "Applied Date: " + new java.util.Date() + "\n\n" + "Next Steps:\n"
					+ "- The employer will review your application\n"
					+ "- You'll be contacted if your profile matches their requirements\n"
					+ "- You can view all your applications in your dashboard\n\n" + "Good luck!\n\n"
					+ "Best regards,\n" + "The Job Portal Team";

			// Send the notification
			notificationService.sendNotification(jobseeker.getEmail(), subject, message);

		} catch (Exception e) {
			// Log the error but don't break the application flow
			System.err.println("Failed to send application confirmation: " + e.getMessage());
			// Consider adding this to an error monitoring system
		}
	}
}
