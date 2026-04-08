import boundary.pages.AuthenticatorUI;
import boundary.pages.CustomerDashboardUI;
import boundary.pages.OwnerDashBoardUI;
import entity.User;
import utils.SupabaseClient;

import java.io.IOException;

import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        AuthenticatorUI authUI = new AuthenticatorUI();

        authUI.setAuthSuccessCallback(user -> {
            System.out.println("Authentication successful! Welcome " + user.getUsername());
            
            String role = user.getRole();

            if ("director".equals(role)) {
                // Create Owner UI only after successful login
                OwnerDashBoardUI ownerDashBoardUI = new OwnerDashBoardUI();
                ownerDashBoardUI.run();
            } else if ("employee".equals(role)) {
                JOptionPane.showMessageDialog(null, "Employee UI not implemented yet");
            } else if ("manager".equals(role)) {
                JOptionPane.showMessageDialog(null, "Manager UI not implemented yet");
            } else if ("customer".equals(role)) {
                // Create Customer UI with the actual logged-in user
                CustomerDashboardUI customerDashboardUI = new CustomerDashboardUI(user);
                customerDashboardUI.run();
            } else {
                JOptionPane.showMessageDialog(null, "Unknown role: " + role);
            }
        });
        
        authUI.run();
    }
}