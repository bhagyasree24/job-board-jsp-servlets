package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Application;

public class ApplicationDAO {
    // SQL Queries
    private static final String INSERT_APPLICATION = 
        "INSERT INTO applications (job_id, jobseeker_id, status,resume_path) VALUES (?, ?, ,?, ?)";
    private static final String CHECK_APPLICATION = 
        "SELECT COUNT(*) FROM applications WHERE jobseeker_id = ? AND job_id = ?";
    private static final String SELECT_BY_JOB = 
        "SELECT * FROM applications WHERE job_id = ?";
    private static final String SELECT_BY_EMPLOYER = 
        "SELECT a.* FROM applications a JOIN jobs j ON a.job_id = j.job_id WHERE j.employer_id = ?";
    private static final String SELECT_BY_JOBSEEKER = 
        "SELECT * FROM applications WHERE jobseeker_id = ?";
    private static final String UPDATE_STATUS = 
        "UPDATE applications SET status = ? WHERE application_id = ?";

    public boolean applyForJob(Application application) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_APPLICATION)) {
            
            stmt.setInt(1, application.getJobId());
            stmt.setInt(2, application.getJobseekerId());
            stmt.setString(3, application.getStatus());
            stmt.setString(4, application.getResumePath());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAlreadyApplied(int jobseekerId, int jobId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CHECK_APPLICATION)) {
            
            stmt.setInt(1, jobseekerId);
            stmt.setInt(2, jobId);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Application> getApplicationsByJob(int jobId) {
        List<Application> applications = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_JOB)) {
            
            stmt.setInt(1, jobId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                applications.add(extractApplicationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    public List<Application> getApplicationsByEmployer(int employerId) {
        List<Application> applications = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYER)) {
            
            stmt.setInt(1, employerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                applications.add(extractApplicationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    public List<Application> getApplicationsByJobseeker(int jobseekerId) {
        List<Application> applications = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_JOBSEEKER)) {
            
            stmt.setInt(1, jobseekerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                applications.add(extractApplicationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    public boolean updateApplicationStatus(int applicationId, String newStatus) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, applicationId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Application extractApplicationFromResultSet(ResultSet rs) throws SQLException {
        Application application = new Application();
        application.setApplicationId(rs.getInt("application_id"));
        application.setJobId(rs.getInt("job_id"));
        application.setJobseekerId(rs.getInt("jobseeker_id"));
        application.setAppliedDate(rs.getTimestamp("applied_date"));
        application.setStatus(rs.getString("status"));
        application.setResumePath(rs.getString("resume_path"));

        return application;
    }
}