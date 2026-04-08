package boundary.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import controller.Authenticator;
import utils.UIUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import entity.User;

import entity.Customer;
import entity.Employee;
import entity.Owner;

public class AuthenticatorUI {

    private Consumer<Owner> authSuccessCallback;
    private Consumer<Customer> authSuccessCustomerCallback;
    private Consumer<Employee> authSuccessEmployeeCallback;

    public void setAuthSuccessCallback(Consumer<Owner> authSuccessCallback) {
        this.authSuccessCallback = authSuccessCallback;
    }

    public void setAuthSuccessCustomerCallback(Consumer<Customer> authSuccessCustomerCallback) {
        this.authSuccessCustomerCallback = authSuccessCustomerCallback;
    }

    public void setAuthSuccessEmployeeCallback(Consumer<Employee> authSuccessEmployeeCallback) {
        this.authSuccessEmployeeCallback = authSuccessEmployeeCallback;
    }

    public void run() {

        JFrame frame = new JFrame("Authenticator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400); // Increased height to accommodate sign up button
        frame.setLocationRelativeTo(null);

        // Gradient Background Panel
        JPanel background = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color c1 = new Color(58, 123, 213);
                Color c2 = new Color(0, 210, 255);
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new GridBagLayout());

        // Card Panel
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 30, 30));
        card.setBorder(new EmptyBorder(25, 35, 25, 35));

        // Title
        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Color.LIGHT_GRAY);
        JTextField userText = new JTextField();
        styleTextField(userText);

        // Password Panel (for toggle)
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.LIGHT_GRAY);

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passPanel.setBackground(new Color(50, 50, 50));

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        passwordText.setBackground(new Color(50, 50, 50));
        passwordText.setForeground(Color.WHITE);

        JButton toggle = new JButton("👁");
        toggle.setFocusPainted(false);
        toggle.setBorder(null);
        toggle.setBackground(new Color(50, 50, 50));
        toggle.setForeground(Color.WHITE);

        toggle.addActionListener(e -> {
            if (passwordText.getEchoChar() == '\u0000') {
                passwordText.setEchoChar('•');
            } else {
                passwordText.setEchoChar('\u0000');
            }
        });

        passPanel.add(passwordText, BorderLayout.CENTER);
        passPanel.add(toggle, BorderLayout.EAST);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(0, 150, 255));
            }

            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(0, 123, 255));
            }
        });

        // Sign Up Button
        JButton signUpButton = new JButton("Create New Account");
        signUpButton.setFocusPainted(false);
        signUpButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signUpButton.setForeground(new Color(0, 123, 255));
        signUpButton.setBackground(new Color(50, 50, 50));
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        signUpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        signUpButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                signUpButton.setForeground(new Color(0, 150, 255));
            }

            public void mouseExited(MouseEvent e) {
                signUpButton.setForeground(new Color(0, 123, 255));
            }
        });

        // Layout spacing
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        card.add(userLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(userText);

        card.add(Box.createRigidArea(new Dimension(0, 15)));

        card.add(passLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(passPanel);

        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(loginButton);
        
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(signUpButton);

        background.add(card);
        frame.add(background);

        // Login Button Logic
        loginButton.addActionListener(e -> {
            String username = userText.getText().trim();
            String password = new String(passwordText.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                UIUtils.showMessage(frame, "Error", "Please enter both username and password.");
                return;
            }

            Authenticator auth = new Authenticator();
            boolean isAuthenticated = auth.authenticate(username, password);

            if (isAuthenticated) {
                UIUtils.showMessage(frame, "Success", "Login successful!");
                String role = auth.getUserRole(username);

                if ("employee".equals(role) && authSuccessEmployeeCallback != null) {
                    Employee employee = new Employee(username, password);
                    authSuccessEmployeeCallback.accept(employee);
                    frame.dispose();
                }
                if ("customer".equals(role) && authSuccessCustomerCallback != null) {
                    Customer customer = new Customer(username, password);
                    authSuccessCustomerCallback.accept(customer);
                    frame.dispose();
                }
                if ("director".equals(role) && authSuccessCallback != null) {
                    Owner owner = new Owner(username, password);
                    authSuccessCallback.accept(owner);
                    frame.dispose();
                }
            } else {
                UIUtils.showMessage(frame, "Error", "Invalid credentials.");
            }
        });

        // Sign Up Button Logic
        signUpButton.addActionListener(e -> showSignUpDialog(frame));

        frame.setVisible(true);
    }

    // NEW METHOD: Show Sign Up Dialog
    private void showSignUpDialog(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Create New Customer Account", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Title
        JLabel titleLabel = new JLabel("Sign Up as Customer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Username field
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.LIGHT_GRAY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField usernameField = new JTextField(15);
        styleTextField(usernameField);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        // Password field
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.LIGHT_GRAY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField passwordField = new JPasswordField(15);
        stylePasswordField(passwordField);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        // Confirm Password field
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setForeground(Color.LIGHT_GRAY);
        confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField confirmField = new JPasswordField(15);
        stylePasswordField(confirmField);
        confirmField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.setFocusPainted(false);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(0, 123, 255));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(0, 150, 255));
            }
            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(new Color(0, 123, 255));
            }
        });
        
        // Cancel Button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelButton.setForeground(Color.LIGHT_GRAY);
        cancelButton.setBackground(new Color(50, 50, 50));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> dialog.dispose());
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 30, 30));
        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(cancelButton);
        
        // Add components
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(userLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(passLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(confirmLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(confirmField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(buttonPanel);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        
        // Register button logic
        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmField.getPassword());
            
            // Validation
            if (username.isEmpty()) {
                UIUtils.showMessage(dialog, "Error", "Please enter a username.");
                return;
            }
            
            if (password.isEmpty()) {
                UIUtils.showMessage(dialog, "Error", "Please enter a password.");
                return;
            }
            
            if (password.length() < 4) {
                UIUtils.showMessage(dialog, "Error", "Password must be at least 4 characters long.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                UIUtils.showMessage(dialog, "Error", "Passwords do not match.");
                return;
            }
            
            Authenticator auth = new Authenticator();
            
            // Check if username exists
            if (auth.usernameExists(username)) {
                UIUtils.showMessage(dialog, "Error", "Username already exists. Please choose another.");
                return;
            }
            
            // Register the customer
            boolean success = auth.registerCustomer(username, password);
            
            if (success) {
                UIUtils.showMessage(dialog, "Success", "Account created successfully! You can now log in.");
                dialog.dispose();
                // Optionally auto-fill the username in login form
                usernameField.getParent().getParent().getParent().getParent();
            } else {
                UIUtils.showMessage(dialog, "Error", "Failed to create account. Please try again.");
            }
        });
        
        dialog.setVisible(true);
    }

    // Styled Text Field
    private static void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
    }
    
    private static void stylePasswordField(JPasswordField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
    }
}