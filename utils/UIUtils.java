package utils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIUtils {

    // ===== COLORS =====
    private static final Color PRIMARY = new Color(0, 123, 255);
    private static final Color BG = new Color(245, 247, 250);

    // ===== MESSAGE DIALOG =====
    public static void showMessage(Component parent, String title, String message) {
        JDialog dialog = createBaseDialog(parent, title);

        JLabel msg = new JLabel(message, SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton okBtn = createButton("OK");
        okBtn.addActionListener(e -> dialog.dispose());

        dialog.add(msg, BorderLayout.CENTER);
        dialog.add(centerPanel(okBtn), BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ===== CONFIRM DIALOG =====
    public static boolean showConfirm(Component parent, String title, String message) {
        JDialog dialog = createBaseDialog(parent, title);

        final boolean[] result = {false};

        JLabel msg = new JLabel(message, SwingConstants.CENTER);

        JButton yes = createButton("Yes");
        JButton no = createButton("No");

        yes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        no.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(BG);
        btnPanel.add(yes);
        btnPanel.add(no);

        dialog.add(msg, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
        return result[0];
    }

    // ===== INPUT DIALOG =====
    public static String showInput(Component parent, String title, String labelText) {
        JDialog dialog = createBaseDialog(parent, title);

        JTextField input = new JTextField(15);

        JPanel center = new JPanel(new GridLayout(2, 1, 5, 5));
        center.setBackground(BG);
        center.add(new JLabel(labelText));
        center.add(input);

        final String[] result = {null};

        JButton ok = createButton("Submit");
        ok.addActionListener(e -> {
            result[0] = input.getText();
            dialog.dispose();
        });

        dialog.add(center, BorderLayout.CENTER);
        dialog.add(centerPanel(ok), BorderLayout.SOUTH);

        dialog.setVisible(true);
        return result[0];
    }

    // ===== BASE DIALOG =====
    private static JDialog createBaseDialog(Component parent, String title) {
        Frame owner = null;

        if (parent != null) {
            owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        }

        JDialog dialog = new JDialog(
                owner,      // if owner is null, dialog is still modal
                title,
                true
        );

        dialog.setSize(350, 200);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG);
        dialog.setLocationRelativeTo(parent);

        JLabel header = new JLabel(title, SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        dialog.add(header, BorderLayout.NORTH);

        return dialog;
    }
    // ===== BUTTON STYLE =====
    private static JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    private static JPanel centerPanel(JButton btn) {
        JPanel panel = new JPanel();
        panel.setBackground(BG);
        panel.add(btn);
        return panel;
    }
}