import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class UserController {
     
    public static String fetchEmployees(){
        try{
            HttpResponse<String> response = SupabaseClient.get("User?role=eq.employee", null);
            if (response.statusCode() >= 200 && response.statusCode() < 300){
                return response.body();
            }else{
                return "Error fetching users: HTTP "+ response.statusCode()+ " - " + response.body();
            }
        }catch (IOException | InterruptedException e){
            return "Error fetching users: " + e.getMessage();
        }

    }

    public static List<String[]> parseEmployeesJson(String jsonResponse) {

        List<String[]> employees = new ArrayList<>();

        try {
            if (jsonResponse == null || jsonResponse.length() < 2) return employees;

            // Remove outer [ ]
            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();

            if (content.isEmpty()) return employees;

            // Split JSON objects safely using brace counting
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

            // Extract only username and role (ignore id or anything else)
            for (String obj : objects) {

                String username = extractValue(obj, "username");
                String role = extractValue(obj, "role");

                employees.add(new String[]{username, role});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return employees;
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

            // Handle string values
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
