package controller;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import entity.Recipe;
import utils.SupabaseClient;
import utils.UIUtils;

import java.awt.*;

public class RecipeController {

    public static void addNewRecipe(Recipe recipe) {
        saveRecipe(recipe);
    }

    private static void saveRecipe(Recipe recipe) {
        try {
            // Step 1: Create JSON for recipe
            String json = String.format(
                "{\"product_name\":\"%s\"}",
                recipe.getProductName()
            );

            // Step 2: Add header to get inserted row back
            Map<String, String> headers = new HashMap<>();
            headers.put("Prefer", "return=representation");

            // Step 3: Send POST request
            HttpResponse<String> response =
                SupabaseClient.Tables.RECIPES_TABLE.post(json, headers);

            System.out.println("RECIPE RESPONSE: " + response.body());

            // Step 4: Check success
            if (response.statusCode() >= 200 && response.statusCode() < 300) {

                // Step 5: Extract recipe_id safely
                String recipeId = extractIdFromResponse(response.body());

                if (recipeId == null || recipeId.isEmpty()) {
                    System.err.println("ERROR: recipeId is null. Cannot insert ingredients.");
                    return;
                }

                // Step 6: Set ID in object
                recipe.setRecipeId(recipeId);

                System.out.println("Extracted recipeId: " + recipeId);

                // Step 7: Insert ingredients
                insertIngredients(recipe);

            } else {
                System.err.println("Failed to save recipe.");
                System.err.println("Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void insertIngredients(Recipe recipe) {
        try {
            for (int i = 0; i < recipe.getIngredients().size(); i++) {

                String json = String.format(
                    "{\"recipe_id\":%s,\"ingredient_name\":\"%s\",\"measurement\":\"%s\"}",
                    recipe.getRecipeId(),
                    recipe.getIngredients().get(i),
                    recipe.getMeasurements().get(i)
                );

                SupabaseClient.Tables.RECIPE_INGREDIENTS_TABLE.post(json, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractIdFromResponse(String json) {
        try {
            if (json == null || json.isEmpty()) return null;

            int idIndex = json.indexOf("\"recipe_id\"");
            if (idIndex == -1) return null;

            int colon = json.indexOf(":", idIndex);
            if (colon == -1) return null;

            int start = colon + 1;

            // skip whitespace
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }

            int end = start;

            // read number
            while (end < json.length() && Character.isDigit(json.charAt(end))) {
                end++;
            }

            return json.substring(start, end);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String fetchRecipes(){
        try {
            HttpResponse<String> response = SupabaseClient.Tables.RECIPES_TABLE.get("", null);
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return "Error fetching recipes: HTTP " + response.statusCode() + " - " + response.body();
            }
        } catch (IOException | InterruptedException e) {
            return "Error fetching recipes: " + e.getMessage();
        }
    }

    public static String fetchRecipeIngredients(String recipeId){
        try {
            String path = "?recipe_id=eq." + recipeId;
            HttpResponse<String> response = SupabaseClient.Tables.RECIPE_INGREDIENTS_TABLE.get(path, null);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return "Error fetching recipe ingredients: HTTP " + response.statusCode() + " - " + response.body();
            }
        } catch (IOException | InterruptedException e) {
            return "Error fetching recipe ingredients: " + e.getMessage();
        }
    }
    // ================= INGREDIENT PARSER =================
    public static List<String[]> parseRecipeIngredientsJson(String jsonResponse) {
        List<String[]> ingredients = new ArrayList<>();

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            ingredients.add(new String[]{"", "No data", ""});
            return ingredients;
        }

        try {
            String content = jsonResponse.trim();

            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1);
            }

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
                String id = "", name = "", measure = "";

                String[] pairs = obj.substring(1, obj.length() - 1).split(",");

                for (String pair : pairs) {
                    pair = pair.trim();

                    if (pair.contains("recipe_id")) {
                        id = pair.split(":")[1].replace("\"", "").trim();
                    } else if (pair.contains("ingredient_name")) {
                        name = pair.split(":", 2)[1].replaceAll("^\"|\"$", "").trim();
                    } else if (pair.contains("measurement")) {
                        measure = pair.split(":", 2)[1].replaceAll("^\"|\"$", "").trim();
                    }
                }

                ingredients.add(new String[]{id, name, measure});
            }

        } catch (Exception e) {
            ingredients.add(new String[]{"", "Parse Error", ""});
        }

        return ingredients;
    }

    // ================= RECIPE PARSER =================
    public static List<String[]> parseRecipesJson(String jsonResponse) {
        List<String[]> recipes = new ArrayList<>();

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
                String id = "", name = "";

                String[] pairs = obj.substring(1, obj.length() - 1).split(",");

                for (String pair : pairs) {
                    pair = pair.trim();

                    if (pair.contains("recipe_id")) {
                        id = pair.split(":")[1].replace("\"", "").trim();
                    } else if (pair.contains("product_name")) {
                        name = pair.split(":", 2)[1].replaceAll("^\"|\"$", "").trim();
                    }
                }

                recipes.add(new String[]{id, name});
            }

        } catch (Exception e) {
            recipes.add(new String[]{"", "Parse Error"});
        }

        return recipes;
    }

