package boundary.tabs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controller.FinanceController;

import java.awt.*;
import java.util.List;

public class FinanceUI {

    public static JPanel createFinancePanel(String[] args) {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Finance Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton calculateBtn = createStyledButton("Calculate Payroll");
        JButton viewFinancesBtn = createStyledButton("View Financial Summary");

        btnPanel.add(calculateBtn);
        btnPanel.add(viewFinancesBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        // ===== SUMMARY CARDS =====
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        summaryPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        summaryPanel.setBackground(new Color(240, 244, 248));

        JLabel revenueLabel = createSummaryCard("Revenue", "$0.00");
        JLabel payrollLabel = createSummaryCard("Payroll", "$0.00");
        JLabel balanceLabel = createSummaryCard("Balance", "$0.00");

        summaryPanel.add(revenueLabel);
        summaryPanel.add(payrollLabel);
        summaryPanel.add(balanceLabel);

        // ===== TABLE =====
        String[] header = {
            "Employee Name",
            "Completed Tasks",
            "Total Hours",
            "Task Pay",
            "Attendance Pay",
            "Total Pay"
        };

        JTable table = new JTable();
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(new Color(0, 120, 215));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(10, 20, 20, 20));

        // ===== ACTIONS =====

        // Calculate Payroll
        calculateBtn.addActionListener(e -> {
            FinanceController.calculatePayroll();
            refreshTable(table, header);

            JOptionPane.showMessageDialog(mainPanel,
                "Payroll calculated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // View Financial Summary (updates cards)
        viewFinancesBtn.addActionListener(e -> {

            double revenue = FinanceController.CalculateProfit();
            double payroll = FinanceController.getTotalPayroll();
            double expenses = 0; // optional for now

            double balance = revenue - (payroll + expenses);

            revenueLabel.setText(formatCard("Revenue", revenue));
            payrollLabel.setText(formatCard("Payroll", payroll));
            balanceLabel.setText(formatCard("Balance", balance));

            JOptionPane.showMessageDialog(mainPanel,
                "===== FINANCIAL SUMMARY =====\n\n" +
                "Revenue: $" + String.format("%.2f", revenue) + "\n" +
                "Payroll: $" + String.format("%.2f", payroll) + "\n" +
                "Expenses: $" + String.format("%.2f", expenses) + "\n" +
                "-----------------------------\n" +
                "Net Balance: $" + String.format("%.2f", balance),
                "Financial Summary",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // ===== LAYOUT =====
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(240, 244, 248));
        centerPanel.add(summaryPanel, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    // ===== BUTTON STYLE =====
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ===== SUMMARY CARD =====
    private static JLabel createSummaryCard(String title, String value) {
        JLabel label = new JLabel(
            "<html><b>" + title + "</b><br><span style='font-size:16px'>" + value + "</span></html>"
        );
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(new EmptyBorder(15, 15, 15, 15));
        return label;
    }

    private static String formatCard(String title, double value) {
        return "<html><b>" + title + "</b><br><span style='font-size:16px'>$"
                + String.format("%.2f", value) + "</span></html>";
    }

    // ===== TABLE REFRESH =====
    private static void refreshTable(JTable table, String[] header) {

        String payrollJson;

        try {
            payrollJson = FinanceController.fetchPayroll();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        List<String[]> payrollData = FinanceController.parsePayrollJson(payrollJson);

        DefaultTableModel model = new DefaultTableModel(header, 0);

        for (String[] row : payrollData) {
            model.addRow(new Object[]{
                row[0], // Employee Name
                row[1], // Completed Tasks
                row[2], // Hours
                row[3], // Task Pay
                row[4], // Attendance Pay
                row[5]  // Total Pay
            });
        }

        table.setModel(model);
    }
}