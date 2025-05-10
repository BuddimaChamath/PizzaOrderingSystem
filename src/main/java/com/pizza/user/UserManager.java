package com.pizza.user;

import com.pizza.util.JsonHandler;
import com.pizza.observer.Order;
import com.pizza.builder.Pizza;

import java.util.Map;

public class UserManager {
    private JsonHandler jsonHandler;
    private Map<String, User> users;
    private User currentUser;

    public UserManager() {
        this.jsonHandler = new JsonHandler();
        this.users = jsonHandler.loadUsers(); // Load users on startup
    }

    public void registerUser(String name, String email) {
        if (users.containsKey(email)) {
            throw new IllegalArgumentException("User with this email already exists.");
        }
        User user = new User(name, email);
        users.put(email, user);
        jsonHandler.saveUser(users); // Save updated users
        this.currentUser = user; // Automatically log in the registered user
    }

    public void loginUser(String email) {
        if (!users.containsKey(email)) {
            throw new IllegalArgumentException("No user found with this email.");
        }
        this.currentUser = users.get(email);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public void processOrder(Order order) {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to place an order.");
        }

        // Calculate loyalty points based on the total price of the order
        double totalOrderPrice = 0.0;
        for (Pizza pizza : order.getPizzas()) {
            totalOrderPrice += pizza.getPrice();
        }

        // Calculate loyalty points (1 point for every $10 spent)
        int loyaltyPointsEarned = (int) (totalOrderPrice / 10);
        currentUser.addLoyaltyPoints(loyaltyPointsEarned);

        // Debugging: Check loyalty points
        System.out.println("Loyalty Points Earned: " + loyaltyPointsEarned);
        System.out.println("Updated Loyalty Points: " + currentUser.getLoyaltyPoints());

        // Add order to user's history
        currentUser.addOrder(order);

        // Save the updated user data
        System.out.println("Saving updated user to users.json...");
        jsonHandler.saveUser(users);

        // Save the order to orders.json
        System.out.println("Saving order to orders.json...");
        jsonHandler.saveOrder(order);

        // Debugging: Ensure orders are saved
        System.out.println("Order saved with status: " + order.getStatus());
    }

    // Favorites Management
    public void addFavoritePizza(String name, Pizza pizza) {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to manage favorites.");
        }
        currentUser.addFavorite(name, pizza); // Provide both name and pizza
        saveCurrentUser(); // Save the change to JSON
        System.out.println("Pizza added to favorites: " + name);
    }

    public boolean isFavoritePizza(String name) {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to manage favorites.");
        }
        return currentUser.getFavorites().containsKey(name); // Check by name
    }

    public void listFavoritePizzas() {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to view favorites.");
        }
        System.out.println("Favorite Pizzas:");
        for (Map.Entry<String, Pizza> entry : currentUser.getFavorites().entrySet()) {
            System.out.println("Name: " + entry.getKey() + ", Pizza: " + entry.getValue());
        }
    }

    // Save current user data
    public void saveCurrentUser() {
        if (currentUser != null) {
            users.put(currentUser.getEmail(), currentUser);
            jsonHandler.saveUser(users);
        }
    }

    // UserManager.java
    public User getUserByEmail(String email) {
        return users.get(email);
    }

}
