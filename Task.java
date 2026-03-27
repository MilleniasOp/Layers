import java.time.LocalDateTime;
import java.util.UUID;


public class Task {
    private String taskId;
    private String description;
    private String assignedTo;
    private String status;
    private LocalDateTime createdDate;

    /**
     * Default constructor
     */
    public Task() {
        this.taskId = generateTaskId();
        this.description = "";
        this.assignedTo = "";
        this.status = "Pending";
        this.createdDate = LocalDateTime.now();
    }

    /**
     * Constructor with description
     * @param description the task description
     */
    public Task(String description) {
        this();
        this.description = description;
        // defer saving until we know if an assignee will be set
    }

    /**
     * Constructor with description and assignee
     * @param description the task description
     * @param assignedTo the person assigned to the task
     */
    public Task(String description, String assignedTo) {
        this();
        this.description = description;
        this.assignedTo = assignedTo;
    }

    private String generateTaskId() {
        return "TASK-" + UUID.randomUUID().toString();
    }
    // Getters
    public String getTaskId() { return taskId; }
    public String getDescription() { return description; }
    public String getAssignedTo() { return assignedTo; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedDate() { return createdDate; }

    // Setters
    public void setDescription(String description) { this.description = description; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public void setStatus(String status) { this.status = status; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

}