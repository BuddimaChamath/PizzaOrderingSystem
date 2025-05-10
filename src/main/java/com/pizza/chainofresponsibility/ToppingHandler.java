package com.pizza.chainofresponsibility;

import com.pizza.builder.PizzaBuilder;

public class ToppingHandler extends CustomizationHandler {
    @Override
    public void handle(PizzaBuilder builder, String customization) {
        if (customization.startsWith("Topping:")) {
            builder.addTopping(customization.split(":")[1].trim());
        } else if (nextHandler != null) {
            nextHandler.handle(builder, customization);
        }
    }
}
