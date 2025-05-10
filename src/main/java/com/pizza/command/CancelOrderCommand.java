package com.pizza.command;

import com.pizza.observer.Order;
import com.pizza.strategy.PaymentStrategy;

public class CancelOrderCommand implements Command {
    private Order order;
    private PaymentStrategy paymentStrategy;

    public CancelOrderCommand(Order order, PaymentStrategy paymentStrategy) {
        this.order = order;
        this.paymentStrategy = paymentStrategy;
    }

    @Override
    public void execute() {
        // Cancel order logic
        System.out.println("Cancelling order");
    }

}
