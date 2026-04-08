package boundary.pages;

import controller.BrowseMenuController;
import controller.OrderController;
import entity.Product;
import entity.Order;
import entity.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerDashboardUI extends JFrame {

    private final User                    user;
    private final BrowseMenuController    browseCtrl = new BrowseMenuController();
    private final OrderController         orderCtrl  = new OrderController();

    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable  table       = new JTable(tableModel);
    private final JLabel  statusLabel = new JLabel(" ");

    private String selectedOrderId = null;

    public CustomerDashboardUI(User user) {
        this.user = user;
        buildUI();
    }

    private void buildUI() {
        setTitle("Customer Dashboard — " + user.getUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setBackground(new Color(30, 136, 229));
        JLabel welcome = new JLabel("  Welcome, " + user.getUsername() + "  |  Customer Portal");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        topBar.add(welcome);
        add(topBar, BorderLayout.NORTH);

        JPanel sidebar = new JPanel(new GridLayout(6, 1, 5, 5));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnBrowse = sideBtn("🍽  Browse Menu",       Color.decode("#1565C0"));
        JButton btnPlace  = sideBtn("🛒  Place New Order",   Color.decode("#2E7D32"));
        JButton btnCancel = sideBtn("✖  Cancel an Order",   Color.decode("#C62828"));
        JButton btnView   = sideBtn("📋  My Order Details",  Color.decode("#6A1B9A"));
        JButton btnLogout = sideBtn("🚪  Logout",            Color.decode("#37474F"));

        sidebar.add(btnBrowse);
        sidebar.add(btnPlace);
        sidebar.add(btnCancel);
        sidebar.add(btnView);
        sidebar.add(new JLabel());
        sidebar.add(btnLogout);
        add(sidebar, BorderLayout.WEST);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Results"));
        add(scroll, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        add(statusLabel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                Object val = tableModel.getValueAt(table.getSelectedRow(), 0);
                selectedOrderId = val != null ? val.toString() : null;
            }
        });

        btnBrowse.addActionListener(e -> loadBrowseMenu());
        btnPlace.addActionListener(e  -> openPlaceOrder());
        btnCancel.addActionListener(e -> openCancelOrder());
        btnView.addActionListener(e   -> viewSelectedOrderDetails());
        btnLogout.addActionListener(e -> logout());

        setVisible(true);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            (new AuthenticatorUI()).run();
        }
    }

    private void loadBrowseMenu() {
        selectedOrderId = null;
        runAsync("Loading menu…", () -> {
            List<Product> items = browseCtrl.retrieveMenuItems();
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                tableModel.setColumnIdentifiers(
                        new String[]{"ID", "Name", "Description", "Price", "Available"});
                if (items.isEmpty()) { status("No menu items available at the moment."); return; }
                for (Product p : items) {
                    tableModel.addRow(new Object[]{
                            p.getItemId(), p.getName(), p.getDescription(),
                            String.format("$%.2f", p.getPrice()),
                            p.isAvailable() ? "✔ Yes" : "✘ No"
                    });
                }
                status("Menu loaded — " + items.size() + " items.");
            });
        });
    }

    private void openPlaceOrder() {
        runAsync("Fetching available items…", () -> {
            List<Product> items = orderCtrl.getAvailableItems();
            SwingUtilities.invokeLater(() -> {
                if (items.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No items are currently in stock.", "Place Order",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JPanel panel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(5, 5, 5, 5);
                
                gbc.gridx = 0;
                gbc.gridy = 0;
                panel.add(new JLabel("Select"), gbc);
                gbc.gridx = 1;
                panel.add(new JLabel("Item Name"), gbc);
                gbc.gridx = 2;
                panel.add(new JLabel("Price"), gbc);
                gbc.gridx = 3;
                panel.add(new JLabel("Quantity"), gbc);
                
                List<JCheckBox> checkBoxes = new java.util.ArrayList<>();
                List<JSpinner> spinners = new java.util.ArrayList<>();
                
                int row = 1;
                for (Product item : items) {
                    // Checkbox
                    gbc.gridx = 0;
                    gbc.gridy = row;
                    JCheckBox checkBox = new JCheckBox();
                    checkBoxes.add(checkBox);
                    panel.add(checkBox, gbc);
                    
                    // Item name
                    gbc.gridx = 1;
                    panel.add(new JLabel(item.getName()), gbc);
                    
                    // Price
                    gbc.gridx = 2;
                    panel.add(new JLabel(String.format("$%.2f", item.getPrice())), gbc);
                    
                    // Quantity spinner (1-10)
                    gbc.gridx = 3;
                    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
                    JSpinner spinner = new JSpinner(spinnerModel);
                    spinner.setEnabled(false);
                    spinners.add(spinner);
                    panel.add(spinner, gbc);
                    
                    checkBox.addActionListener(e -> {
                        spinner.setEnabled(checkBox.isSelected());
                        if (!checkBox.isSelected()) {
                            spinner.setValue(1);
                        }
                    });
                    
                    row++;
                }
                
                JScrollPane scrollPane = new JScrollPane(panel);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                
                int result = JOptionPane.showConfirmDialog(this,
                        scrollPane, "Select Items to Order (with quantities)",
                        JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) return;
                
                List<String> selectedIds = new java.util.ArrayList<>();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        int quantity = (Integer) spinners.get(i).getValue();
                        Product item = items.get(i);
                        for (int q = 0; q < quantity; q++) {
                            selectedIds.add(item.getItemId());
                        }
                    }
                }
                
                if (selectedIds.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No items selected.");
                    return;
                }
                
                runAsync("Placing order…", () -> {
                    Order order = orderCtrl.buildOrder(user.getUsername(), selectedIds);
                    StringBuilder sb = new StringBuilder("Order Summary:\n\n");
                    
                    java.util.Map<String, Integer> quantityMap = new java.util.HashMap<>();
                    java.util.Map<String, Product> itemMap = new java.util.HashMap<>();
                    
                    for (Order.OrderItem orderItem : order.getItems()) {
                        Product item = orderItem.getMenuItem();
                        int quantity = orderItem.getQuantity();
                        quantityMap.put(item.getName(), quantity);
                        itemMap.put(item.getName(), item);
                    }
                    
                    for (java.util.Map.Entry<String, Integer> entry : quantityMap.entrySet()) {
                        Product item = itemMap.get(entry.getKey());
                        int quantity = entry.getValue();
                        sb.append("  • ").append(item.getName())
                          .append(" x").append(quantity)
                          .append("  $").append(String.format("%.2f", item.getPrice() * quantity))
                          .append("\n");
                    }
                    sb.append(String.format("%nTOTAL:  $%.2f", order.getTotalPrice()));
                    
                    int confirm = JOptionPane.showConfirmDialog(this,
                            sb.toString(), "Confirm Order?", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    
                    orderCtrl.confirmOrder(order);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "✔ Order placed!\nOrder ID: " + order.getOrderId()
                                + "\n\nYou have 5 minutes to cancel if needed.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadOrderHistory();
                    });
                });
            });
        });
    }

    private void openCancelOrder() {
        runAsync("Fetching your cancellable orders…", () -> {
            List<Order> orders = orderCtrl.fetchActiveOrdersForUser(user.getUsername());
            SwingUtilities.invokeLater(() -> {
                if (orders.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "You have no pending orders that can be cancelled.\n"
                            + "(Only orders placed within the last 5 minutes can be cancelled.)",
                            "Cancel Order", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] options = orders.stream()
                        .map(o -> o.getOrderId()
                                + "  |  $" + String.format("%.2f", o.getTotalPrice())
                                + "  |  " + o.getPlacedAt())
                        .toArray(String[]::new);

                String chosen = (String) JOptionPane.showInputDialog(this,
                        "Select an order to cancel:", "Cancel Order",
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (chosen == null) return;

                String orderId = chosen.split("\\s*\\|")[0].trim();

                runAsync("Cancelling…", () -> {
                    String msg = orderCtrl.cancelOrder(orderId, user.getUsername());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, msg,
                                msg.startsWith("SUCCESS") ? "Success" : "Notice",
                                msg.startsWith("ERROR")
                                        ? JOptionPane.ERROR_MESSAGE
                                        : JOptionPane.INFORMATION_MESSAGE);
                        loadOrderHistory();
                    });
                });
            });
        });
    }

    private void loadOrderHistory() {
        selectedOrderId = null;
        runAsync("Loading your orders…", () -> {
            List<Order> orders = orderCtrl.getOrderHistory(user.getUsername());
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                tableModel.setColumnIdentifiers(
                        new String[]{"Order ID", "Status", "Total", "Placed At"});
                if (orders.isEmpty()) { status("You have no orders on record."); return; }
                for (Order o : orders) {
                    tableModel.addRow(new Object[]{
                            o.getOrderId(), o.getStatus(),
                            String.format("$%.2f", o.getTotalPrice()), o.getPlacedAt()
                    });
                }
                status("" + orders.size() + " order(s) found. "
                        + "Click a row then press '📋 My Order Details' to see items.");
            });
        });
    }

    private void viewSelectedOrderDetails() {
        if (selectedOrderId == null) {
            if (tableModel.getColumnCount() == 0
                    || !"Order ID".equals(tableModel.getColumnName(0))) {
                loadOrderHistory();
                status("Select an order from the list, then press '📋 My Order Details' again.");
                return;
            }
            status("Please click on an order row first, then press '📋 My Order Details'.");
            return;
        }

        String orderId = selectedOrderId;
        runAsync("Loading order details…", () -> {
            Order order = orderCtrl.getOrderDetails(orderId, user.getUsername());
            SwingUtilities.invokeLater(() -> {
                if (order == null) {
                    JOptionPane.showMessageDialog(this,
                            "Could not load order details.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DefaultTableModel detailModel = new DefaultTableModel() {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                };
                detailModel.setColumnIdentifiers(new String[]{"Item", "Quantity", "Price", "Subtotal"});

                for (Order.OrderItem orderItem : order.getItems()) {
                    Product item = orderItem.getMenuItem();
                    int quantity = orderItem.getQuantity();
                    double price = item.getPrice();
                    double subtotal = price * quantity;
                    
                    detailModel.addRow(new Object[]{
                        item.getName(), 
                        quantity, 
                        String.format("$%.2f", price),
                        String.format("$%.2f", subtotal)
                    });
                }

                JTable detailTable = new JTable(detailModel);
                detailTable.setFillsViewportHeight(true);
                JScrollPane scroll = new JScrollPane(detailTable);
                scroll.setPreferredSize(new Dimension(500, 300));

                String info = String.format(
                        "<html><body style='width: 300px;'>" +
                        "<h3>Order Details</h3>" +
                        "<b>Order ID:</b> %s<br>" +
                        "<b>Status:</b> %s<br>" +
                        "<b>Total:</b> $%.2f<br>" +
                        "<b>Placed At:</b> %s<br>" +
                        "<br><b>Items Ordered:</b></body></html>",
                        order.getOrderId(), order.getStatus(),
                        order.getTotalPrice(), order.getPlacedAt());

                JPanel popup = new JPanel(new BorderLayout(0, 8));
                popup.add(new JLabel(info), BorderLayout.NORTH);
                popup.add(scroll, BorderLayout.CENTER);

                JOptionPane.showMessageDialog(this, popup,
                        "Order Details — " + orderId, JOptionPane.PLAIN_MESSAGE);
            });
        });
    }

    private void runAsync(String loadingMsg, ThrowingRunnable task) {
        status(loadingMsg);
        new Thread(() -> {
            try { task.run(); }
            catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Error: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void status(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    private JButton sideBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    @FunctionalInterface
    interface ThrowingRunnable { void run() throws Exception; }

    public void run() { loadBrowseMenu(); }
}