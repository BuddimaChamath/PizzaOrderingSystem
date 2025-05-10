package com.pizza.command;

import com.pizza.observer.Order;
import com.pizza.strategy.PaymentStrategy;

public class PlaceOrderCommand implements Command {
    private Order order;
    private PaymentStrategy paymentStrategy;

    public PlaceOrderCommand(Order order, PaymentStrategy paymentStrategy) {
        this.order = order;
        this.paymentStrategy = paymentStrategy;
    }

    @Override
    public void execute() {
        double totalPrice = order.getPizzas().stream()
                .mapToDouble(pizza -> pizza.getPrice())
                .sum();
        paymentStrategy.pay(totalPrice);
        order.nextState(); // Progress order state
    }

}