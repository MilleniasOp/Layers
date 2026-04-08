import boundary.pages.AuthenticatorUI;
import boundary.pages.EmployeeDashBoardUI;
import boundary.pages.OwnerDashBoardUI;
import boundary.pages.CustomerDashboardUI;
import entity.User;
import utils.SupabaseClient;

import java.io.IOException;

import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        AuthenticatorUI authUI = new AuthenticatorUI();
        OwnerDashBoardUI ownerDashBoardUI = new OwnerDashBoardUI();
        EmployeeDashBoardUI employeeDashBoardUI = new EmployeeDashBoardUI();

        authUI.setAuthSuccessCallback(user -> {

        authUI.setAuthSuccessCallback(role -> {
            System.out.println("Authentication successful! Proceeding to the main application...");
            

            if ("director".equals(user.getRole())) {
            if ("director".equals(role)) {
                // Create Owner UI only after successful login
                ownerDashBoardUI.run();
            } else if ("employee".equals(user.getRole())) {
                employeeDashBoardUI.run(user);
            } else if ("manager".equals(user.getRole())) {
                JOptionPane.showMessageDialog(null, "Manager UI not implemented yet");

            } else {
                JOptionPane.showMessageDialog(null, "Unknown role: " + user.getRole());
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