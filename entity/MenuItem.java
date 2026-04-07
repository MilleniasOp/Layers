package entity;

public class MenuItem {

    private String itemId;
    private String name;
    private String description;
    private double price;
    private boolean available;
    
    public MenuItem(String itemId, String name, String description, double price, boolean available) {
            this.itemId      = itemId;
            this.name        = name;
            this.description = description;
            this.price       = price;
            this.available   = available;
        }
    
        public String  getItemId() {return itemId;}
        public String  getName() {return name;}
        public String  getDescription() {return description;}
        public double  getPrice() {return price;}
        public boolean isAvailable() {return available;}
        public void    setAvailable(boolean available) {this.available = available;} 
    
        @Override
        public String toString() {
            return String.format("[%s] %s - $%.2f (%s)",
                    itemId, name, price, available ? "Available" : "Unavailable");
    }
}
