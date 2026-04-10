package boundary.tabs;
import javax.swing.*;
import java.awt.*;
import java.util.List;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controller.OrderController;
import controller.ProductController;
import entity.User;

public class OrderUI {

    public static JPanel createOrderPanel(User user) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Order Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton CancelBtn = createStyledButton("Cancel Order");
        JButton refreshBtn = createStyledButton("🔄 Refresh");   
        
        btnPanel.add(CancelBtn);
        btnPanel.add(refreshBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        // ===== TABLE =====
        String[] header = {"Order ID", "Customer Name", "Status", "Total Price", "Placed At"};
        JTable table = new JTable();
        table.setRowHeight(25);

        refreshTable(table, header);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ===== ACTIONS =====
        CancelBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select an order to cancel.");
                return;
            }
            String orderId = (String) table.getValueAt(selectedRow, 0);
            try {
                String message = new OrderController().cancelOrder(orderId, user);
                JOptionPane.showMessageDialog(null, message);
                refreshTable(table, header);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error cancelling order: " + ex.getMessage());
            }
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
        try {
            String data = OrderController.fetchOrders();
            List<String[]> list = OrderController.parseOrdersJson(data);
            table.setModel(new DefaultTableModel(list.toArray(new String[0][]), header));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading orders: " + e.getMessage());
        }
    }
}    
