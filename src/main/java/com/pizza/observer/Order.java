package com.pizza.observer;

import com.pizza.builder.Pizza;
import com.pizza.state.*;
import com.pizza.feedback.Feedback;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<Pizza> pizzas = new ArrayList<>();
    private List<OrderObserver> observers = new ArrayList<>();
    private OrderState state;
    private Label statusLabel;
    private Feedback feedback;
    private String deliveryEstimate;
    private double discountedPrice;

    public Order() {
        this.state = new PlacedState();
    }

    public void nextState() {
        state.next(this);
    }

    public void addPizza(Pizza pizza) {
        pizzas.add(pizza);
    }

    public double calculateTotalPrice() {
        return pizzas.stream().mapToDouble(Pizza::getPrice).sum();
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public double getDiscountedPrice() {
        return discountedPrice > 0 ? discountedPrice : calculateTotalPrice();
    }

    public void detach(OrderObserver observer) {
        observers.remove(observer);
    }

    public void setState(OrderState state) {
        this.state = state;
        updateStatus();
    }

    public void addFeedback(int rating, String comment, String customerName) {
        if (this.getPizzas().isEmpty()) {
            throw new IllegalStateException("Cannot add feedback to an empty order");
        }
        this.feedback = new Feedback(rating, comment, this.getPizzas().get(0), customerName);
    }

    public void setDeliveryEstimate(String estimate) {
        this.deliveryEstimate = estimate;
    }

    public String getDeliveryEstimate() {
        return deliveryEstimate;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    private void updateStatus() {
        notifyObservers();
        updateStatusLabel();
    }

    public void attach(OrderObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        String currentStatus = state.getStatus();
        for (OrderObserver observer : observers) {
            observer.update(currentStatus);
        }
    }

    public String getStatus() {
        return state.getStatus();
    }

    public List<Pizza> getPizzas() {
        return pizzas;
    }

    public void setStatusLabel(Label statusLabel) {
        this.statusLabel = statusLabel;
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        if (statusLabel != null) {
            statusLabel.setText("Order Status: " + state.getStatus());
        }
    }
}