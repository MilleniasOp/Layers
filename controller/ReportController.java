package controller;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import entity.Report;
import entity.Task;
import utils.SupabaseClient;

import java.awt.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ReportController {

    public static List<Report> generateReports(String username) {

        List<Report> reportsList = new ArrayList<>();

        try {
            String path = "?assignedTo=eq." + username;

            HttpResponse<String> response = SupabaseClient.Tables.TASKS_TABLE.get(path, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {

                String json = response.body();

                List<Task> tasks = parseTasksToObjects(json);


                for (Task task : tasks) {
                    Report report = new Report(task);
                    reportsList.add(report);
                }

            } else {
                System.err.println("Failed to fetch tasks: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return reportsList;
    }



    // ================= PARSER =================
    private static List<Task> parseTasksToObjects(String jsonResponse) {

        List<Task> taskList = new ArrayList<>();

        try {
            if (jsonResponse == null || jsonResponse.length() < 2) return taskList;

            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();
            if (content.isEmpty()) return taskList;

            // SAFE splitting
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

            // Convert each object into Task
            for (String obj : objects) {

                String taskId = extractValue(obj, "taskId");
                String description = extractValue(obj, "description");
                String assignedTo = extractValue(obj, "assignedTo");
                String status = extractValue(obj, "status");
                String createdDate = extractValue(obj, "createdDate");

                Task task;

                if (assignedTo == null || assignedTo.isEmpty()) {
                    task = new Task(description);
                } else {
                    task = new Task(description, assignedTo);
                }
                task.setTaskId(taskId);
                task.setStatus(status);

                taskList.add(task);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return taskList;
    }

    private static String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);

            if (start == -1) return "";

            start += pattern.length();

            while (Character.isWhitespace(json.charAt(start))) start++;

            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf('"', start);
                return json.substring(start, end);
            }

            return "";

        } catch (Exception e) {
            return "";
        }
    }


}