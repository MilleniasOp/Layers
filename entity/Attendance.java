package entity;

public class Attendance {
    private String id;
    private String username;
    private String clockIn;
    private String clockOut;

    public Attendance(String id, String username, String clockIn, String clockOut) {
        this.id = id;
        this.username = username;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getClockIn() { return clockIn; }
    public String getClockOut() { return clockOut; }

    public void setClockOut(String clockOut) {
        this.clockOut = clockOut;
    }
}