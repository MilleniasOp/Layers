package controller;

import entity.MenuItem;
import entity.Order;
import utils.SupabaseClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NewOrderController {

    private final BrowseMenuController browseMenuController;

    public NewOrderController() {
        this.browseMenuController = new BrowseMenuController();
    }

    public List<MenuItem> getAvailableItems() throws IOException, InterruptedException {
        return browseMenuController.retrieveAvailableItems();
    }

    public Order buildOrder(String username, List<String> selectedItemIds)
            throws IOException, InterruptedException {

        List<MenuItem> allItems = browseMenuController.retrieveMenuItems();
        Order order = new Order(UUID.randomUUID().toString(), username);

        // Count occurrences of each item ID
        java.util.Map<String, Integer> quantityMap = new java.util.HashMap<>();
        for (String id : selectedItemIds) {
            quantityMap.put(id, quantityMap.getOrDefault(id, 0) + 1);
        }

        // Add items with quantities
        for (java.util.Map.Entry<String, Integer> entry : quantityMap.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            
            allItems.stream()
                    .filter(i -> i.getItemId().equals(itemId) && i.isAvailable())
                    .findFirst()
                    .ifPresent(item -> order.addItem(item, quantity));
        }
        return order;
    }

    public Order confirmOrder(Order order) throws IOException, InterruptedException {
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with no items.");
        }

        order.setStatus(Order.Status.PENDING);

        // 1. Save the order
        String orderJson = String.format(
                "{\"order_id\":\"%s\",\"username\":\"%s\",\"status\":\"PENDING\",\"total_price\":%.2f}",
                order.getOrderId(), order.getUsername(), order.getTotalPrice());

        System.out.println("Saving order: " + orderJson);
        
        var orderResponse = SupabaseClient.Tables.ORDERS_TABLE.post(
                orderJson,
                Map.of("Prefer", "return=representation"));
        
        System.out.println("Order save response status: " + orderResponse.statusCode());
        System.out.println("Order save response body: " + orderResponse.body());

        // 2. Save each order item with quantity
        for (Order.OrderItem orderItem : order.getItems()) {
            MenuItem item = orderItem.getMenuItem();
            int quantity = orderItem.getQuantity();
            
            String itemJson = String.format(
                    "{\"order_id\":\"%s\",\"item_id\":\"%s\",\"quantity\":%d}",
                    order.getOrderId(), 
                    item.getItemId(), 
                    quantity);

            System.out.println("Saving order item: " + itemJson);
            
            var itemResponse = SupabaseClient.Tables.ORDER_ITEMS_TABLE.post(
                    itemJson,
                    Map.of("Prefer", "return=representation"));
            
            System.out.println("Order item save response status: " + itemResponse.statusCode());
            System.out.println("Order item save response body: " + itemResponse.body());
        }

        return order;
    }
}