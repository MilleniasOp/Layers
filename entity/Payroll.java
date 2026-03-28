package entity;
public class Payroll{
    private String username;
    private int tasksCompleted;
    private double pay;

    public Payroll(String username, int tasksCompleted, double pay) {
        this.username = username;
        this.tasksCompleted = tasksCompleted;
        this.pay = pay;
    }

    public String getUsername() { return username; }
    public int getTasksCompleted() { return tasksCompleted; }
    public double getPay() { return pay; }
}