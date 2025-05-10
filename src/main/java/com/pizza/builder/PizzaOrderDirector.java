package com.pizza.builder;

public class PizzaOrderDirector {
    public Pizza buildMargherita() {
        return new PizzaBuilder()
                .setCrust("Thin")
                .addTopping("Cheese")
                .addTopping("Tomato")
                .setSauce("Tomato")
                .setPrice(12.99)
                .build();
    }

    public Pizza buildPepperoni() {
        return new PizzaBuilder()
                .setCrust("Thick")
                .addTopping("Cheese")
                .addTopping("Pepperoni")
                .setSauce("Tomato")
                .setPrice(14.99)
                .build();
    }

    public Pizza buildVeggieDelight() {
        return new PizzaBuilder()
                .setCrust("Stuffed")
                .addTopping("Cheese")
                .addTopping("Vegetables")
                .setSauce("Tomato")
                .setPrice(13.99)
                .build();
    }
}
