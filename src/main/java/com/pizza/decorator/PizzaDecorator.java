package com.pizza.decorator;

import com.pizza.builder.Pizza;
import com.pizza.builder.PizzaBuilder;

import java.util.List;

public abstract class PizzaDecorator extends Pizza {
    private final Pizza pizza;

    public PizzaDecorator(Pizza pizza) {
        super(new PizzaBuilder()); // Create empty base
        this.pizza = pizza;
    }

    // Add this method to get the base pizza
    public Pizza getBasePizza() {
        return pizza;
    }

    @Override
    public String getCrust() {
        return pizza.getCrust();
    }

    @Override
    public List<String> getToppings() {
        return pizza.getToppings();
    }

    @Override
    public String getSauce() {
        return pizza.getSauce();
    }

    @Override
    public double getPrice() {
        return pizza.getPrice();
    }

    @Override
    public String toString() {
        return pizza.toString();
    }
}
