package controller;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import entity.Payroll;

public class FinanceController {

    public static void calculatePayroll() {

        try {
            // Step 1: Fetch employees
            String employeeJson = UserController.fetchEmployees();
            List<String[]> employees = UserController.parseEmployeesJson(employeeJson);

            List<Payroll> payrollList = new ArrayList<>();

            // Step 2: Loop employees
            for (String[] emp : employees) {

                String username = emp[0];

                // Step 3: Get tasks
                String taskJson = TaskController.fetchTasks();
                List<String[]> tasks = TaskController.parseTasksJson(taskJson);

                int completedTasks = 0;

                for (String[] task : tasks) {
                    String assignedTo = task[2];
                    String status = task[3];

                    if (username.equals(assignedTo) && status.equalsIgnoreCase("Completed")) {
                        completedTasks++;
                    }
                }

                // Step 4: Calculate pay
                double payPerTask = 500; // you can change this
                double totalPay = completedTasks * payPerTask;

                payrollList.add(new Payroll(username, completedTasks, totalPay));
            }

            // Step 5: Save to file
            savePayrollToFile(payrollList);

            System.out.println("Payroll calculated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void savePayrollToFile(List<Payroll> payrollList) {
        try{

        }catch (Exception e) {
            
        }
    }
}