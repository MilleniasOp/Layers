package entity;

public class Employee extends User {
    public Employee(String username, String password) {
        super(username, password, "employee");
    }
}