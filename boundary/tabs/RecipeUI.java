package boundary.tabs;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import boundary.pages.OwnerDashBoardUI;
import controller.RecipeController;
import utils.UIUtils;

import java.awt.*;
import java.util.List;

public class RecipeUI {

    public static JPanel createRecipePanel(String[] args) {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Recipe Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton addBtn = createStyledButton("➕ Add");
        JButton refreshBtn = createStyledButton("🔄 Refresh");
        JButton viewBtn = createStyledButton("👁 View");

        buttonPanel.add(addBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // ===== TABLE =====
        String[] header = {"Recipe ID", "Product Name"};
        JTable table = new JTable();
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        refreshTable(table, header);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ===== BUTTON ACTIONS =====
        addBtn.addActionListener(e -> {
            RecipeController.showAddRecipeDialog(null);
            refreshTable(table, header);
        });

        refreshBtn.addActionListener(e -> refreshTable(table, header));

        viewBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow != -1) {
                String recipeId = (String) table.getValueAt(selectedRow, 0);

                String ingredientdata = RecipeController.fetchRecipeIngredients(recipeId);
                String[] ingredientHeader = {"Recipe ID", "Ingredient", "Measurement"};

                List<String[]> ingredientList = RecipeController.parseRecipeIngredientsJson(ingredientdata);
                String[][] ingredientData = ingredientList.toArray(new String[0][]);

                JTable ingredientTable = new JTable(ingredientData, ingredientHeader);

                JOptionPane.showMessageDialog(
                        mainPanel,
                        new JScrollPane(ingredientTable),
                        "Recipe Ingredients",
                        JOptionPane.PLAIN_MESSAGE
                );

            } else {
                UIUtils.showMessage(mainPanel, "Warning", "Select a recipe first.");
            }
        });

        // ===== ADD TO MAIN =====
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    // Styled button
    private static JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 123, 255));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);   
        return btn;
    }

    // Refresh table
    private static void refreshTable(JTable table, String[] header) {
        String data = RecipeController.fetchRecipes();
        List<String[]> list = RecipeController.parseRecipesJson(data);
        table.setModel(new DefaultTableModel(list.toArray(new String[0][]), header));
    }
}