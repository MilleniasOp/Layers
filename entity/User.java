package entity;
public class User {
    private final String username;
    private final String password;
    private final String role;
    private final String userId;


    public User(String username, String password, String role, String userId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getRole(){ 
        return role;
    }
    public String getUserId(){
        return userId;
    }
}
