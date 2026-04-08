package controller;

import entity.MenuItem;
import entity.Order;
import utils.SupabaseClient;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailsController {
 
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
        System.out.println("Order items response: " + itemsBody); // Debug log
        
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
                    
                    System.out.println("Processing item - ID: " + itemId + ", Quantity: " + quantity); // Debug log
                    
                    if (quantity > 0) {
                        // Get full menu item details
                        MenuItem fullItem = getMenuItemDetails(itemId);
                        if (fullItem != null) {
                            System.out.println("Found item: " + fullItem.getName() + " - $" + fullItem.getPrice()); // Debug log
                            order.addItem(fullItem, quantity);
                        } else {
                            System.err.println("Failed to find menu item details for ID: " + itemId);
                            // Create a fallback menu item with a meaningful name
                            MenuItem fallbackItem = new MenuItem(
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
        } else {
            System.out.println("No order items found for order: " + orderId);
        }
        
        System.out.println("Total items added to order: " + order.getItems().size()); // Debug log
        
        return order;
    }
    
    private MenuItem getMenuItemDetails(String itemId) 
            throws IOException, InterruptedException {
        HttpResponse<String> response =
                SupabaseClient.Tables.MENU_ITEMS_TABLE.get(
                        "?item_id=eq." + itemId + "&select=*&limit=1",
                        null);
        
        String body = response.body();
        System.out.println("Menu item response for " + itemId + ": " + body); // Debug log
        
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
        
        System.out.println("Parsed menu item - Name: " + name + ", Price: " + price); // Debug log
        
        return new MenuItem(itemId, name, description, price, available);
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