package controller;

import entity.User;
import utils.SupabaseClient;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import entity.Employee;
import entity.Owner;
import entity.Customer;

import utils.UIUtils;
import java.awt.*;

public class UserController {

    private static void AddNewEmployee(Employee employee) { saveEmployee(employee); }
    private static void AddNewCustomer(Customer customer) { saveCustomer(customer); }
    private static void AddNewOwner(Owner owner)          { saveOwner(owner); }

    private static void saveOwner(Owner owner) {
        try {
            String jsonBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"director\"}",
                    owner.getUsername(), owner.getPassword());
            HttpResponse<String> response = SupabaseClient.post("users", jsonBody, null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Owner saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save owner. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving owner: " + e.getMessage());
        }
    }

    private static void saveEmployee(Employee employee) {
        try {
            String jsonBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"employee\"}",
                    employee.getUsername(), employee.getPassword());
            HttpResponse<String> response = SupabaseClient.post("users", jsonBody, null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Employee saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save employee. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving employee: " + e.getMessage());
        }
    }
    
    private static void saveCustomer(Customer customer) {
        try {
            String jsonBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"customer\"}",
                    customer.getUsername(), customer.getPassword());
            HttpResponse<String> response = SupabaseClient.post("users", jsonBody, null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Customer saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save customer. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving customer: " + e.getMessage());
        }
    }


    public static String fetchWorkers() {
        try {
            HttpResponse<String> response = SupabaseClient.get("users?role=neq.customer", null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return "Error fetching users: HTTP " + response.statusCode() + " - " + response.body();
            }
        } catch (IOException | InterruptedException e) {
            return "Error fetching users: " + e.getMessage();
        }
    }

    public static String fetchEmployees() {
        try {
            HttpResponse<String> response = SupabaseClient.get("users?role=eq.employee", null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return "Error fetching employees: HTTP " + response.statusCode() + " - " + response.body();
            }
        } catch (IOException | InterruptedException e) {
            return "Error fetching employees: " + e.getMessage();
        }
    }

    public static List<String[]> parseWorkersJson(String jsonResponse) {
        List<String[]> workers = new ArrayList<>();
        try {
            if (jsonResponse == null || jsonResponse.length() < 2) return workers;
            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();
            if (content.isEmpty()) return workers;
            for (String obj : splitObjects(content)) {
                workers.add(new String[]{
                        extractValue(obj, "username"),
                        extractValue(obj, "password"),
                        extractValue(obj, "role")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return workers;
    }

    public static List<String[]> parseEmployeesJson(String jsonResponse) {
        List<String[]> employees = new ArrayList<>();
        try {
            if (jsonResponse == null || jsonResponse.length() < 2) return employees;
            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();
            if (content.isEmpty()) return employees;
            for (String obj : splitObjects(content)) {
                employees.add(new String[]{
                        extractValue(obj, "username"),
                        extractValue(obj, "role")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return employees;
    }

    public static void showAddEmployeeDialog(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Add New Employee", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField username = new JTextField(10);
        JTextField password = new JTextField(10);
        panel.add(new JLabel("Employee Username")); panel.add(username);
        panel.add(new JLabel("Employee Password")); panel.add(password);
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String name = username.getText();
            String pass = password.getText();
            
            try {
                Employee employee = new Employee(name, pass);
                AddNewEmployee(employee);
                UIUtils.showMessage(dialog, "Success", "Employee created successfully!");
                dialog.dispose();
            } catch (Exception ex) {
                UIUtils.showMessage(dialog, "Error", "Error: " + ex.getMessage());
            }
        });
        JPanel btnPanel = new JPanel();
        btnPanel.add(createButton);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }

    public static void showAddDirectorDialog(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Add New Director", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField username = new JTextField(10);
        JTextField password = new JTextField(10);
        panel.add(new JLabel("Director Name"));    panel.add(username);
        panel.add(new JLabel("Director Password")); panel.add(password);
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String name = username.getText();
            String pass = password.getText();

            try {
                Owner owner = new Owner(name,pass);
                AddNewOwner(owner);
                UIUtils.showMessage(dialog, "Success", "Director created successfully!");
                dialog.dispose();
            } catch (Exception ex) {
                UIUtils.showMessage(dialog, "Error", "Error: " + ex.getMessage());
            }
        });
        JPanel btnPanel = new JPanel();
        btnPanel.add(createButton);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }

    private static List<String> splitObjects(String content) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0, start = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') { if (braceCount++ == 0) start = i; }
            else if (c == '}' && --braceCount == 0) objects.add(content.substring(start, i + 1));
        }
        return objects;
    }

    private static String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1) return "";
            start += pattern.length();
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (json.charAt(start) == '"') {
                start++;
                return json.substring(start, json.indexOf('"', start));
            }
            return "";
        } catch (Exception e) { return ""; }
    }
}