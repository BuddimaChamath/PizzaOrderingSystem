package com.pizza.builder;

import java.util.ArrayList;
import java.util.List;

public class PizzaBuilder {
    String crust = "Thin";
    List<String> toppings = new ArrayList<>();
    String sauce = "Tomato";
    double price = 10.0;

    public PizzaBuilder setCrust(String crust) {
        this.crust = crust;
        return this;
    }

    public PizzaBuilder addTopping(String topping) {
        if (topping != null && !topping.trim().isEmpty()) {
            toppings.add(topping.trim());
        }
        return this;
    }

    public PizzaBuilder setSauce(String sauce) {
        this.sauce = sauce;
        return this;
    }

    public PizzaBuilder setPrice(double price) {
        this.price = price;
        return this;
    }

    private double calculatePrice() {
        double basePrice = 10.0;
        // Add cost for each topping
        basePrice += toppings.size() * 1.5;
        // Add cost for special crust
        if ("Stuffed".equals(crust)) {
            basePrice += 2.0;
        }
        return basePrice;
    }

    public Pizza build() {
        // Calculate price if not explicitly set
        if (price == 10.0) {
            price = calculatePrice();
        }
        return new Pizza(this);
    }

    // Method to check current toppings (for debugging)
    public List<String> getCurrentToppings() {
        return new ArrayList<>(toppings);
    }
}