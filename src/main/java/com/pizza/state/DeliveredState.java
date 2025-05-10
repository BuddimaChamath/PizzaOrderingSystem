package com.pizza.state;

import com.pizza.observer.Order;

public class DeliveredState implements OrderState {
    @Override
    public void next(Order order) {
        // Final state
    }

    @Override
    public String getStatus() {
        return "Delivered";
    }
}