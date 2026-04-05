package boundary.tabs;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controller.TaskController;
import entity.Task;
import entity.User;

import java.awt.*;
import java.util.List;

public class EmployeeTaskUI {
    private static User user;
    
    public static JPanel createTaskPanel(User user) {
        EmployeeTaskUI.user = user;

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 244, 248));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Task Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton updateStatusBtn = createStyledButton("✏️ Update Status");
        JButton viewTasksBtn = createStyledButton("👀 View Tasks");
        JButton refreshBtn = createStyledButton("🔄 Refresh");
        btnPanel.add(updateStatusBtn);
        btnPanel.add(viewTasksBtn);
        btnPanel.add(refreshBtn);
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);

        String[] header = {"Task ID", "Description", "Assigned To", "Status", "Date"};
        JTable table = new JTable();
        table.setRowHeight(25);


        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ACTIONS
        viewTasksBtn.addActionListener(e -> refreshTable(table, header));

        updateStatusBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select a task to update.");
                return;
            }
            String taskId = table.getValueAt(selectedRow, 0).toString();
            String currentStatus = table.getValueAt(selectedRow, 3).toString();
            showUpdateStatusDialog(taskId, currentStatus);
            refreshTable(table, header);
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
        btn.setHorizontalAlignment(SwingConstants.CENTER); // ✅ center text+emoji horizontally
        btn.setVerticalAlignment(SwingConstants.CENTER);   // ✅ center vertically
        return btn;
    }

    private static void refreshTable(JTable table, String[] header) {
        List<Task> list = TaskController.getTasksFromSupabase(EmployeeTaskUI.user);
        Object[][] data = new Object[list.size()][header.length];
        for (int i = 0; i < list.size(); i++) {
            Task t = list.get(i);
            data[i][0] = t.getTaskId();
            data[i][1] = t.getDescription();
            data[i][2] = t.getAssignedTo();
            data[i][3] = t.getStatus();
            data[i][4] = t.getCreatedDate();
        }
        DefaultTableModel model = new DefaultTableModel(data, header);
        table.setModel(model);
    }

    private static void showUpdateStatusDialog(String taskId, String currentStatus) {
        String[] options = {"Pending", "In Progress", "Completed"};
        String newStatus = (String) JOptionPane.showInputDialog(null, "Select new status for Task ID: " + taskId,
                "Update Task Status", JOptionPane.QUESTION_MESSAGE, null, options, currentStatus);
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            TaskController.updateTaskStatus(taskId, newStatus);
            JOptionPane.showMessageDialog(null, "Task status updated successfully!");
        }
    }

}       
