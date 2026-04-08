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
import entity.User;

public class AuthenticatorUI {

    private Consumer<String> authSuccessCallback;
    private Consumer<User> authSuccessCustomerCallback;

    public void setAuthSuccessCallback(Consumer<User> authSuccessCallback) {
        this.authSuccessCallback = authSuccessCallback;
    }

    public void setAuthSuccessCustomerCallback(Consumer<User> authSuccessCustomerCallback) {
        this.authSuccessCustomerCallback = authSuccessCustomerCallback;
    }

    public void run() {

        JFrame frame = new JFrame("Authenticator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);
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

        background.add(card);
        frame.add(background);

        // Button Logic
        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());

            Authenticator auth = new Authenticator();
            boolean isAuthenticated = auth.authenticate(username, password);


            if (isAuthenticated) {
                UIUtils.showMessage(frame, "Success", "Login successful!");
                String role = auth.getUserRole(username);
                User user = new User(username, password, role);
                this.authUsername = username; // Store authenticated username

                if (authSuccessCallback != null) {
                    authSuccessCallback.accept(user);
                if ("customer".equals(role) && authSuccessCustomerCallback != null) {
                    Customer customer = new Customer(username, password);
                    authSuccessCustomerCallback.accept(customer);
                }

                else if(authSuccessCallback != null) {
                    authSuccessCallback.accept(role);
                }

                frame.setVisible(false);
                

            } else {
                UIUtils.showMessage(frame, "Error", "Invalid credentials.");
            }
        });

        frame.setVisible(true);
    }

    // Styled Text Field
    private static void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
    }

    public String getAuthenticatedUsername() {
        return authUsername;
    }
    
}