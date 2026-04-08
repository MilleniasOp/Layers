package boundary.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import boundary.tabs.ProductUI;
import boundary.tabs.RecipeUI;
import boundary.tabs.ReportUI;
import boundary.tabs.TaskUI;
import boundary.tabs.UserUI;
import controller.AlertController;
import boundary.tabs.FinanceUI;

public class OwnerDashBoardUI {

    private static JPanel contentPanel;

    public void run() {

        JFrame frame = new JFrame("Director Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 550);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // ================= SIDEBAR =================
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 30, 30));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight()));

        JLabel title = new JLabel("Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton recipeBtn = createSidebarButton("Create Recipe");
        JButton productBtn = createSidebarButton("Create Product");
        JButton taskBtn = createSidebarButton("Create Task");
        JButton reportBtn = createSidebarButton("Reports");
        JButton userBtn = createSidebarButton("Create User");
        JButton financeBtn = createSidebarButton("Finance");

        sidebar.add(title);
        sidebar.add(recipeBtn);
        sidebar.add(productBtn);
        sidebar.add(taskBtn);
        sidebar.add(reportBtn);
        sidebar.add(userBtn);
        sidebar.add(financeBtn);

        // ================= TOPBAR =================
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Color.WHITE);
        topbar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel welcome = new JLabel("Welcome, Director");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);

        JPanel notificationPanel = new JPanel();
        notificationPanel.setLayout(null); // absolute positioning
        notificationPanel.setPreferredSize(new Dimension(40, 30));
        notificationPanel.setOpaque(false);

        JButton notificationBtn = new JButton("🔔");
        notificationBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        notificationBtn.setFocusPainted(false);
        notificationBtn.setBorderPainted(false);
        notificationBtn.setContentAreaFilled(false);
        notificationBtn.setBounds(0, 0, 40, 30);

        JLabel badgeLabel = new JLabel("0");
        badgeLabel.setForeground(Color.RED);
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 8));
        badgeLabel.setOpaque(true);
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // small circle look
        badgeLabel.setBounds(18, 0, 16, 16);

        // add both
        notificationPanel.add(notificationBtn);
        notificationPanel.add(badgeLabel);
        
        JButton logoutBtn = new JButton("Logout");

        rightPanel.add(notificationPanel);
        rightPanel.add(logoutBtn);

        topbar.add(welcome, BorderLayout.WEST);
        topbar.add(rightPanel, BorderLayout.EAST);

        // ================= CONTENT =================
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 244, 248));
        contentPanel.add(createHomePanel(), BorderLayout.CENTER);

        // ================= NOTIFICATION DROPDOWN =================
        JPanel dropdownPanel = new JPanel(new BorderLayout());
        dropdownPanel.setBackground(Color.WHITE);
        dropdownPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        dropdownPanel.setBounds(550, 50, 300, 250);
        dropdownPanel.setVisible(false);

        JLabel header = new JLabel("Notifications");
        header.setBorder(new EmptyBorder(10, 10, 5, 10));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextArea alertArea = new JTextArea();
        alertArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(alertArea);

        dropdownPanel.add(header, BorderLayout.NORTH);
        dropdownPanel.add(scroll, BorderLayout.CENTER);

        // ================= LAYERED PANE =================
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        contentPanel.setBounds(0, 0, 900, 500);
        layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(dropdownPanel, JLayeredPane.POPUP_LAYER);

        // ================= ADD TO FRAME =================
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(topbar, BorderLayout.NORTH);
        mainPanel.add(layeredPane, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        // ================= NAVIGATION =================
        recipeBtn.addActionListener(e ->
                switchPanel(RecipeUI.createRecipePanel(null)));

        productBtn.addActionListener(e ->
                switchPanel(ProductUI.createProductPanel(null)));

        taskBtn.addActionListener(e ->
                switchPanel(TaskUI.createTaskPanel(null)));

        reportBtn.addActionListener(e ->
                switchPanel(ReportUI.createReportsPanel(null)));

        userBtn.addActionListener(e ->
                switchPanel(UserUI.createUserPanel(null)));

        financeBtn.addActionListener(e ->
                switchPanel(FinanceUI.createFinancePanel(null)));

        // ================= NOTIFICATIONS =================
        notificationBtn.addActionListener(e -> {
            dropdownPanel.setVisible(!dropdownPanel.isVisible());

            if (dropdownPanel.isVisible()) {
                loadAlerts(alertArea);
                AlertController.markAllAsRead();
            }
        });

        // Auto badge refresh
        new Timer(5000, e -> {
            int count = AlertController.getUnreadCount();
            badgeLabel.setText(String.valueOf(count));
        }).start();

        // Close dropdown when clicking outside
        mainPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dropdownPanel.setVisible(false);
            }
        });

        // Logout
        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new AuthenticatorUI().run();
        });
    }

    // ================= HELPERS =================

    private static JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(30, 30, 30));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 50, 50));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(30, 30, 30));
            }
        });

        return btn;
    }

    private static void switchPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private static JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 244, 248));

        JLabel label = new JLabel("Select an option from the sidebar");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        panel.add(label);
        return panel;
    }

    // ================= ALERT LOADER =================

    private static void loadAlerts(JTextArea area) {
        try {
            String json = AlertController.fetchUnreadAlerts();
            var alerts = AlertController.parseAlerts(json);

            area.setText("");

            for (var alert : alerts) {
                area.append("🔔 " + alert.get("message") + "\n");
            }

            if (alerts.isEmpty()) {
                area.setText("No new alerts");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}