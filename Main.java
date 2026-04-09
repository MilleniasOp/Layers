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
            System.out.println("Owner authentication successful! Proceeding to the owner dashboard...");
            ownerDashBoardUI.run(authUI);
        });

        authUI.setAuthSuccessEmployeeCallback(user -> {
            System.out.println("Employee authentication successful! Proceeding to the employee dashboard...");
            employeeDashBoardUI.run(user, authUI);
        });

        authUI.setAuthSuccessCustomerCallback(user -> {
            System.out.println("Customer authentication successful! Proceeding to the customer dashboard...");
            CustomerDashboardUI customerDashboardUI = new CustomerDashboardUI(user, authUI);
            customerDashboardUI.run();
        });
        
        authUI.run();
    }
}