package entity;

//import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
 
    public enum Status {PENDING, CONFIRMED, CANCELLED}
 
    private final String         orderId;
    private final String         username;
    private final List<MenuItem> items;
    private       Status         status;
    private       double         totalPrice;
    private       String         placedAt;
 
    public Order(String orderId, String username) {
        this.orderId    = orderId;
        this.username   = username;
        this.items      = new ArrayList<>();
        this.status     = Status.PENDING;
        this.totalPrice = 0.0;
        this.placedAt   = "";
    }
 
    public void addItem(MenuItem item) {
        items.add(item);
        totalPrice += item.getPrice();
    }
 
    public String             getOrderId()    { return orderId; }
    public String             getUsername()   { return username; }
    public List<MenuItem>     getItems()      { return Collections.unmodifiableList(items); }
    public Status             getStatus()     { return status; }
    public double             getTotalPrice() { return totalPrice; }
    public String             getPlacedAt()   { return placedAt; }
 
    public void setStatus(Status status)    { this.status = status; }
    public void setTotalPrice(double total) { this.totalPrice = total; }
    public void setPlacedAt(String placedAt){ this.placedAt = placedAt; }
 
    @Override
    public String toString() {
        return String.format("Order[%s] %s | %s | $%.2f",
                orderId, username, status, totalPrice);
    }
}
