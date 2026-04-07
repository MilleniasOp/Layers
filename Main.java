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
        //fix this idk how to get the user info from the authenticator 
        CustomerDashboardUI customerDashboardUI = new CustomerDashboardUI((
            new User("john_doe", "password123", "customer", "cust_001")));

        authUI.setAuthSuccessCallback(role -> {
            System.out.println("Authentication successful! Proceeding to the main application...");

            if ("director".equals(role)) {
                ownerDashBoardUI.run();
            } else if ("employee".equals(role)) {
                JOptionPane.showMessageDialog(null, "Employee UI not implemented yet");
            } else if ("manager".equals(role)) {
                JOptionPane.showMessageDialog(null, "Manager UI not implemented yet");
            } else if ("customer".equals(role)) {
                customerDashboardUI.run();
            } else {
                JOptionPane.showMessageDialog(null, "Unknown role: " + role);
            }
        });
        authUI.run();
    }
}