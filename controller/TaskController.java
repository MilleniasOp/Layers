package controller;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import entity.Employee;
import entity.Task;
import entity.User;
import utils.SupabaseClient;
import utils.UIUtils;

import java.awt.*;

public class TaskController {

    public static void addNewTask(Task task) {
        saveTask(task);
    }

    private static void saveTask(Task task) {
        try {
            String json = String.format(
                    "{\"taskId\":\"%s\",\"description\":\"%s\",\"assignedTo\":\"%s\",\"status\":\"%s\",\"createdDate\":\"%s\"}",
                    task.getTaskId(),
                    task.getDescription(),
                    task.getAssignedTo(),
                    task.getStatus(),
                    task.getCreatedDate().toString());

            HttpResponse<String> response = SupabaseClient.Tables.TASKS_TABLE.post(json, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Task saved successfully.");
            } else {
                System.err.println("Failed: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String fetchTasks() {
        try {
            HttpResponse<String> response = SupabaseClient.Tables.TASKS_TABLE.get("", null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return "Error: " + response.body();
            }

        } catch (IOException | InterruptedException e) {
            return e.getMessage();
        }
    }

    public static void updateTaskAssignment(String taskId, String assignedTo) {
        try {
            String json = String.format(
                    "{\"assignedTo\":\"%s\"}",
                    assignedTo);

            String path = "?taskId=eq." + taskId;

            HttpResponse<String> response = SupabaseClient.Tables.TASKS_TABLE.patch(path, json, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Task updated.");
            } else {
                System.err.println("Update failed: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TASK PARSER =================
    public static List<String[]> parseTasksJson(String jsonResponse) {

        List<String[]> tasks = new ArrayList<>();

        try {
            if (jsonResponse == null || jsonResponse.length() < 2)
                return tasks;

            // Remove outer [ ]
            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();

            if (content.isEmpty())
                return tasks;

            // Split objects safely
            List<String> objects = new ArrayList<>();
            int braceCount = 0;
            int start = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '{') {
                    if (braceCount == 0)
                        start = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        objects.add(content.substring(start, i + 1));
                    }
                }
            }

            // Extract fields from each object
            for (String obj : objects) {

                String taskId = extractValue(obj, "taskId");
                String description = extractValue(obj, "description");
                String assignedTo = extractValue(obj, "assignedTo");
                String status = extractValue(obj, "status");
                String createdDate = extractValue(obj, "createdDate");

                tasks.add(new String[] {
                        taskId,
                        description,
                        assignedTo,
                        status,
                        createdDate
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tasks;
    }

    private static String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);

            if (start == -1)
                return "";

            start += pattern.length();

            // Skip whitespace
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }

            // If value is a string
            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf('"', start);
                return json.substring(start, end);
            } else {
                // If value is not quoted (just in case)
                int end = start;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                    end++;
                }
                return json.substring(start, end).trim();
            }

        } catch (Exception e) {
            return "";
        }
    }

    // ================= ADD TASK DIALOG =================
    public static void showAddTaskDialog() {

        JTextField descriptionField = new JTextField(20);
        JTextField assignedToField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Task Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Assign To (optional):"));
        panel.add(assignedToField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Add New Task",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String description = descriptionField.getText().trim();
            String assignedTo = assignedToField.getText().trim();

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Description cannot be empty!");
                return;
            }

            Task task;

            if (assignedTo.isEmpty()) {
                task = new Task(description);
            } else {
                task = new Task(description, assignedTo);
            }

            addNewTask(task);

            JOptionPane.showMessageDialog(null, "Task added successfully!");
        }
    }

    // ================= ASSIGN TASK DIALOG =================
    public static void showAssignTaskDialog(List<String[]> tasks) {

        if (tasks == null || tasks.isEmpty()) {
            UIUtils.showMessage(null, "Error", "No tasks available.");
            return;
        }

        String[] taskOptions = new String[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            taskOptions[i] = tasks.get(i)[0] + " - " + tasks.get(i)[1];
        }

        JComboBox<String> taskDropdown = new JComboBox<>(taskOptions);
        JTextField assignedToField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Select Task:"));
        panel.add(taskDropdown);
        panel.add(new JLabel("Assign To:"));
        panel.add(assignedToField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Assign Task",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedIndex = taskDropdown.getSelectedIndex();
            String taskId = tasks.get(selectedIndex)[0];
            String assignedTo = assignedToField.getText().trim();

            if (assignedTo.isEmpty()) {
                UIUtils.showMessage(null, "Error", "Please enter a user.");
                return;
            }

            updateTaskAssignment(taskId, assignedTo);
            UIUtils.showMessage(null, "Success", "Task assigned successfully!");
        }
    }

    public List<Task> viewTasks(Employee employee) {
        System.out.println("Viewing tasks assigned to employee: " + employee.getUsername());
        List<Task> tasks = getTasksFromSupabase(employee);
        for (Task task : tasks) {
            System.out.println(task);
        }
        return tasks;
    }

    public void updateTask(Task task, Employee employee, String newStatus) {
        if (task.getAssignedTo().equals(employee.getUsername())) {
            task.setStatus(newStatus);
            updateTaskStatus(task.getTaskId(), newStatus);
        } else {
            System.out.println("Cannot update task: not assigned to you.");
        }
    }

    public static void updateTaskStatus(String task_id, String newStatus) {
        try {
            // Create JSON body for the update
            String jsonBody = String.format(
                    "{\"status\":\"%s\"}",
                    escapeJson(newStatus));

            // PATCH to Task table with taskId filter
            String path = "Task?taskId=eq." + task_id;
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

    private static String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static List<Task> getTasksFromSupabase(User user) {
        List<Task> tasks = new ArrayList<>();
        try {
            // GET tasks assigned to this employee using filter
            String path = "Task?assignedTo=eq." + user.getUsername();
            HttpResponse<String> response = SupabaseClient.get(path, null);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("Failed to retrieve tasks from Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return tasks;
            }
            System.out.println("Tasks retrieved from Supabase successfully.");
            String responseBody = response.body();
            // Assuming response is a JSON array like [{"taskId":"...", ...}, ...]
            if (!(responseBody.startsWith("[") && responseBody.endsWith("]"))) {
                return tasks;
            }
            String content = responseBody.substring(1, responseBody.length() - 1); // remove [ ]

            if (content.trim().isEmpty()) {
                return tasks;
            }
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
        } catch (IOException | InterruptedException e) {
            System.err.println("Error retrieving tasks from Supabase: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    private static Task parseTaskFromJson(String json) {
        try {
            // Simple JSON parsing for
            // {"taskId":"...","description":"...","assignedTo":"...","status":"...","createdDate":"..."}
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

    private static String extractJsonValue(String json, String key) {
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