    // ================= ADD RECIPE DIALOG =================
    public static void showAddRecipeDialog(JFrame parentFrame) {

        JDialog dialog = new JDialog(parentFrame, "Add New Recipe", true);
        dialog.setSize(500, 500);
        dialog.setLayout(new BorderLayout());

        // Top panel (product name)
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField productNameField = new JTextField();
        topPanel.add(new JLabel("Product Name:"));
        topPanel.add(productNameField);

        // Ingredient container
        JPanel ingredientContainer = new JPanel();
        ingredientContainer.setLayout(new BoxLayout(ingredientContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(ingredientContainer);

        // Store fields
        List<JTextField> ingredientFields = new ArrayList<>();
        List<JTextField> measurementFields = new ArrayList<>();

        // Add row function
        Runnable addRow = () -> {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField ingredientField = new JTextField(10);
            JTextField measurementField = new JTextField(8);
            JButton removeButton = new JButton("X");

            row.add(new JLabel("Ingredient:"));
            row.add(ingredientField);
            row.add(new JLabel("Measurement:"));
            row.add(measurementField);
            row.add(removeButton);

            ingredientFields.add(ingredientField);
            measurementFields.add(measurementField);

            removeButton.addActionListener(e -> {
                ingredientContainer.remove(row);
                ingredientFields.remove(ingredientField);
                measurementFields.remove(measurementField);
                ingredientContainer.revalidate();
                ingredientContainer.repaint();
            });

            ingredientContainer.add(row);
            ingredientContainer.revalidate();
            ingredientContainer.repaint();
        };

        addRow.run(); // Add first row

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addIngredientButton = new JButton("Add Ingredient");
        JButton createButton = new JButton("Create Recipe");
        buttonPanel.add(addIngredientButton);
        buttonPanel.add(createButton);

        addIngredientButton.addActionListener(e -> addRow.run());

        createButton.addActionListener(e -> {
            String productName = productNameField.getText().trim();
            if (productName.isEmpty()) {
                UIUtils.showMessage(dialog, "Error", "Enter product name!");
                return;
            }

            List<String> ingredients = new ArrayList<>();
            List<String> measurements = new ArrayList<>();

            for (int i = 0; i < ingredientFields.size(); i++) {
                String ing = ingredientFields.get(i).getText().trim();
                String meas = measurementFields.get(i).getText().trim();

                if (!ing.isEmpty() && !meas.isEmpty()) {
                    ingredients.add(ing);
                    measurements.add(meas);
                }
            }

            if (ingredients.isEmpty()) {
                UIUtils.showMessage(dialog, "Error", "Add at least one ingredient!");
                return;
            }

            try {
                Recipe r = new Recipe(productName, ingredients, measurements);
                addNewRecipe(r);
                UIUtils.showMessage(dialog, "Success", "Recipe created successfully!");
                dialog.dispose();
            } catch (Exception ex) {
                UIUtils.showMessage(dialog, "Error", "Error: " + ex.getMessage());
            }
        });

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }
}