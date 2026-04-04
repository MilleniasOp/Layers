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

        authUI.setAuthSuccessCallback(role -> {
            System.out.println("Authentication successful! Proceeding to the main application...");

            if ("director".equals(role)) {
                ownerDashBoardUI.run();
            } else if ("employee".equals(role)) {
                employeeDashBoardUI.run(authUI.getAuthenticatedUsername());
            } else if ("manager".equals(role)) {
                JOptionPane.showMessageDialog(null, "Manager UI not implemented yet");
            } else {
                JOptionPane.showMessageDialog(null, "Unknown role: " + role);
            }
        });
        authUI.run();
    }
}