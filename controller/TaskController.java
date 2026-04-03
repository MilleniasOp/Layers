package controller;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import entity.Task;
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
                task.getCreatedDate().toString()
            );

            HttpResponse<String> response =
                SupabaseClient.Tables.TASKS_TABLE.post(json, null);

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
            HttpResponse<String> response =
                SupabaseClient.Tables.TASKS_TABLE.get("", null);

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
                assignedTo
            );

            String path = "?taskId=eq." + taskId;

            HttpResponse<String> response =
                SupabaseClient.Tables.TASKS_TABLE.patch(path, json, null);

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
            if (jsonResponse == null || jsonResponse.length() < 2) return tasks;

            // Remove outer [ ]
            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();

            if (content.isEmpty()) return tasks;

            // Split objects safely
            List<String> objects = new ArrayList<>();
            int braceCount = 0;
            int start = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '{') {
                    if (braceCount == 0) start = i;
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

                tasks.add(new String[]{
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

            if (start == -1) return "";

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
                JOptionPane.PLAIN_MESSAGE
        );

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
                JOptionPane.PLAIN_MESSAGE
        );

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
}