package com.pizza.chainofresponsibility;

import com.pizza.builder.PizzaBuilder;

public class CrustHandler extends CustomizationHandler {
    @Override
    public void handle(PizzaBuilder builder, String customization) {
        if (customization.startsWith("Crust:")) {
            builder.setCrust(customization.split(":")[1].trim());
        } else if (nextHandler != null) {
            nextHandler.handle(builder, customization);
        }
    }
}

