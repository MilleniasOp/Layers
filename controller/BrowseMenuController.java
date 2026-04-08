package controller;

import entity.Product;
import utils.SupabaseClient;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class BrowseMenuController {
 
    public List<Product> retrieveMenuItems() throws IOException, InterruptedException {
        HttpResponse<String> response =
                SupabaseClient.Tables.MENU_ITEMS_TABLE.get("?select=*", null);
 
        return parseMenuItems(response.body());
    }
 
    public List<Product> retrieveAvailableItems() throws IOException, InterruptedException {
        HttpResponse<String> response =
                SupabaseClient.Tables.MENU_ITEMS_TABLE.get(
                        "?select=*&available=eq.true", null);
 
        return parseMenuItems(response.body());
    }
 
    private List<Product> parseMenuItems(String json) {
        List<Product> items = new ArrayList<>();
        if (json == null || json.equals("[]") || json.isBlank()) return items;
 
        json = json.trim().substring(1, json.length() - 1); 
        for (String obj : json.split("\\},\\s*\\{")) {
            String itemId      = extractString(obj, "item_id");
            String name        = extractString(obj, "name");
            String description = extractString(obj, "description");
            double price       = extractDouble(obj, "price");
            boolean available  = extractBoolean(obj, "available");
            items.add(new Product(itemId, name, description, price, available));
        }
        return items;
    }
 
    static String extractString(String obj, String field) {
        String key = "\"" + field + "\":\"";
        int s = obj.indexOf(key);
        if (s == -1) return "";
        s += key.length();
        int e = obj.indexOf("\"", s);
        return e == -1 ? "" : obj.substring(s, e);
    }
 
    static double extractDouble(String obj, String field) {
        String key = "\"" + field + "\":";
        int s = obj.indexOf(key);
        if (s == -1) return 0.0;
        s += key.length();
        int e = obj.indexOf(",", s);
        if (e == -1) e = obj.indexOf("}", s);
        if (e == -1) e = obj.length();
        try { return Double.parseDouble(obj.substring(s, e).trim()); }
        catch (NumberFormatException ex) { return 0.0; }
    }
 
    static boolean extractBoolean(String obj, String field) {
        String key = "\"" + field + "\":";
        int s = obj.indexOf(key);
        if (s == -1) return false;
        s += key.length();
        int e = obj.indexOf(",", s);
        if (e == -1) e = obj.indexOf("}", s);
        if (e == -1) e = obj.length();
        return "true".equalsIgnoreCase(obj.substring(s, e).trim());
    }
}
