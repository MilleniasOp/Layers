package entity;

public class Owner extends User{
    public Owner(String username, String password) {
        super(username, password, "director");
    }
}