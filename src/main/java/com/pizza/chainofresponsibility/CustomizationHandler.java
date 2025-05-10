package com.pizza.chainofresponsibility;

import com.pizza.builder.PizzaBuilder;

public abstract class CustomizationHandler {
    protected CustomizationHandler nextHandler;

    public void setNextHandler(CustomizationHandler handler) {
        this.nextHandler = handler;
    }

    public abstract void handle(PizzaBuilder builder, String customization);
}
