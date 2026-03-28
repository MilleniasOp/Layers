package boundary.tabs;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controller.ProductController;

import java.awt.*;
import java.util.List;

public class ProductUI {

    public static JPanel createProductPanel(String[] args) {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Product Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton addBtn = createStyledButton("➕ Add");
        JButton refreshBtn = createStyledButton("🔄 Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(refreshBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        // ===== TABLE =====
        String[] header = {"Product Name", "Product Cost"};
        JTable table = new JTable();
        table.setRowHeight(25);

        refreshTable(table, header);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ===== ACTIONS =====
        addBtn.addActionListener(e -> {
            ProductController.showAddProductDialog(null);
            refreshTable(table, header);
        });

        refreshBtn.addActionListener(e -> refreshTable(table, header));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);

        return mainPanel;
    }

    private static JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 123, 255));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.CENTER); // ✅ center text+emoji horizontally
        btn.setVerticalAlignment(SwingConstants.CENTER);   // ✅ center vertically
        return btn;
    }

    private static void refreshTable(JTable table, String[] header) {
        String data = ProductController.fetchProducts();
        List<String[]> list = ProductController.parseProductsJson(data);
        table.setModel(new DefaultTableModel(list.toArray(new String[0][]), header));
    }
}