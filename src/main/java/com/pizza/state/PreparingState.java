package com.pizza.state;

import com.pizza.observer.Order;

public class PreparingState implements OrderState {
    @Override
    public void next(Order order) {
        order.setState(new DeliveredState());
    }

    @Override
    public String getStatus() {
        return "Preparing";
    }
}