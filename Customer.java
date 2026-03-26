import java.util.List;
import java.util.ArrayList;

public class Customer extends User {
    private List<Orders> orderHistory;

    public Customer(String username, String password) {
        super(username, password, "customer");
        this.orderHistory = new ArrayList<>();
    }

    /**
     * Browse the menu (list of available products)
     * @param menu the list of available products
     */
    public void browseMenu(List<Products> menu) {
        System.out.println("=== MENU ===");
        for (Products product : menu) {
            System.out.println(product);
        }
    }

    /**
     * Create a new order
     * @param items list of products to order
     * @return the created order
     */
    public Orders createOrder(List<Products> items) {
        Orders newOrder = new Orders();
        for (Products item : items) {
            newOrder.addItem(item);
        }
        orderHistory.add(newOrder);
        return newOrder;
    }

    /**
     * View order details by order ID
     * @param orderId the ID of the order to view
     * @return order details as string, or null if not found
     */
    public String viewOrderDetails(String orderId) {
        for (Orders order : orderHistory) {
            if (order.getOrderId().equals(orderId)) {
                return order.toString();
            }
        }
        return "Order not found.";
    }

    /**
     * Get all orders for this customer
     * @return list of customer's orders
     */
    public List<Orders> getOrderHistory() {
        return orderHistory;
    }

    /**
     * Get the most recent order
     * @return the latest order, or null if no orders
     */
    public Orders getLatestOrder() {
        if (orderHistory.isEmpty()) {
            return null;
        }
        return orderHistory.get(orderHistory.size() - 1);
    }
}
