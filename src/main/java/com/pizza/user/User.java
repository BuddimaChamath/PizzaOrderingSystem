package com.pizza.user;

import com.pizza.builder.Pizza;
import com.pizza.observer.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String name;
    private String email;
    private int loyaltyPoints;
    private List<Order> orderHistory = new ArrayList<>();
    private Map<String, Pizza> favorites = new HashMap<>(); // Updated to Map

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.loyaltyPoints = 0;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }

    public void addOrder(Order order) {
        orderHistory.add(order);
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    // Favorites Management
    public Map<String, Pizza> getFavorites() {
        return favorites;
    }

    public void addFavorite(String name, Pizza pizza) {
        favorites.put(name, pizza);
    }

    public void removeFavorite(String name) {
        favorites.remove(name);
    }

    public Pizza getFavoriteByName(String name) {
        return favorites.get(name);
    }


    @Override
    public String toString() {
        return "User{name='" + name + "', email='" + email + "', loyaltyPoints=" + loyaltyPoints +
                ", favorites=" + favorites.keySet() + '}';
    }
}
