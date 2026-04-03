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

        btnPanel.add(calculateBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        // ===== TABLE =====
        String[] header = {"Employee Name", "Completed Tasks", "Total Hours", "Task Pay", "Attendance Pay","Total Pay"};
        JTable table = new JTable();
        table.setRowHeight(25);


        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ===== ACTIONS =====
        calculateBtn.addActionListener(e -> {
            FinanceController.calculatePayroll();
            refreshTable(table, header);
        });

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);

        return mainPanel;
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return button;
    }

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
                row[0],
                row[1],
                row[2],
                row[3],
                row[4],
                row[5]
            });
        }

        table.setModel(model);
    }    
}
