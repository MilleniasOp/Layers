package entity;
import java.time.*;

public class Payroll{
    private String username;
    private int tasksCompleted;
    private int totalhours;
    private double taskPay;
    private double attendancePay;
    private double totalPay;
    private String period;

    public Payroll(String username, int tasksCompleted, int totalhours, double taskPay, double attendancePay, double totalPay) {
        this.username = username;
        this.tasksCompleted = tasksCompleted;
        this.totalhours = totalhours;
        this.taskPay = taskPay;
        this.attendancePay = attendancePay;
        this.totalPay = totalPay;
        period = java.time.LocalDate.now().getMonth().toString() + " " + java.time.LocalDate.now().getYear();
    }

    public String getUsername() { return username; }
    public int getTasksCompleted() { return tasksCompleted; }
    public int getTotalHours() { return totalhours; }
    public double getTaskPay() { return taskPay; }
    public double getAttendancePay() { return attendancePay; }
    public double getTotalPay() { return totalPay; }
    public String getPeriod() { return period; }
}