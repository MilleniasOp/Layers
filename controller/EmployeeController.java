package controller;
import controller.AttendanceController;
import controller.TaskController;
import entity.Task;
import entity.Employee;
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
    public List<Task> viewTasksAssigned (Employee employee){
        return TaskController.getTasksFromSupabase(employee);
    }

    //Updating the status of tasks
    public void updateTaskStatus (Task task, Employee employee, String newStatus){
        TaskController.updateTask (task,employee, newStatus);
    }

}
