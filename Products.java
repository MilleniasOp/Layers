public class Products {
    private String name;
    private float cost;

    /**
     * Default constructor
     */
    public Products() {
        this.name = "";
        this.cost = 0;
    }

    /**
     * Constructor with name and cost
     * @param name the product name
     * @param cost the product cost
     */
    public Products(String name, float cost) {
        this.name = name;
        this.cost = cost;
    }
    // Getters
    public String getName() {
        return name;
    }

    public float getCost() {
        return cost;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return name + " - $" + String.format("%.2f", cost);
    }

}