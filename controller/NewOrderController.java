package controller;

import entity.MenuItem;
import entity.Order;
import utils.SupabaseClient;
 
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
//import java.util.stream.Collectors;
 
public class NewOrderController{
 
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
 
        for (String id : selectedItemIds) {
            allItems.stream()
                    .filter(i -> i.getItemId().equals(id) && i.isAvailable())
                    .findFirst()
                    .ifPresent(order::addItem);
        }
        return order;
    }
 
    public Order confirmOrder(Order order) throws IOException, InterruptedException {
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with no items.");
        }
 
        order.setStatus(Order.Status.CONFIRMED);
 
        String orderJson = String.format(
                "{\"order_id\":\"%s\",\"username\":\"%s\",\"status\":\"CONFIRMED\",\"total_price\":%.2f}",
                order.getOrderId(), order.getUsername(), order.getTotalPrice());
 
        SupabaseClient.Tables.ORDERS_TABLE.post(
                orderJson,
                Map.of("Prefer", "return=representation"));
 
        for (MenuItem item : order.getItems()) {
            String itemJson = String.format(
                    "{\"order_id\":\"%s\",\"item_id\":\"%s\",\"quantity\":1}",
                    order.getOrderId(), item.getItemId());
 
            SupabaseClient.Tables.ORDER_ITEMS_TABLE.post(
                    itemJson,
                    Map.of("Prefer", "return=representation"));
        }
 
        return order;
    }
}