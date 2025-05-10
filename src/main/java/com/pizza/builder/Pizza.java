package com.pizza.builder;

import java.util.ArrayList;
import java.util.List;

public class Pizza {
    private String crust;
    private List<String> toppings;
    private String sauce;
    private double price;

    public Pizza(PizzaBuilder builder) {
        this.crust = builder.crust;
        this.toppings = new ArrayList<>(builder.toppings);
        this.sauce = builder.sauce;
        this.price = builder.price;
    }

    public String getCrust() {
        return crust;
    }

    public List<String> getToppings() {
        return new ArrayList<>(toppings);
    }

    public String getSauce() {
        return sauce;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Pizza with " + crust + " crust, Toppings: " +
                String.join(", ", toppings) + ", Sauce: " + sauce +
                ", Price: $" + String.format("%.2f", price);
    }
}