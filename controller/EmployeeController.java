package controller;
import controller.AttendanceController;
import controller.TaskController;
import entity.Task;
import entity.Employee;
import entity.User;
import java.util.List;

public class EmployeeController {

    AttendanceController attendanceController = new AttendanceController();

    public void clockIn(String username) {
        attendanceController.clockIn(username);
    }

    public void clockOut(String username) {
        attendanceController.clockOut(username);
    }

    //Viewing tasks assigned to employee
    public List<Task> viewTasksAssigned (User employee){
        return TaskController.getTasksFromSupabase(employee);
    }

    //Updating the status of tasks
    public void updateTaskStatus (Task task, Employee employee, String newStatus){
        TaskController.updateTask (task,employee, newStatus);
    }

    public String taskReminder(User employee) throws Exception {

        StringBuilder pendingTasks = new StringBuilder();

        for (Task task : viewTasksAssigned(employee)) {
            if (task.getStatus().equals("Pending") || task.getStatus().equals("In Progress")) {
                pendingTasks.append(" - ").append(task.getDescription()).append("\n");
            }
        }

        String taskList = pendingTasks.toString();
        System.out.println(taskList);
        if (taskList.length() == 0) {
            return "No reminders.";
        } else {
            return taskList;
        }
        
    }
}
