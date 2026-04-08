package controller;

import java.util.ArrayList;
import java.util.List;
import java.net.http.HttpResponse;

import utils.SupabaseClient;
import entity.Payroll;

public class FinanceController {

    public static String getToday_Period() {
        return java.time.LocalDate.now().getMonth().toString() + " " + java.time.LocalDate.now().getYear();
    }

    public static void calculatePayroll() {
        //find all the completed tasks in the payroll period, eg. 1st to 30th of the month
        //find all the attendance records in the payroll period
        //id based on the employee and period, calculate the total pay based on the number of completed tasks and hours worked
        try {
            // Step 1: Fetch employees
            String employeeJson = UserController.fetchEmployees();
            List<String[]> employees = UserController.parseEmployeesJson(employeeJson);

            // ✅ Fetch tasks ONCE (optimization)
            String taskJson = TaskController.fetchTasks();
            List<String[]> tasks = TaskController.parseTasksJson(taskJson);

            List<Payroll> payrollList = new ArrayList<>();

            // Step 2: Loop employees
            for (String[] emp : employees) {

                String username = emp[0];

                // Fetch attendance per user
                String attendanceJson = AttendanceController.fetchAttendanceByUsername(username);
                List<String[]> attendance = AttendanceController.parseAttendanceJson(attendanceJson);

                int completedTasks = 0;
                int totalHours = 0;

                // Step 3: Count completed tasks
                String jsonBody = String.format(
                    "{\"p_username\":\"%s\",\"p_period\":\"%s\"}",
                    username,
                    getToday_Period()
                );

                var response = SupabaseClient.rpc("count_completed_tasks_by_user_period", jsonBody, null);
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    String countStr = response.body().replaceAll("[^0-9]", "");
                    completedTasks = Integer.parseInt(countStr);
                } else {
                    System.err.println("Failed to fetch completed tasks count for " + username + ": " + response.body());
                }

                // Step 4: Calculate attendance hours
                for (String[] att : attendance) {
                    String clockIn = att[2];
                    String clockOut = att[3];

                    if (clockOut == null || clockOut.isEmpty()) continue;

                    int hours = Integer.parseInt(
                        AttendanceController
                            .calculateHoursWorked(clockIn, clockOut)
                            .split(" ")[0]
                    );

                    totalHours += hours;
                }
                // Step 5: Calculate pay
                double payPerTask = 500;
                double hourlyRate = 400;

                double taskPay = completedTasks * payPerTask;
                double attendancePay = totalHours * hourlyRate;
                double totalPay = taskPay + attendancePay;

                payrollList.add(new Payroll(
                        username,
                        completedTasks,
                        totalHours,
                        taskPay,
                        attendancePay,
                        totalPay
                ));
            }

            // Step 6: Save to Supabase
            savePayrollToFile(payrollList);

            System.out.println("Payroll calculated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double CalculateProfit() {
        double profit = 0;

        try {
            var response = SupabaseClient.rpc(
                "sum_confirmed_orders_total_price",
                "{}", 
                null
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String profitStr = response.body().replaceAll("[^0-9.]", "");
                profit = Double.parseDouble(profitStr);
                System.out.println("Profit for the period: " + profit);
            } else {
                System.err.println("Failed to calculate profit: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return profit;
    }

    public static double getTotalPayroll() {
        double total = 0;

        try {
            String payrollJson = fetchPayroll();
            List<String[]> payrollData = parsePayrollJson(payrollJson);

            for (String[] row : payrollData) {
                total += Double.parseDouble(row[5]); // totalPay column
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }    
            

    private static void savePayrollToFile(List<Payroll> payrollList) {
        try {
            for (Payroll payroll : payrollList) {

                String jsonBody = String.format(
                    "{\"username\": \"%s\", \"completedtasks\": %d, \"total_hours\": %d, \"taskpay\": %.2f, \"attendancepay\": %.2f, \"totalpay\": %.2f, \"period\": \"%s\"}",
                    payroll.getUsername(),
                    payroll.getTasksCompleted(),
                    payroll.getTotalHours(),
                    payroll.getTaskPay(),
                    payroll.getAttendancePay(),
                    payroll.getTotalPay(),
                    payroll.getPeriod()
                );

                HttpResponse<String> response = SupabaseClient.Tables.PAYROLL_TABLE.postUpsert(jsonBody, "username,period", null);

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("Payroll entry for " + payroll.getUsername() + " saved successfully.");
                } else {
                    System.err.println("Failed to save payroll entry for " + payroll.getUsername());
                    System.err.println("Status: " + response.statusCode());
                    System.err.println("Response: " + response.body());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving payroll to Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String fetchPayroll() throws Exception {
        HttpResponse<String> response = SupabaseClient.Tables.PAYROLL_TABLE.get("", null);
        return response.body();
    }

    public static List<String[]> parsePayrollJson(String jsonResponse) {
        List<String[]> payrollData = new ArrayList<>();

        try {
            String content = jsonResponse.substring(1, jsonResponse.length() - 1);

            List<String> entries = new ArrayList<>();
            int braceCount = 0, start = -1;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '{') {
                    if (braceCount == 0) start = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0 && start != -1) {
                        entries.add(content.substring(start, i + 1));
                        start = -1;
                    }
                }
            }

            for (String entry : entries) {

                String username = extractValue(entry, "username");
                String completedTasks = extractValue(entry, "completedtasks");
                String totalHours = extractValue(entry, "total_hours");
                String taskPay = extractValue(entry, "taskpay");
                String attendancePay = extractValue(entry, "attendancepay");
                String totalPay = extractValue(entry, "totalpay");

                payrollData.add(new String[]{
                        username,
                        completedTasks,
                        totalHours,
                        taskPay,
                        attendancePay,
                        totalPay
                });
            }

        } catch (Exception e) {
            payrollData.add(new String[]{"Error parsing payroll data"});
        }

        return payrollData;
    }

    private static String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);

        if (startIndex == -1) return "";

        startIndex += searchKey.length();

        char firstChar = json.charAt(startIndex);

        if (firstChar == '"') {
            int endIndex = json.indexOf('"', startIndex + 1);
            return json.substring(startIndex + 1, endIndex);
        } else {
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) endIndex = json.indexOf('}', startIndex);
            return json.substring(startIndex, endIndex).trim();
        }
    }
}