package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Job;

public class JobDAO {
	
	public Boolean postJob(Job job) {
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO JOBS (employer_id, title, description, category, salary, location) VALUES (?, ?, ?, ?, ?, ?)",Statement.RETURN_GENERATED_KEYS)){
			stmt.setInt(1, job.getEmployerId());
			stmt.setString(2, job.getTitle());
            stmt.setString(3, job.getDescription());
            stmt.setString(4, job.getCategory());
            stmt.setString(5, job.getSalary());
            stmt.setString(6, job.getLocation());
            
            int affectedRows = stmt.executeUpdate();
            if(affectedRows>0) {
            	try(ResultSet generatedKeys = stmt.getGeneratedKeys()){
            		if(generatedKeys.next()) {
            			job.setJobId(generatedKeys.getInt(1));
            		}
            		
            	}
            	return true;
            }
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		
		}
		return false;
	}
	
	public Job getJobById(int jobId) {
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM JOBS WHERE JOB_ID=?")){
			stmt.setInt(1, jobId);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				return extractJobFromResultset(rs);
			}
			
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Job> getAllActiveJobs() {
		List<Job> jobs = new ArrayList<>();
		try(Connection conn = DBConnection.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM JOBS WHERE STATUS='active' ORDER BY posted_date DESC")){
			
			while (rs.next()) {
				jobs.add(extractJobFromResultset(rs));
				
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return jobs;
	}
	public List<Job> getJobByEmployer(int employerId){
		List<Job> jobs = new ArrayList<>();
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM JOBS WHERE employer_id=? ORDER BY posted_date DESC")){
			
			stmt.setInt(1, employerId);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				jobs.add(extractJobFromResultset(rs));
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return jobs;
	}
	
	public Boolean updateJob(Job job) {
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement("UPDATE jobs SET title = ?, description = ?, category = ?, salary = ?, location = ?, status = ? WHERE job_id = ?")) {
			
			stmt.setString(1, job.getTitle());
			stmt.setString(2, job.getDescription());
            stmt.setString(3, job.getCategory());
            stmt.setString(4, job.getSalary());
            stmt.setString(5, job.getLocation());
            stmt.setString(6, job.getStatus());
            stmt.setInt(7, job.getJobId());
            
            return stmt.executeUpdate() >0;
 			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean deleteJob(int jobId) {
		try (Connection conn = DBConnection.getConnection();
	             PreparedStatement stmt = conn.prepareStatement("UPDATE jobs SET status = 'inactive'WHERE job_id=?")) {
	            
	            stmt.setInt(1, jobId);
	            return stmt.executeUpdate() > 0;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	}
	
	public List<Job> searchJob(String keyword) {
		
		List<Job> jobs = new ArrayList<>();
		String searchTerm = '%'+keyword+'%';
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM JOBS WHERE title LIKE ? OR description LIKE ? OR category LIKE ? OR location LIKE ?")) {
			
			stmt.setString(1, searchTerm);
			stmt.setString(2, searchTerm);
			stmt.setString(3, searchTerm);
			stmt.setString(4, searchTerm);
			
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				jobs.add(extractJobFromResultset(rs));
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return jobs;
		
	}
	
	public int countJobsByEmployer(int employerId) {
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM JOBS WHERE employer_id=?")) {
			stmt.setInt(1, employerId);
			ResultSet rs =stmt.executeQuery();
			while(rs.next()) {
				return rs.getInt(1);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return 0;
	}
	
	public int countAllActiveJobs() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM JOBS WHERE status='active'")) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
	
	public List<Job> getRecentJobs(int limit){
		List<Job> jobs = new ArrayList<>();
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM JOBS WHERE status='active' ORDER BY posted_date DESC LIMIT ?")) {
			
			stmt.setInt(1, limit);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				jobs.add(extractJobFromResultset(rs));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return jobs;
	}
	
	public Job extractJobFromResultset(ResultSet rs) throws SQLException {
		Job job = new Job();
		job.setJobId(rs.getInt("job_id"));
		job.setEmployerId(rs.getInt("employer_id"));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setCategory(rs.getString("category"));
        job.setSalary(rs.getString("salary"));
        job.setLocation(rs.getString("location"));
        job.setPostedDate(rs.getTimestamp("posted_date"));
        job.setStatus(rs.getString("status"));
        return job;
	}
	
	public List<Job> getJobsByCategory(String category) {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE status = 'active' AND category = ? ORDER BY posted_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                jobs.add(extractJobFromResultset(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }
	
	public List<Job> getJobsByLocation(String location) {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE status = 'active' AND location LIKE ? ORDER BY posted_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + location + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                jobs.add(extractJobFromResultset(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }
	
	public List<Job> getJobsBySalaryRange(String minSalary, String maxSalary) {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE status = 'active' AND " +
                     "REPLACE(REPLACE(salary, '$', ''), ',', '') BETWEEN ? AND ? " +
                     "ORDER BY posted_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, minSalary);
            stmt.setString(2, maxSalary);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                jobs.add(extractJobFromResultset(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }
	

    public String getJobStatus(int jobId) {
        String sql = "SELECT status FROM jobs WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, jobId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("status");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateJobStatus(int jobId, String status) {
        String sql = "UPDATE jobs SET status = ? WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, jobId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Job> getAllJobs(String statusFilter) {
        List<Job> jobs = new ArrayList<>();
        String baseSql = "SELECT * FROM jobs";
        String sql = baseSql + (statusFilter != null ? " WHERE status = ?" : "");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (statusFilter != null) {
                stmt.setString(1, statusFilter);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                jobs.add(extractJobFromResultset(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }



}
