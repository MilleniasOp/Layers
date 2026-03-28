package controller;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import entity.Employee;
import entity.Owner;
import entity.Customer;

import utils.SupabaseClient;
import utils.UIUtils;

import java.awt.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class UserController {

    private static void AddNewEmployee(Employee employee){
        saveEmployee(employee);
    }


    private static void AddNewCustomer(Customer customer){
        saveCustomer(customer);
    }


    private static void AddNewOwner(Owner owner){
        saveOwner(owner);
    }

    private static void saveOwner(Owner owner) {
        try {
            // Create JSON body for the insert
            String jsonBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"director\"}",
                owner.getUsername(),
                owner.getPassword()

            );

            // POST to User table
            HttpResponse<String> response = SupabaseClient.post("User", jsonBody, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Owner saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save owner to Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving owner to Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveEmployee(Employee employee) {
        try {
            // Create JSON body for the insert
            String jsonBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"employee\"}",
                employee.getUsername(),
                employee.getPassword()
            );

            // POST to User table
            HttpResponse<String> response = SupabaseClient.post("User", jsonBody, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Employee saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save employee to Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving employee to Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveCustomer(Customer customer) {
        try {
            // Create JSON body for the insert
            String jsonBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"director\"}",
                customer.getUsername(),
                customer.getPassword()

            );

            // POST to User table
            HttpResponse<String> response = SupabaseClient.post("User", jsonBody, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Owner saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save owner to Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving owner to Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String fetchWorkers(){
        try{
            HttpResponse<String> response = SupabaseClient.get("User?role=neq.customer" , null);
            if (response.statusCode() >= 200 && response.statusCode() < 300){
                return response.body();
            }else{
                return "Error fetching users: HTTP "+ response.statusCode()+ " - " + response.body();
            }

        }catch(IOException | InterruptedException e){
            return "Error fetching users: " + e.getMessage();
        }
    }
    public static String fetchEmployees(){
        try{
            HttpResponse<String> response = SupabaseClient.get("User?role=eq.employee", null);
            if (response.statusCode() >= 200 && response.statusCode() < 300){
                return response.body();
            }else{
                return "Error fetching users: HTTP "+ response.statusCode()+ " - " + response.body();
            }
        }catch (IOException | InterruptedException e){
            return "Error fetching users: " + e.getMessage();
        }

    }

    public static List<String[]> parseWorkersJson(String jsonResponse) {

        List<String[]> workers = new ArrayList<>();

        try {
            if (jsonResponse == null || jsonResponse.length() < 2) return workers;

            // Remove outer [ ]
            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();

            if (content.isEmpty()) return workers;

            // Split JSON objects safely using brace counting
            List<String> objects = new ArrayList<>();
            int braceCount = 0;
            int start = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '{') {
                    if (braceCount == 0) start = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        objects.add(content.substring(start, i + 1));
                    }
                }
            }

            // Extract only username and role (ignore id or anything else)
            for (String obj : objects) {

                String username = extractValue(obj, "username");
                String password = extractValue(obj, "password");
                String role = extractValue(obj, "role");

                workers.add(new String[]{username, password, role});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return workers;
    }   


    public static List<String[]> parseEmployeesJson(String jsonResponse) {

        List<String[]> employees = new ArrayList<>();

        try {
            if (jsonResponse == null || jsonResponse.length() < 2) return employees;

            // Remove outer [ ]
            String content = jsonResponse.substring(1, jsonResponse.length() - 1).trim();

            if (content.isEmpty()) return employees;

            // Split JSON objects safely using brace counting
            List<String> objects = new ArrayList<>();
            int braceCount = 0;
            int start = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '{') {
                    if (braceCount == 0) start = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        objects.add(content.substring(start, i + 1));
                    }
                }
            }

            // Extract only username and role (ignore id or anything else)
            for (String obj : objects) {

                String username = extractValue(obj, "username");
                String role = extractValue(obj, "role");

                employees.add(new String[]{username, role});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return employees;
    }   
    
    private static String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);

            if (start == -1) return "";

            start += pattern.length();

            // Skip whitespace
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }

            // Handle string values
            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf('"', start);
                return json.substring(start, end);
            }

            return "";

        } catch (Exception e) {
            return "";
        }
    }

    public static void showAddEmployeeDialog(JFrame parentFrame){

        JDialog dialog = new JDialog(parentFrame, "Add New Employee", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2,2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField username =  new JTextField(10);
        JTextField password = new JTextField(10);

        panel.add(new JLabel("Employee Username"));
        panel.add(username);
        panel.add(new JLabel("Employee Password"));
        panel.add(password);

        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String name = username.getText();

            String pass = password.getText();

            try{
                Employee employee = new Employee(name,pass);
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

    public static void showAddDirectorDialog(JFrame parentFrame){

        JDialog dialog = new JDialog(parentFrame, "Add New Director", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2,2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField username =  new JTextField(10);
        JTextField password = new JTextField(10);

        panel.add(new JLabel("Director Name"));
        panel.add(username);
        panel.add(new JLabel("Director Pasword"));
        panel.add(password);

        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String name = username.getText();
            String pass = password.getText();

            try{
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



}
