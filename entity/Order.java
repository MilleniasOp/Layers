package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.util.HashMap;
//import java.util.Map;

public class Order {
 
    public enum Status {PENDING, CONFIRMED, CANCELLED}
    
    public static class OrderItem {
        private final MenuItem menuItem;
        private int quantity;
        
        public OrderItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }
        
        public MenuItem getMenuItem() { return menuItem; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getSubtotal() { return menuItem.getPrice() * quantity; }
    }
 
    private final String orderId;
    private final String username;
    private final List<OrderItem> items;
    private Status status;
    private double totalPrice;
    private String placedAt;
 
    public Order(String orderId, String username) {
        this.orderId    = orderId;
        this.username   = username;
        this.items      = new ArrayList<>();
        this.status     = Status.PENDING;
        this.totalPrice = 0.0;
        this.placedAt   = "";
    }
    
    public void addItem(MenuItem item, int quantity) {
        for (OrderItem orderItem : items) {
            if (orderItem.getMenuItem().getItemId().equals(item.getItemId())) {
                orderItem.setQuantity(orderItem.getQuantity() + quantity);
                totalPrice += item.getPrice() * quantity;
                return;
            }
        }
        items.add(new OrderItem(item, quantity));
        totalPrice += item.getPrice() * quantity;
    }
    
    public void addItem(MenuItem item) {
        addItem(item, 1);
    }
 
    public String getOrderId()    { return orderId; }
    public String getUsername()   { return username; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Status getStatus()     { return status; }
    public double getTotalPrice() { return totalPrice; }
    public String getPlacedAt()   { return placedAt; }
 
    public void setStatus(Status status)    { this.status = status; }
    public void setTotalPrice(double total) { this.totalPrice = total; }
    public void setPlacedAt(String placedAt){ this.placedAt = placedAt; }
 
    @Override
    public String toString() {
        return String.format("Order[%s] %s | %s | $%.2f",
                orderId, username, status, totalPrice);
    }
}