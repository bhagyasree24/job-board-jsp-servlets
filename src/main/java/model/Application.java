package model;

import java.sql.Timestamp;

public class Application {
	
	private int applicationId;
    private int jobId;
    private int jobseekerId;
    private Timestamp appliedDate;
    private String status;
    private String resumePath;
    
    
    public Application() {
    	
    }

	public Application(int applicationId, int jobId, int jobseekerId) {
		super();
		this.applicationId = applicationId;
		this.jobId = jobId;
		this.jobseekerId = jobseekerId;
		
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public int getJobseekerId() {
		return jobseekerId;
	}

	public void setJobseekerId(int jobseekerId) {
		this.jobseekerId = jobseekerId;
	}

	public Timestamp getAppliedDate() {
		return appliedDate;
	}

	public void setAppliedDate(Timestamp appliedDate) {
		this.appliedDate = appliedDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getResumePath() {
		return resumePath;
	}

	public void setResumePath(String resumePath) {
		this.resumePath = resumePath;
	}
    

}
