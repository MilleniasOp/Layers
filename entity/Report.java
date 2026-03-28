package entity;
import java.time.LocalDateTime;

public class Report{
    public String ReportID;
    private String taskId;
    private String description;
    private String assignedTo;
    private String status;
    private LocalDateTime createdDate;    

    public Report(Task task){
        this.ReportID = generateReportID();
        this.taskId = task.getTaskId();
        this.description = task.getDescription();
        this.assignedTo = task.getAssignedTo();
        this.status = task.getStatus();
        this.createdDate = task.getCreatedDate();
    }
    private static int idCounter = 0;
    private synchronized String generateReportID() {
        idCounter++;
        return "Report-" + idCounter;
    }
    public String getReportID(){return ReportID;};

    public void setReportID(String ReportID){this.ReportID = ReportID;};
    public String getTaskId() { return taskId; }
    public String getDescription() { return description; }
    public String getAssignedTo() { return assignedTo; }
    public String getStatus() { return status; }
    public String getCreatedDate() { return createdDate.toString(); }

    @Override
    public String toString() {
        return "ReportID=" + ReportID + ", TaskID=" + taskId +", Description=" + description + ", AssignedTo=" + assignedTo +", Status=" + status +", CreatedDate=" + createdDate;
}
    
}
