import boundary.pages.AuthenticatorUI;
import boundary.pages.EmployeeDashBoardUI;
import boundary.pages.OwnerDashBoardUI;
import boundary.pages.EmployeeDashBoardUI;
import utils.SupabaseClient;

import java.io.IOException;

import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        AuthenticatorUI authUI = new AuthenticatorUI();

        authUI.setAuthSuccessCallback(user -> {
            System.out.println("Authentication successful! Proceeding to the main application...");

            if ("director".equals(user.getRole())) {
                OwnerDashBoardUI ownerDashBoardUI = new OwnerDashBoardUI();
                ownerDashBoardUI.run();
            } else if ("employee".equals(user.getRole())) {
                EmployeeDashBoardUI employeeDashBoardUI = new EmployeeDashBoardUI();
                employeeDashBoardUI.run(user);
            } else if ("manager".equals(user.getRole())) {
                JOptionPane.showMessageDialog(null, "Manager UI not implemented yet");
            } else {
                JOptionPane.showMessageDialog(null, "Unknown role: " + user.getRole());
            }
        });
        authUI.run();
    }
}