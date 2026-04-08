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
        OwnerDashBoardUI ownerDashBoardUI = new OwnerDashBoardUI();


        authUI.setAuthSuccessCallback(role -> {
            System.out.println("Authentication successful! Proceeding to the main application...");
            

            if ("director".equals(role)) {
                // Create Owner UI only after successful login
                ownerDashBoardUI.run();
            } else if ("employee".equals(role)) {
                JOptionPane.showMessageDialog(null, "Employee UI not implemented yet");
            } else if ("manager".equals(role)) {
                JOptionPane.showMessageDialog(null, "Manager UI not implemented yet");

            } else {
                JOptionPane.showMessageDialog(null, "Unknown role: " + role);
            }
        });

        authUI.setAuthSuccessCustomerCallback(user -> {
            System.out.println("Customer authentication successful! Proceeding to the customer dashboard...");
            CustomerDashboardUI customerDashboardUI = new CustomerDashboardUI(user);
            customerDashboardUI.run();
        });
        
        authUI.run();
    }
}