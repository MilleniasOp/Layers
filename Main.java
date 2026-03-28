import boundary.pages.AuthenticatorUI;
import boundary.pages.OwnerDashBoardUI;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {
        AuthenticatorUI authUI = new AuthenticatorUI();
        OwnerDashBoardUI ownerDashBoardUI = new OwnerDashBoardUI();

        authUI.setAuthSuccessCallback(role -> {
            System.out.println("Authentication successful! Proceeding to the main application...");

            if ("director".equals(role)) {
                ownerDashBoardUI.run();
            } else if ("employee".equals(role)) {
                JOptionPane.showMessageDialog(null, "Employee UI not implemented yet");
            } else if ("manager".equals(role)) {
                JOptionPane.showMessageDialog(null, "Manager UI not implemented yet");
            } else {
                JOptionPane.showMessageDialog(null, "Unknown role: " + role);
            }
        });
        authUI.run();
    }
}