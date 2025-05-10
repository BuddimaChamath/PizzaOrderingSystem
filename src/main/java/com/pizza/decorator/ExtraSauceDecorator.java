package com.pizza.decorator;

import com.pizza.builder.Pizza;

public class ExtraSauceDecorator extends PizzaDecorator {
    public ExtraSauceDecorator(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String toString() {
        return super.toString() + ", Extra Sauce";
    }

    @Override
    public double getPrice() {
        return super.getPrice() + 1.0;
    }
}
