package controller;

import java.util.ArrayList;
import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.net.http.HttpResponse;

import utils.SupabaseClient;
import entity.Attendance;

public class AttendanceController {

    // ✅ Consistent datetime format
    private static final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ================= CLOCK IN =================
    public void clockIn(String username) {
        try {
            if (hasActiveSession(username)) {
                System.out.println("Employee already clocked in!");
                return;
            }

            String time = LocalDateTime.now().format(formatter);

            String jsonBody = String.format(
                "{\"username\":\"%s\",\"clock_in\":\"%s\"}",
                username, time
            );

            HttpResponse<String> response =
                SupabaseClient.post("attendance", jsonBody, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Clock-in successful.");
            } else {
                System.err.println("Clock-in failed: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CLOCK OUT =================
    public void clockOut(String username) {
        try {
            Attendance att = getActiveSession(username);

            if (att == null) {
                System.out.println("Employee has not clocked in!");
                return;
            }

            LocalDateTime inTime = LocalDateTime.parse(att.getClockIn().replace(" ", "T"));
            LocalDateTime now = LocalDateTime.now();

            // Cap at 8 hours
            LocalDateTime maxOut = inTime.plusHours(8);
            if (now.isAfter(maxOut)) {
                now = maxOut;
            }

            String jsonBody = String.format(
                "{\"clock_out\":\"%s\"}",
                now.format(formatter)
            );

            HttpResponse<String> response =
                SupabaseClient.patch(
                    "attendance?id=eq." + att.getId(),
                    jsonBody,
                    null
                );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Clock-out successful.");
            } else {
                System.err.println("Clock-out failed: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CHECK ACTIVE SESSION =================
    private boolean hasActiveSession(String username) throws Exception {
        HttpResponse<String> response =
            SupabaseClient.get(
                "attendance?username=eq." + username + "&clock_out=is.null",
                null
            );

        return response.body() != null && !response.body().equals("[]");
    }

    // ================= GET ACTIVE SESSION =================
    private Attendance getActiveSession(String username) throws Exception {
        HttpResponse<String> response =
            SupabaseClient.get(
                "attendance?username=eq." + username + "&clock_out=is.null&limit=1",
                null
            );

        String body = response.body();
        if (body == null || body.equals("[]")) return null;

        String id = extractValue(body, "id");
        String user = extractValue(body, "username");
        String clockIn = extractValue(body, "clock_in");

        return new Attendance(id, user, clockIn, null);
    }

    // ================= CALCULATE HOURS =================
    public static String calculateHoursWorked(String clockIn, String clockOut) {
        if (clockIn == null || clockOut == null) return "0";

        try {
            LocalDateTime inTime = LocalDateTime.parse(clockIn.replace(" ", "T"));
            LocalDateTime outTime = LocalDateTime.parse(clockOut.replace(" ", "T"));

            Duration duration = Duration.between(inTime, outTime);

            long minutes = Math.min(duration.toMinutes(), 480);

            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            return hours + " hours " + remainingMinutes + " minutes";

        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    // ================= FETCH =================
    public static String fetchAttendanceByUsername(String username) throws Exception {
        HttpResponse<String> response =
            SupabaseClient.get(
                "attendance?username=eq." + username,
                null
            );

        return response.body();
    }

    // ================= PARSE JSON (FIXED) =================
    public static List<String[]> parseAttendanceJson(String json) {
        List<String[]> attendanceList = new ArrayList<>();

        if (json == null || json.equals("[]")) return attendanceList;

        String content = json.substring(1, json.length() - 1);

        List<String> entries = new ArrayList<>();
        int braceCount = 0, start = -1;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '{') {
                if (braceCount == 0) start = i;
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && start != -1) {
                    entries.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        for (String entry : entries) {
            String id = extractValue(entry, "id");
            String username = extractValue(entry, "username");
            String clockIn = extractValue(entry, "clock_in");
            String clockOut = extractValue(entry, "clock_out");

            attendanceList.add(new String[]{id, username, clockIn, clockOut});
        }

        return attendanceList;
    }

    // ================= EXTRACT VALUE =================
    private static String extractValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int start = json.indexOf(searchKey);

            if (start == -1) return null;

            start += searchKey.length();

            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            }

            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);

            return json.substring(start, end).trim();

        } catch (Exception e) {
            return null;
        }
    }
}