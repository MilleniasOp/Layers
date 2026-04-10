package boundary.pages;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import boundary.tabs.EmployeeTaskUI;
import boundary.tabs.OrderUI;
import boundary.tabs.TaskUI;
import controller.EmployeeController;
import entity.Employee;
import entity.User;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EmployeeDashBoardUI {

    EmployeeController employeeController = new EmployeeController();
    private static JPanel contentPanel;
    private String username;
    private static String reminders;

    public void run(User user, AuthenticatorUI authUI) {
        this.username = user.getUsername();
        employeeController.clockIn(username);
        EventQueue.invokeLater(() -> { try {
            reminders = employeeController.taskReminder(user);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Failed to send task reminder notification");
        }});


        JFrame frame = new JFrame("Employee Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 550);
        frame.setLocationRelativeTo(null);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 30, 30));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight()));

        JLabel title = new JLabel("Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton taskBtn = createSidebarButton("My Tasks");
        JButton OrderBtn = createSidebarButton("Orders");

        sidebar.add(title);
        sidebar.add(taskBtn);
        sidebar.add(OrderBtn);


        // Top bar
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Color.WHITE);
        topbar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel welcome = new JLabel("Welcome, Employee");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);

        topbar.add(welcome, BorderLayout.WEST);
        topbar.add(logoutBtn, BorderLayout.EAST);

        // Content panel (changes dynamically)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 244, 248));

        JLabel reminderLabel = new JLabel("Reminders: Loading...");
        reminderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JPanel homePanel = new JPanel(new GridBagLayout());
        homePanel.setBackground(new Color(240, 244, 248));
        homePanel.add(reminderLabel);

        contentPanel.add(homePanel, BorderLayout.CENTER);

        EventQueue.invokeLater(() -> {
            try {
                reminders = employeeController.taskReminder(user);
                reminderLabel.setText("Reminders:" + reminders);
            } catch (Exception e) {
                e.printStackTrace();
                reminderLabel.setText("Reminders: Failed to load");
            }
        });

        // Layout assembly
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(topbar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        // Navigation logic
        taskBtn.addActionListener(e -> switchPanel(createPage("Task Management", "Open Task UI", () -> {
            switchPanel(EmployeeTaskUI.createTaskPanel(user));
        })));

        OrderBtn.addActionListener(e -> switchPanel(createPage("Order Management", "Open Order UI", () -> {
            switchPanel(OrderUI.createOrderPanel(user));
        })));

        logoutBtn.addActionListener(e -> {
            frame.dispose();
            employeeController.clockOut(username);
            authUI.run();
        });
    }

    // Sidebar button style
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

    // Switch center content
    private static void switchPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Generic page layout
    private static JPanel createPage(String title, String buttonText, Runnable action) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 244, 248));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton actionBtn = new JButton(buttonText);
        actionBtn.setFocusPainted(false);
        actionBtn.setBackground(new Color(0, 123, 255));
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionBtn.addActionListener(e -> action.run());

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(actionBtn);

        return panel;
    }
}