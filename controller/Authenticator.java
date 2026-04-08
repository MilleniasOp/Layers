package controller;

import entity.User;
import utils.SupabaseClient;

import java.io.IOException;
import java.net.http.HttpResponse;

public class Authenticator {
    
    public boolean authenticate(String username, String password) {
        try {
            User user = getUser(username, password);
            return user != null;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public User getUser(String username, String password) throws IOException, InterruptedException {
        HttpResponse<String> response = SupabaseClient.get(
                "users?username=eq." + username
                + "&password=eq." + password
                + "&select=username,password,role,userId"
                + "&limit=1",
                null);
        
        String body = response.body();
        if (body == null || body.equals("[]") || body.isBlank()) {
            return null;
        }
        
        String role = extractValue(body, "role");
        String userId = extractValue(body, "userId");
        
        return new User(username, password, role, userId);
    }
    
    public String getUserRole(String username) {
        try {
            HttpResponse<String> response = SupabaseClient.get(
                    "users?username=eq." + username + "&select=role&limit=1",
                    null);
            
            String body = response.body();
            if (body == null || body.equals("[]") || body.isBlank()) {
                return null;
            }
            
            return extractValue(body, "role");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern);
            if (start == -1) return "";
            start += pattern.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
}