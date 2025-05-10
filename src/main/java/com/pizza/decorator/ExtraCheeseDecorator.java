package com.pizza.decorator;

import com.pizza.builder.Pizza;

public class ExtraCheeseDecorator extends PizzaDecorator {
    public ExtraCheeseDecorator(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String toString() {
        return super.toString() + ", Extra Cheese";
    }

    @Override
    public double getPrice() {
        return super.getPrice() + 1.5;
    }
}