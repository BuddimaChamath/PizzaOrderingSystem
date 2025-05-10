package com.pizza.strategy;

public class DigitalWalletPayment implements PaymentStrategy {
    @Override
    public boolean pay(double amount) {
        System.out.println("Paid $" + amount + " with Digital Wallet");
        return true;
    }
}
