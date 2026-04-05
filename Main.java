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
        OwnerDashBoardUI ownerDashBoardUI = new OwnerDashBoardUI();
        EmployeeDashBoardUI employeeDashBoardUI = new EmployeeDashBoardUI();

        authUI.setAuthSuccessCallback(user -> {
            System.out.println("Authentication successful! Proceeding to the main application...");

            if ("director".equals(user.getRole())) {
                ownerDashBoardUI.run();
            } else if ("employee".equals(user.getRole())) {
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