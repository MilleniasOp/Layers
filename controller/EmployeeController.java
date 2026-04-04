package controller;
import controller.AttendanceController;

public class EmployeeController {

    AttendanceController attendanceController = new AttendanceController();

    public void clockIn(String username) {
        attendanceController.clockIn(username);
    }

    public void clockOut(String username) {
        attendanceController.clockOut(username);
    }

}
