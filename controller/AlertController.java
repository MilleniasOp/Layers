package controller;

import java.util.*;

public class AlertController {

    // ================= CREATE ALERT =================
    public static void createAlert(String message) {
        try {
            String jsonBody = String.format(
                "{\"message\":\"%s\",\"is_read\":false}",
                escapeJson(message)
            );

            var response = utils.SupabaseClient.post("Alert", jsonBody, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Alert created successfully.");
            } else {
                System.err.println("Failed: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FETCH =================
    public static String fetchUnreadAlerts() {
        try {
            var response = utils.SupabaseClient.get("Alert?is_read=eq.false", null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return "[]";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    // ================= PARSE (NO JSON LIBRARY) =================
    public static List<Map<String, String>> parseAlerts(String json) {
        List<Map<String, String>> alerts = new ArrayList<>();

        try {
            if (json == null || json.length() < 2) return alerts;

            // remove [ ]
            json = json.trim();
            if (!json.startsWith("[")) return alerts;

            String content = json.substring(1, json.length() - 1);

            if (content.trim().isEmpty()) return alerts;

            // split objects
            List<String> objects = splitObjects(content);

            for (String obj : objects) {
                Map<String, String> alert = new HashMap<>();

                alert.put("id", extractValue(obj, "id"));
                alert.put("message", extractValue(obj, "message"));
                alert.put("created_at", extractValue(obj, "created_at"));

                alerts.add(alert);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return alerts;
    }

    // ================= SPLIT JSON OBJECTS =================
    private static List<String> splitObjects(String content) {
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

        return objects;
    }

    // ================= EXTRACT VALUE =================
    private static String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);

            if (start == -1) return "";

            start += pattern.length();

            // skip spaces
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }

            // string value
            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf('"', start);
                if (end == -1) return "";
                return json.substring(start, end);
            }

            // non-string value
            int end = start;
            while (end < json.length() &&
                    json.charAt(end) != ',' &&
                    json.charAt(end) != '}') {
                end++;
            }

            return json.substring(start, end).trim();

        } catch (Exception e) {
            return "";
        }
    }

    // ================= COUNT =================
    public static int getUnreadCount() {
        try {
            String json = fetchUnreadAlerts();

            List<Map<String, String>> alerts = parseAlerts(json);
            return alerts.size();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ================= MARK ONE =================
    public static void markAlertAsRead(String alertId) {
        try {
            String jsonBody = "{\"is_read\": true}";

            var response = utils.SupabaseClient.patch(
                "Alert?id=eq." + alertId,
                jsonBody,
                null
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Alert marked as read.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MARK ALL =================
    public static void markAllAsRead() {
        try {
            String jsonBody = "{\"is_read\": true}";

            var response = utils.SupabaseClient.patch(
                "Alert?is_read=eq.false",
                jsonBody,
                null
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("All alerts marked as read.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= JSON ESCAPE =================
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}