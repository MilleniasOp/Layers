import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;


public class Employee extends User {
    public Employee(String username, String password) {
        super(username, password, "employee");
    }

    public List<Task> viewTasks() {
        System.out.println("Viewing tasks assigned to employee: " + getUsername());
        List<Task> tasks = getTasksFromSupabase();
        for (Task task : tasks) {
            System.out.println(task);
        }
        return tasks;
    }

    public void updateTask(Task task, String newStatus) {
        if (task.getAssignedTo().equals(getUsername())) {
            task.setStatus(newStatus);
            updateTaskStatus(task);
        } else {
            System.out.println("Cannot update task: not assigned to you.");
        }
    }

    private void updateTaskStatus(Task task) {
        try {
            // Create JSON body for the update
            String jsonBody = String.format(
                "{\"status\":\"%s\"}",
                escapeJson(task.getStatus())
            );

            // PATCH to Task table with taskId filter
            String path = "Task?taskId=eq." + task.getTaskId();
            HttpResponse<String> response = SupabaseClient.patch(path, jsonBody, null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Task status updated in Supabase successfully.");
            } else {
                System.err.println("Failed to update task status in Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error updating task status in Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private List<Task> getTasksFromSupabase() {
        List<Task> tasks = new ArrayList<>();
        try {
            // GET tasks assigned to this employee using filter
            String path = "Task?assignedTo=eq." + getUsername();
            HttpResponse<String> response = SupabaseClient.get(path, null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Tasks retrieved from Supabase successfully."); 
                String responseBody = response.body();
                // Assuming response is a JSON array like [{"taskId":"...", ...}, ...]
                if (responseBody.startsWith("[") && responseBody.endsWith("]")) {
                    String content = responseBody.substring(1, responseBody.length() - 1); // remove [ ]
                    if (!content.trim().isEmpty()) {
                        String[] taskStrings = content.split("},");
                        for (int i = 0; i < taskStrings.length; i++) {
                            String taskStr = taskStrings[i];
                            if (i < taskStrings.length - 1) {
                                taskStr += "}"; // add back the } that was removed by split
                            }
                            Task task = parseTaskFromJson(taskStr.trim());
                            if (task != null) {
                                tasks.add(task);
                            }
                        }
                    }
                }
            } else {
                System.err.println("Failed to retrieve tasks from Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error retrieving tasks from Supabase: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    private Task parseTaskFromJson(String json) {
        try {
            // Simple JSON parsing for {"taskId":"...","description":"...","assignedTo":"...","status":"...","createdDate":"..."}
            String taskId = extractJsonValue(json, "taskId");
            String description = extractJsonValue(json, "description");
            String assignedTo = extractJsonValue(json, "assignedTo");
            String status = extractJsonValue(json, "status");
            String createdDateStr = extractJsonValue(json, "createdDate");
            
            if (taskId != null && description != null && assignedTo != null && status != null) {
                Task task = new Task();
                task.setTaskId(taskId);
                task.setDescription(description);
                task.setAssignedTo(assignedTo);
                task.setStatus(status);
                // Note: createdDate is not settable, skipping for now
                return task;
            }
        } catch (Exception e) {
            System.err.println("Error parsing task JSON: " + json);
            e.printStackTrace();
        }
        return null;
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start != -1) {
            start += search.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                return json.substring(start, end);
            }
        }
        return null;
    }

}