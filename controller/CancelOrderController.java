package controller;

import entity.Order;
import utils.SupabaseClient;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CancelOrderController {

    public List<Order> fetchActiveOrdersForUser(String username)
            throws IOException, InterruptedException {

        HttpResponse<String> response =
                SupabaseClient.Tables.ORDERS_TABLE.get(
                        "?username=eq." + username
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