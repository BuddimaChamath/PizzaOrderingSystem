package com.pizza.state;

import com.pizza.observer.Order;

public class PlacedState implements OrderState {
    @Override
    public void next(Order order) {
        order.setState(new PreparingState());
    }

    @Override
    public String getStatus() {
        return "Order Placed";
    }
}
