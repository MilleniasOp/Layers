package controller;

import entity.Product;
import entity.Order;
import utils.SupabaseClient;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderController {

    private final BrowseMenuController browseMenuController;

    public OrderController() {
        this.browseMenuController = new BrowseMenuController();
    }

    // ORDER PLACEMENT
    
    public List<Product> getAvailableItems() throws IOException, InterruptedException {
        return browseMenuController.retrieveAvailableItems();
    }

    public Order buildOrder(String username, List<String> selectedItemIds)
            throws IOException, InterruptedException {

        List<Product> allItems = browseMenuController.retrieveMenuItems();
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
             Product item = orderItem.getMenuItem();
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

    // ORDER CANCELLATION

    public List<Order> fetchActiveOrdersForUser(String username)
            throws IOException, InterruptedException {

        String encodedUsername = SupabaseClient.encodeValue(username);
        HttpResponse<String> response =
                SupabaseClient.Tables.ORDERS_TABLE.get(
                        "?username=eq." + encodedUsername
                        + "&status=eq.PENDING"
                        + "&select=*"
                        + "&order=placed_at.desc",
                        null);

        return parseOrders(response.body());
    }

    public String cancelOrder(String orderId, String username)
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                SupabaseClient.Tables.ORDERS_TABLE.get(
                        "?order_id=eq." + orderId + "&select=*&limit=1", null);

        String body = response.body();
        if (body == null || body.equals("[]") || body.isBlank()) {
            return "ERROR: Order not found.";
        }

        String ownerUsername = BrowseMenuController.extractString(body, "username");
        String status        = BrowseMenuController.extractString(body, "status");
        String placedAt      = BrowseMenuController.extractString(body, "placed_at");

        if (!ownerUsername.equals(username)) {
            return "ERROR: You can only cancel your own orders.";
        }
        if ("CONFIRMED".equals(status)) {
            return "ERROR: Confirmed orders cannot be cancelled.";
        }
        if ("CANCELLED".equals(status)) {
            return "ERROR: This order is already cancelled.";
        }
        if (!isWithinWindow(placedAt)) {
            return "Order can no longer be cancelled (5-minute window has passed).";
        }

        SupabaseClient.Tables.ORDERS_TABLE.patch(
                "?order_id=eq." + orderId,
                "{\"status\":\"CANCELLED\"}",
                Map.of("Prefer", "return=minimal"));

        return "SUCCESS: Order " + orderId + " has been cancelled.";
    }

    private boolean isWithinWindow(String placedAtStr) {
        try {
            LocalDateTime placed = OffsetDateTime.parse(placedAtStr).toLocalDateTime();
            return LocalDateTime.now().isBefore(placed.plusMinutes(5));
        } catch (Exception e) {
            return false;
        }
    }

    

    // ORDER DETAILS 

    public List<Order> getOrderHistory(String username)
            throws IOException, InterruptedException {

        String encodedUsername = SupabaseClient.encodeValue(username);
        HttpResponse<String> response =
                SupabaseClient.Tables.ORDERS_TABLE.get(
                        "?username=eq." + encodedUsername
                        + "&select=*"
                        + "&order=placed_at.desc",
                        null);

        return parseOrders(response.body());
    }

    public Order getOrderDetails(String orderId, String username)
            throws IOException, InterruptedException {

        // First, get the order details
        HttpResponse<String> response =
                SupabaseClient.Tables.ORDERS_TABLE.get(
                        "?order_id=eq." + orderId + "&select=*&limit=1",
                        null);

        String body = response.body();
        if (body == null || body.equals("[]") || body.isBlank()) return null;

        // Remove outer array brackets
        String orderObj = body.trim();
        if (orderObj.startsWith("[") && orderObj.endsWith("]")) {
            orderObj = orderObj.substring(1, orderObj.length() - 1);
        }
        
        if (orderObj.isBlank()) return null;

        // Check if the order belongs to this user
        if (!orderObj.contains("\"username\":\"" + username + "\"")) return null;

        String status = BrowseMenuController.extractString(orderObj, "status");
        double total = BrowseMenuController.extractDouble(orderObj, "total_price");
        String placedAt = BrowseMenuController.extractString(orderObj, "placed_at");

        Order order = new Order(orderId, username);
        try { 
            order.setStatus(Order.Status.valueOf(status)); 
        } catch (IllegalArgumentException ignored) {}
        order.setTotalPrice(total);
        order.setPlacedAt(placedAt);

        // Now get the order items with quantities
        HttpResponse<String> itemsResponse =
                SupabaseClient.Tables.ORDER_ITEMS_TABLE.get(
                        "?order_id=eq." + orderId + "&select=item_id,quantity",
                        null);
        
        String itemsBody = itemsResponse.body();
        
        if (itemsBody != null && !itemsBody.equals("[]") && !itemsBody.isBlank()) {
            // Parse the order items
            String itemsJson = itemsBody.trim();
            if (itemsJson.startsWith("[") && itemsJson.endsWith("]")) {
                itemsJson = itemsJson.substring(1, itemsJson.length() - 1);
            }
            
            if (!itemsJson.isBlank()) {
                // Split into individual item objects
                String[] itemObjects = itemsJson.split("\\},\\s*\\{");
                for (String itemObj : itemObjects) {
                    // Clean up the item object to ensure proper JSON format
                    if (!itemObj.startsWith("{")) itemObj = "{" + itemObj;
                    if (!itemObj.endsWith("}")) itemObj = itemObj + "}";
                    
                    String itemId = BrowseMenuController.extractString(itemObj, "item_id");
                    int quantity = (int) BrowseMenuController.extractDouble(itemObj, "quantity");
                    
                    if (quantity > 0) {
                        // Get full menu item details
                        Product fullItem = getMenuItemDetails(itemId);
                        if (fullItem != null) {
                            order.addItem(fullItem, quantity);
                        } else {
                            // Fallback: create a basic menu item if details can't be fetched
                            Product fallbackItem = new Product(
                                itemId, 
                                "Unknown Item (ID: " + itemId + ")", 
                                "Item details not found", 
                                0.0, 
                                true
                            );
                            order.addItem(fallbackItem, quantity);
                        }
                    }
                }
            }
        }

        return order;
    }

    private Product getMenuItemDetails(String itemId) 
            throws IOException, InterruptedException {
        HttpResponse<String> response =
                SupabaseClient.Tables.MENU_ITEMS_TABLE.get(
                        "?item_id=eq." + itemId + "&select=*&limit=1",
                        null);
        
        String body = response.body();
        
        if (body == null || body.equals("[]") || body.isBlank()) return null;
        
        String itemObj = body.trim();
        if (itemObj.startsWith("[") && itemObj.endsWith("]")) {
            itemObj = itemObj.substring(1, itemObj.length() - 1);
        }
        
        if (itemObj.isBlank()) return null;
        
        String name = BrowseMenuController.extractString(itemObj, "name");
        String description = BrowseMenuController.extractString(itemObj, "description");
        double price = BrowseMenuController.extractDouble(itemObj, "price");
        boolean available = BrowseMenuController.extractBoolean(itemObj, "available");
        
        return new Product(itemId, name, description, price, available);
    }

    private List<Order> parseOrders(String json) {
        List<Order> orders = new ArrayList<>();
        if (json == null || json.equals("[]") || json.isBlank()) return orders;

        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        
        if (trimmed.isBlank()) return orders;
        
        for (String obj : trimmed.split("\\},\\s*\\{")) {
            // Ensure proper JSON format for each object
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";
            
            String orderId = BrowseMenuController.extractString(obj, "order_id");
            String username = BrowseMenuController.extractString(obj, "username");
            String status = BrowseMenuController.extractString(obj, "status");
            double total = BrowseMenuController.extractDouble(obj, "total_price");
            String placedAt = BrowseMenuController.extractString(obj, "placed_at");

            Order o = new Order(orderId, username);
            try { 
                o.setStatus(Order.Status.valueOf(status)); 
            } catch (IllegalArgumentException ignored) {}
            o.setTotalPrice(total);
            o.setPlacedAt(placedAt);
            orders.add(o);
        }
        return orders;
    }
}