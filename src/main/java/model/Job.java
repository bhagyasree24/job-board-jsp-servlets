package model;

import java.sql.Timestamp;

public class Job {
	
	private int jobId;
    private int employerId;
    private String title;
    private String description;
    private String category;
    private String salary;
    private String location;
    private Timestamp postedDate;
    private String status;
    
public Job() {}
    
    public Job(int employerId, String title, String description, String category, 
               String salary, String location) {
        this.employerId = employerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.salary = salary;
        this.location = location;
    }

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public int getEmployerId() {
		return employerId;
	}

	public void setEmployerId(int employerId) {
		this.employerId = employerId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Timestamp getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(Timestamp postedDate) {
		this.postedDate = postedDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    

}
