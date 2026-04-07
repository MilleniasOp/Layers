package entity;
import java.util.List;
import java.util.ArrayList;

public class Customer extends User {
    class Order {};
    private List<Order> orderHistory;

    public Customer(String username, String password, String role, String userId) {
        super(username, password, "customer", userId);
        this.orderHistory = new ArrayList<>();
    }

}
