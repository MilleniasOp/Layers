package entity;

public class Owner extends User{
    public Owner(String username, String password) {
        super(username, password, "director");
    }

    /**
     * Saves the owner to Supabase User table
     * @param username the owner's username
     * @param password the owner's password
     */
}