package entity;
import java.util.List;

public class Recipe {

    private String recipeId;
    private String productName;
    private List<String> ingredients;
    private List<String> measurements;

    public Recipe(String productName, List<String> ingredients, List<String> measurements) {
        this.productName = productName;
        this.ingredients = ingredients;
        this.measurements = measurements;
    }

    // Getters
    public String getProductName() { return productName; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getMeasurements() { return measurements; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }
}