package ro.butzi.order.food.model;

import java.math.BigDecimal;

public class FoodItem {
    private String title;
    private BigDecimal price;

    public FoodItem(String title, BigDecimal price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "title='" + title + '\'' +
                ", price=" + price +
                '}';
    }
}
