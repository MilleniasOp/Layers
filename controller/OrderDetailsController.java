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
 
        HttpResponse<String> response =
                SupabaseClient.Tables.ORDERS_TABLE.get(
                        "?username=eq." + username
                        + "&select=*"
                        + "&order=placed_at.desc",
                        null);
 
        return parseOrders(response.body());
    }
 
    public Order getOrderDetails(String orderId, String username)
            throws IOException, InterruptedException {
 
        HttpResponse<String> response =
                SupabaseClient.Tables.ORDERS_TABLE.get(
                        "?order_id=eq." + orderId
                        + "&select=*,order_items(item_id,quantity)"
                        + "&limit=1",
                        null);
 
        String body = response.body();
        if (body == null || body.equals("[]") || body.isBlank()) return null;
 
        if (!body.contains("\"username\":\"" + username + "\"")) return null;
 
        String obj = body.trim().substring(1, body.length() - 1);
 
        String status   = BrowseMenuController.extractString(obj, "status");
        double total    = BrowseMenuController.extractDouble(obj, "total_price");
        String placedAt = BrowseMenuController.extractString(obj, "placed_at");
 
        Order order = new Order(orderId, username);
        order.setStatus(Order.Status.valueOf(status));
        order.setTotalPrice(total);
        order.setPlacedAt(placedAt);
 
        int start = obj.indexOf("\"order_items\":[");
        if (start != -1) {
            start += "\"order_items\":[".length();
            int end = obj.indexOf("]", start);
            String itemsJson = obj.substring(start, end).trim();
 
            if (!itemsJson.isBlank()) {
                for (String itemObj : itemsJson.split("\\},\\s*\\{")) {
                    String itemId  = BrowseMenuController.extractString(itemObj, "item_id");
                    int    qty     = (int) BrowseMenuController.extractDouble(itemObj, "quantity");
                    for (int i = 0; i < qty; i++) {
                        order.addItem(new MenuItem(itemId, itemId, "", 0.0, true));
                    }
                }
            }
        }
 
        return order;
    }
 
private List<Order> parseOrders(String json) {
        List<Order> orders = new ArrayList<>();
        if (json == null || json.equals("[]") || json.isBlank()) return orders;
 
        json = json.trim().substring(1, json.length() - 1);
        for (String obj : json.split("\\},\\s*\\{")) {
            String orderId  = BrowseMenuController.extractString(obj, "order_id");
            String username = BrowseMenuController.extractString(obj, "username");
            String status   = BrowseMenuController.extractString(obj, "status");
            double total    = BrowseMenuController.extractDouble(obj, "total_price");
            String placedAt = BrowseMenuController.extractString(obj, "placed_at");
 
            Order o = new Order(orderId, username);
            try { o.setStatus(Order.Status.valueOf(status)); }
            catch (IllegalArgumentException ignored) {}
            o.setTotalPrice(total);
            o.setPlacedAt(placedAt);
            orders.add(o);
        }
        return orders;
    }
}