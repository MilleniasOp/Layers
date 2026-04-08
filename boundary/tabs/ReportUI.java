package boundary.tabs;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controller.ReportController;
import controller.UserController;
import entity.Report;
import utils.UIUtils;

import java.awt.*;
import java.util.List;

public class ReportUI {

    public static JPanel createReportsPanel(String[] args) {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Employee Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton generateBtn = createStyledButton("📊 Generate");
        JButton refreshBtn = createStyledButton("🔄 Refresh");

        btnPanel.add(generateBtn);
        btnPanel.add(refreshBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        // ===== TABLE =====
        String[] header = {"Username", "Role"};
        JTable table = new JTable();
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        refreshTable(table, header);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ===== ACTIONS =====
        generateBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow != -1) {

                String username = (String) table.getValueAt(selectedRow, 0);
                List<Report> reports = ReportController.generateReports(username);

                // ===== MODERN DIALOG =====
                JDialog dialog = new JDialog(
                        (Frame) SwingUtilities.getWindowAncestor(mainPanel),
                        "Employee Report: " + username,
                        true
                );
                dialog.setSize(700, 450);
                dialog.setLayout(new BorderLayout());

                // TOP SUMMARY
                JPanel top = new JPanel(new GridLayout(2, 1));
                top.setBorder(new EmptyBorder(15, 10, 15, 10));
                top.setBackground(Color.WHITE);

                JLabel userLabel = new JLabel("Report for: " + username, SwingConstants.CENTER);
                userLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

                JLabel totalLabel = new JLabel("Total Tasks: " + reports.size(), SwingConstants.CENTER);
                totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                top.add(userLabel);
                top.add(totalLabel);

                // TABLE DATA
                String[] columns = {"Report ID", "Task ID", "Description", "Status", "Created Date"};
                String[][] tableData = new String[reports.size()][5];

                for (int i = 0; i < reports.size(); i++) {
                    Report r = reports.get(i);

                    tableData[i][0] = r.getReportID();
                    tableData[i][1] = r.getTaskId();
                    tableData[i][2] = r.getDescription();
                    tableData[i][3] = r.getStatus();
                    tableData[i][4] = r.getCreatedDate();
                }

                JTable reportTable = new JTable(tableData, columns);
                reportTable.setRowHeight(25);

                JScrollPane tableScroll = new JScrollPane(reportTable);

                // BOTTOM BUTTON
                JButton closeBtn = createStyledButton("Close");
                closeBtn.addActionListener(ev -> dialog.dispose());

                JPanel bottom = new JPanel();
                bottom.setBackground(Color.WHITE);
                bottom.add(closeBtn);

                // ADD TO DIALOG
                dialog.add(top, BorderLayout.NORTH);
                dialog.add(tableScroll, BorderLayout.CENTER);
                dialog.add(bottom, BorderLayout.SOUTH);

                dialog.setLocationRelativeTo(mainPanel);
                dialog.setVisible(true);

            } else {
                UIUtils.showMessage(mainPanel, "Warning", "Select an employee first.");
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
        btn.setHorizontalAlignment(SwingConstants.CENTER); 
        btn.setVerticalAlignment(SwingConstants.CENTER);   
        return btn;
    }

    private static void refreshTable(JTable table, String[] header) {
        String data = UserController.fetchEmployees();
        List<String[]> list = UserController.parseEmployeesJson(data);
        table.setModel(new DefaultTableModel(list.toArray(new String[0][]), header));
    }
}