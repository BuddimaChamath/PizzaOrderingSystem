package com.pizza.state;

import com.pizza.observer.Order;

public interface OrderState {
    void next(Order order);
    String getStatus();
}
