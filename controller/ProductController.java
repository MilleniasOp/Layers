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
                "{\"name\":\"%s\",\"cost\":\"%s\"}",
                product.getName(),
                product.getCost()
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

        try {
            String content = jsonResponse.substring(1, jsonResponse.length() - 1);

            List<String> objects = new ArrayList<>();
            int braceCount = 0, start = -1;

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

            for (String obj : objects) {
                String name = "", cost = "";

                String[] pairs = obj.substring(1, obj.length() - 1).split(",");

                for (String pair : pairs) {
                    pair = pair.trim();

                    if (pair.contains("\"name\"")) {
                        name = pair.split(":", 2)[1].replaceAll("^\"|\"$", "").trim();
                    } else if (pair.contains("\"cost\"")) {
                        cost = pair.split(":")[1].replace("\"", "").trim();
                    }                    

                }

                products.add(new String[]{name, cost});
            }

        } catch (Exception e) {
            products.add(new String[]{"Parse Error", ""});
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
    public static void showAddProductDialog(JFrame parentFrame){

        JDialog dialog = new JDialog(parentFrame, "Add New Product", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2,2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField product_name =  new JTextField(10);
        JTextField product_cost = new JTextField(10);

        panel.add(new JLabel("Product Name"));
        panel.add(product_name);
        panel.add(new JLabel("Product Cost"));
        panel.add(product_cost);

        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String name = product_name.getText();

            float cost;
            try{
                cost = Float.parseFloat(product_cost.getText());
            } catch (NumberFormatException ex){
                UIUtils.showMessage(dialog, "Error", "Please enter a valid number for cost");
                return;
            }

            try{
                Product p = new Product(name,cost);
                AddNewProduct(p);
                UIUtils.showMessage(dialog, "Success", "Product created successfully!");
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
