package entity;

public class Owner extends User{
    public Owner(String username, String password, String role, String userId) {
        super(username, password, "director", userId);
    }

    /**
     * Saves the owner to Supabase User table
     * @param username the owner's username
     * @param password the owner's password
     */
}