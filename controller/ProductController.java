package controller;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import entity.Product;
import utils.SupabaseClient;
import utils.UIUtils;

import java.awt.*;


public class ProductController {

    public static void AddNewProduct(Product product){
        saveProduct(product);
    }

    private static void saveProduct(Product product) {
        try {
            // Create JSON body for the insert
            String jsonBody = String.format(
                "{\"item_id\":\"%s\",\"name\":\"%s\",\"description\":\"%s\",\"price\":\"%s\",\"available\":\"%s\"}",
                product.getItemId(),
                product.getName(),
                product.getDescription(),
                String.valueOf(product.getPrice()),
                String.valueOf(product.isAvailable())
            );

            // POST to User table
            HttpResponse<String> response = SupabaseClient.Tables.PRODUCTS_TABLE.post(jsonBody, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Product saved to Supabase successfully.");
            } else {
                System.err.println("Failed to save product to Supabase. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error saving product to Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // ================= PRODUCT PARSER =================
    public static List<String[]> parseProductsJson(String jsonResponse) {
        List<String[]> products = new ArrayList<>();
        if (jsonResponse == null || jsonResponse.equals("[]") || jsonResponse.isBlank()) return products;

        jsonResponse = jsonResponse.trim().substring(1, jsonResponse.length() - 1); 
        for (String obj : jsonResponse.split("\\},\\s*\\{")) {
            String itemId      = BrowseMenuController.extractString(obj, "item_id");
            String name        = BrowseMenuController.extractString(obj, "name");
            String description = BrowseMenuController.extractString(obj, "description");
            String price       = String.valueOf(BrowseMenuController.extractDouble(obj, "price"));
            String available   = String.valueOf(BrowseMenuController.extractBoolean(obj, "available"));
            products.add(new String[]{itemId, name, description, price, available});
        }
        return products;
    }

    public static String fetchProducts(){
        try{
            HttpResponse<String> response = SupabaseClient.Tables.PRODUCTS_TABLE.get("", null);
            if (response.statusCode() >= 200 && response.statusCode() < 300){
                return response.body();
            }else{
                return "Error fetching products: HTTP "+ response.statusCode()+ " - " + response.body();
            }
        }catch (IOException | InterruptedException e){
            return "Error fetching product: " + e.getMessage();
        }

    }

    //================== Add Product Dialog ==================
    public static void showAddProductDialog(JFrame parentFrame) {

        JDialog dialog = new JDialog(parentFrame, "Add New Product", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JTextField itemIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField costField = new JTextField();

        // Better UX than typing true/false
        JCheckBox availableCheck = new JCheckBox("Available");

        formPanel.add(new JLabel("Item ID:"));
        formPanel.add(itemIdField);

        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Cost:"));
        formPanel.add(costField);

        formPanel.add(new JLabel("")); // empty space
        formPanel.add(availableCheck);

        // Buttons
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);

        // Actions
        cancelButton.addActionListener(e -> dialog.dispose());

        createButton.addActionListener(e -> {
            String itemId = itemIdField.getText().trim();
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            boolean available = availableCheck.isSelected();

            float cost;
            try {
                cost = Float.parseFloat(costField.getText().trim());
            } catch (NumberFormatException ex) {
                UIUtils.showMessage(dialog, "Error", "Please enter a valid number for cost");
                return;
            }

            if (itemId.isEmpty() || name.isEmpty()) {
                UIUtils.showMessage(dialog, "Error", "Item ID and Name are required");
                return;
            }

            try {
                Product p = new Product(itemId, name, description, cost, available);
                AddNewProduct(p);
                UIUtils.showMessage(dialog, "Success", "Product created successfully!");
                dialog.dispose();
            } catch (Exception ex) {
                UIUtils.showMessage(dialog, "Error", "Error: " + ex.getMessage());
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
}
