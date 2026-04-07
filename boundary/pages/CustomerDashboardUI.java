package boundary.pages;

import controller.BrowseMenuController;
import controller.CancelOrderController;
import controller.NewOrderController;
import controller.OrderDetailsController;
import entity.MenuItem;
import entity.Order;
import entity.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerDashboardUI extends JFrame {

    private final User user;
    private final BrowseMenuController browseCtrl = new BrowseMenuController();
    private final NewOrderController placeCtrl = new NewOrderController();
    private final CancelOrderController cancelCtrl = new CancelOrderController();
    private final OrderDetailsController viewCtrl = new OrderDetailsController();

    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable            table      = new JTable(tableModel);
    private final JLabel            statusLabel = new JLabel(" ");

    public CustomerDashboardUI(User user) {
        this.user = user;
        buildUI();
    }

    private void buildUI() {
        setTitle("Customer Dashboard — " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        JButton btnBrowse   = sideBtn("🍽  Browse Menu",        Color.decode("#1565C0"));
        JButton btnPlace    = sideBtn("🛒  Place New Order",    Color.decode("#2E7D32"));
        JButton btnCancel   = sideBtn("✖  Cancel an Order",    Color.decode("#C62828"));
        JButton btnView     = sideBtn("📋  My Order Details",   Color.decode("#6A1B9A"));
        JButton btnLogout   = sideBtn("🚪  Logout",             Color.decode("#37474F"));

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

        btnBrowse.addActionListener(e -> loadBrowseMenu());
        btnPlace.addActionListener(e  -> openPlaceOrder());
        btnCancel.addActionListener(e -> openCancelOrder());
        btnView.addActionListener(e   -> loadOrderHistory());
        btnLogout.addActionListener(e -> { dispose(); });

        setVisible(true);
    }

    private void loadBrowseMenu() {
        runAsync("Loading menu…", () -> {
            List<MenuItem> items = browseCtrl.retrieveMenuItems();
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Description", "Price", "Available"});
                if (items.isEmpty()) {
                    status("No menu items available at the moment.");
                    return;
                }
                for (MenuItem m : items) {
                    tableModel.addRow(new Object[]{
                            m.getItemId(), m.getName(), m.getDescription(),
                            String.format("$%.2f", m.getPrice()),
                            m.isAvailable() ? "✔ Yes" : "✘ No"
                    });
                }
                status("Menu loaded — " + items.size() + " items.");
            });
        });
    }

    private void openPlaceOrder() {
        runAsync("Fetching available items…", () -> {
            List<MenuItem> items = placeCtrl.getAvailableItems();
            SwingUtilities.invokeLater(() -> {
                if (items.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No items are currently in stock.", "Place Order",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JPanel panel = new JPanel(new GridLayout(0, 1));
                List<JCheckBox> checkBoxes = items.stream()
                        .map(i -> new JCheckBox(i.getName() + "  ($" +
                                String.format("%.2f", i.getPrice()) + ")  [" + i.getItemId() + "]"))
                        .collect(Collectors.toList());
                checkBoxes.forEach(panel::add);

                int result = JOptionPane.showConfirmDialog(this,
                        new JScrollPane(panel), "Select Items to Order",
                        JOptionPane.OK_CANCEL_OPTION);

                if (result != JOptionPane.OK_OPTION) return;

                List<String> selectedIds = new java.util.ArrayList<>();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        selectedIds.add(items.get(i).getItemId());
                    }
                }

                if (selectedIds.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No items selected.");
                    return;
                }

                runAsync("Placing order…", () -> {
                    Order order = placeCtrl.buildOrder(user.getUsername(), selectedIds);
                    StringBuilder sb = new StringBuilder("Order Summary:\n\n");
                    order.getItems().forEach(i ->
                            sb.append("  • ").append(i.getName())
                              .append("  $").append(String.format("%.2f", i.getPrice()))
                              .append("\n"));
                    sb.append(String.format("%n  TOTAL:  $%.2f", order.getTotalPrice()));

                    int confirm = JOptionPane.showConfirmDialog(this,
                            sb.toString(), "Confirm Order?", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;

                    placeCtrl.confirmOrder(order);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "✔ Order placed!\nOrder ID: " + order.getOrderId(),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadOrderHistory(); // refresh view
                    });
                });
            });
        });
    }

    private void openCancelOrder() {
        runAsync("Fetching your orders…", () -> {
            List<Order> orders = cancelCtrl.fetchActiveOrdersForUser(user.getUsername());
            SwingUtilities.invokeLater(() -> {
                if (orders.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "You have no active orders to cancel.", "Cancel Order",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] options = orders.stream()
                        .map(o -> o.getOrderId() + "  |  $" +
                                String.format("%.2f", o.getTotalPrice()) +
                                "  |  " + o.getPlacedAt())
                        .toArray(String[]::new);

                String chosen = (String) JOptionPane.showInputDialog(this,
                        "Select an order to cancel:", "Cancel Order",
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (chosen == null) return;

                String orderId = chosen.split("\\s*\\|")[0].trim();

                runAsync("Cancelling…", () -> {
                    String msg = cancelCtrl.cancelOrder(orderId, user.getUsername());
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
        runAsync("Loading your orders…", () -> {
            List<Order> orders = viewCtrl.getOrderHistory(user.getUsername());
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                tableModel.setColumnIdentifiers(
                        new String[]{"Order ID", "Status", "Total", "Placed At"});
                if (orders.isEmpty()) {
                    status("You have no orders on record.");
                    return;
                }
                for (Order o : orders) {
                    tableModel.addRow(new Object[]{
                            o.getOrderId(), o.getStatus(),
                            String.format("$%.2f", o.getTotalPrice()), o.getPlacedAt()
                    });
                }
                status(orders.size() + " order(s) found. Click a row, then select 'My Order Details' to view items.");
            });
        });
    }

    private void runAsync(String loadingMsg, ThrowingRunnable task) {
        status(loadingMsg);
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception ex) {
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
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    public void run() {
        loadBrowseMenu();
    }
}