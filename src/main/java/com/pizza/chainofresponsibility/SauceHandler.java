package com.pizza.chainofresponsibility;

import com.pizza.builder.PizzaBuilder;

public class SauceHandler extends CustomizationHandler {
    @Override
    public void handle(PizzaBuilder builder, String customization) {
        if (customization.startsWith("Sauce:")) {
            builder.setSauce(customization.split(":")[1].trim());
        } else if (nextHandler != null) {
            nextHandler.handle(builder, customization);
        }
    }
}
