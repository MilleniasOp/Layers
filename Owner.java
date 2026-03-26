import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.http.HttpResponse;

public class Owner extends User{
    public Owner(String username, String password) {
        super(username, password, "director");
        saveToSupabase(username, password);
    }

    /**
     * Saves the owner to Supabase User table
     * @param username the owner's username
     * @param password the owner's password
     */
    private void saveToSupabase(String username, String password) {
        try {
            // Create JSON body for the insert
            String jsonBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"director\"}",
                escapeJson(username),
                escapeJson(password)
            );

            // POST to User table
            HttpResponse<String> response = SupabaseClient.post("User", jsonBody, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Owner saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save owner to Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving owner to Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Escapes special characters in JSON strings
     * @param s the string to escape
     * @return the escaped string
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}