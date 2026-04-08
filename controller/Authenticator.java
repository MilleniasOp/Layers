
package controller;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import entity.User;
import utils.SupabaseClient;

import java.util.regex.Matcher;

public class Authenticator {
    
    public String fetchUsers() {
        try {
            // Fetch all users from the 'User' table in Supabase
            HttpResponse<String> response = SupabaseClient.Tables.USERS_TABLE.get("", null);

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Parse the JSON array manually
                List<User> users = parseUsersFromJson(responseBody);

                StringBuilder result = new StringBuilder();
                result.append("All Users Information:\n");
                result.append("======================\n");

                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    result.append("User ").append(i + 1).append(":\n");
                    result.append("  Username: ").append(user.getUsername()).append("\n");
                    result.append("  Password: ").append(user.getPassword()).append("\n");
                    result.append("  Role: ").append(user.getRole()).append("\n");
                    result.append("\n");
                }

                return result.toString();
            } else {
                return "Error fetching users: HTTP " + response.statusCode() + " - " + response.body();
            }
        } catch (IOException | InterruptedException e) {
            return "Error fetching users: " + e.getMessage();
        } catch (Exception e) {
            return "Error parsing users data: " + e.getMessage();
        }
    }

    private List<User> parseUsersFromJson(String json) {
        List<User> users = new ArrayList<>();

        // Simple JSON array parser for objects like [{"username":"value","password":"value","role":"value"},...]
        Pattern objectPattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher objectMatcher = objectPattern.matcher(json);

        while (objectMatcher.find()) {
            String objectContent = objectMatcher.group(1);

            String username = extractStringValue(objectContent, "username");
            String password = extractStringValue(objectContent, "password");
            String role = extractStringValue(objectContent, "role");

            users.add(new User(username, password, role));
        }

        return users;
    }

    private String extractStringValue(String jsonObject, String key) {
        // Pattern to match "key":"value"
        Pattern pattern = Pattern.compile("\"" + key + "\":\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(jsonObject);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public boolean authenticate(String username, String password) {
        try {
            // Fetch all users from the 'User' table in Supabase
            HttpResponse<String> response = SupabaseClient.Tables.USERS_TABLE.get("", null);

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Parse the JSON array manually
                List<User> users = parseUsersFromJson(responseBody);

                for (User user : users) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        return true; // Authentication successful
                    }
                }
                return false; // No matching user found
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during authentication: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing users data during authentication: " + e.getMessage());
        }
        return false; 
    } 

    public String getUserRole(String username) {
        try {
            // Fetch all users from the 'User' table in Supabase
            HttpResponse<String> response = SupabaseClient.Tables.USERS_TABLE.get("", null);

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Parse the JSON array manually
                List<User> users = parseUsersFromJson(responseBody);

                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        return user.getRole(); // Return the role of the user
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error fetching user role: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing users data while fetching role: " + e.getMessage());
        }
        return ""; // Return empty string if user not found or error occurs
    }


}
