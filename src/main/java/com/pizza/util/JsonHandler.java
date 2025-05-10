package com.pizza.util;

import com.pizza.builder.Pizza;
import com.pizza.builder.PizzaBuilder;
import com.pizza.decorator.ExtraCheeseDecorator;
import com.pizza.decorator.ExtraSauceDecorator;
import com.pizza.decorator.PizzaDecorator;
import com.pizza.feedback.Feedback;
import com.pizza.observer.Order;
import com.pizza.state.*;
import com.pizza.user.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonHandler {
    private static final String USER_FILE_PATH = "src/main/resources/data/users.json";
    private static final String ORDER_FILE_PATH = "src/main/resources/data/orders.json";
    private static final String FEEDBACK_FILE_PATH = "src/main/resources/data/feedbacks.json";

    public void saveUser(Map<String, User> users) {
        try (FileWriter file = new FileWriter(USER_FILE_PATH)) {
            JSONObject usersJson = new JSONObject();

            for (Map.Entry<String, User> entry : users.entrySet()) {
                User user = entry.getValue();
                JSONObject userJson = new JSONObject();
                userJson.put("name", user.getName());
                userJson.put("email", user.getEmail());
                userJson.put("loyaltyPoints", user.getLoyaltyPoints());

                // Save order history
                JSONArray ordersJson = new JSONArray();
                for (Order order : user.getOrderHistory()) {
                    JSONObject orderJson = new JSONObject();
                    orderJson.put("status", order.getStatus());

                    JSONArray pizzasJson = new JSONArray();
                    for (Pizza pizza : order.getPizzas()) {
                        JSONObject pizzaJson = new JSONObject();
                        savePizzaToJson(pizzaJson, pizza);
                        pizzasJson.add(pizzaJson);
                    }
                    orderJson.put("pizzas", pizzasJson);
                    ordersJson.add(orderJson);
                }
                userJson.put("orders", ordersJson);

                // Save favorites
                JSONObject favoritesJson = new JSONObject();
                for (Map.Entry<String, Pizza> favoriteEntry : user.getFavorites().entrySet()) {
                    JSONObject pizzaJson = new JSONObject();
                    savePizzaToJson(pizzaJson, favoriteEntry.getValue());
                    favoritesJson.put(favoriteEntry.getKey(), pizzaJson);
                }
                userJson.put("favorites", favoritesJson);

                usersJson.put(entry.getKey(), userJson);
            }

            file.write(usersJson.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, User> loadUsers() {
        JSONParser parser = new JSONParser();
        Map<String, User> users = new HashMap<>();

        try (FileReader reader = new FileReader(USER_FILE_PATH)) {
            JSONObject usersJson = (JSONObject) parser.parse(reader);

            for (Object key : usersJson.keySet()) {
                String email = (String) key;
                JSONObject userJson = (JSONObject) usersJson.get(email);

                String name = (String) userJson.get("name");
                User user = new User(name, email);

                // Load loyalty points
                if (userJson.containsKey("loyaltyPoints")) {
                    int loyaltyPoints = ((Long) userJson.get("loyaltyPoints")).intValue();
                    user.addLoyaltyPoints(loyaltyPoints);
                }

                // Load orders
                if (userJson.containsKey("orders")) {
                    JSONArray ordersJson = (JSONArray) userJson.get("orders");
                    for (Object orderObj : ordersJson) {
                        JSONObject orderJson = (JSONObject) orderObj;
                        Order order = new Order();

                        // Set order status
                        String status = (String) orderJson.get("status");
                        order.setState(mapStatusToState(status));

                        // Load pizzas
                        if (orderJson.containsKey("pizzas")) {
                            JSONArray pizzasJson = (JSONArray) orderJson.get("pizzas");
                            for (Object pizzaObj : pizzasJson) {
                                JSONObject pizzaJson = (JSONObject) pizzaObj;
                                Pizza pizza = parsePizza(pizzaJson);
                                order.addPizza(pizza);
                            }
                        }

                        user.addOrder(order);
                    }
                }

                // Load favorite pizzas
                if (userJson.containsKey("favorites")) {
                    JSONObject favoritesJson = (JSONObject) userJson.get("favorites");
                    for (Object favoriteKey : favoritesJson.keySet()) {
                        String favoriteName = (String) favoriteKey;
                        JSONObject pizzaJson = (JSONObject) favoritesJson.get(favoriteName);
                        Pizza pizza = parsePizza(pizzaJson);
                        user.addFavorite(favoriteName, pizza);
                    }
                }

                users.put(email, user);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void saveFeedbacks(List<Feedback> feedbacks) {
        try (FileWriter file = new FileWriter(FEEDBACK_FILE_PATH)) {
            JSONArray feedbacksArray = new JSONArray();

            for (Feedback feedback : feedbacks) {
                JSONObject feedbackJson = new JSONObject();
                feedbackJson.put("rating", feedback.getRating());
                feedbackJson.put("comment", feedback.getComment());
                feedbackJson.put("customerName", feedback.getCustomerName());
                feedbackJson.put("timestamp", feedback.getTimestamp().toString());

                // Save pizza details
                JSONObject pizzaJson = new JSONObject();
                savePizzaToJson(pizzaJson, feedback.getPizza());
                feedbackJson.put("pizza", pizzaJson);
                feedbacksArray.add(feedbackJson);
            }

            file.write(feedbacksArray.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Feedback> loadFeedbacks() {
        List<Feedback> feedbacks = new ArrayList<>();
        File feedbackFile = new File(FEEDBACK_FILE_PATH);

        // Create directory and empty file if they don't exist
        feedbackFile.getParentFile().mkdirs();
        if (!feedbackFile.exists() || feedbackFile.length() == 0) {
            try (FileWriter writer = new FileWriter(feedbackFile)) {
                writer.write("[]");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return feedbacks;
            }
        }

        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(feedbackFile)) {
            JSONArray feedbacksArray = (JSONArray) parser.parse(reader);

            for (Object obj : feedbacksArray) {
                JSONObject feedbackJson = (JSONObject) obj;
                int rating = ((Long) feedbackJson.get("rating")).intValue();
                String comment = (String) feedbackJson.get("comment");
                String customerName = (String) feedbackJson.get("customerName");

                // Parse pizza
                JSONObject pizzaJson = (JSONObject) feedbackJson.get("pizza");
                Pizza pizza = parsePizza(pizzaJson);

                // Create feedback object
                Feedback feedback = new Feedback(rating, comment, pizza, customerName);
                feedbacks.add(feedback);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return feedbacks;
    }

    private Pizza parsePizza(JSONObject pizzaJson) {
        String crust = (String) pizzaJson.get("crust");
        String sauce = (String) pizzaJson.get("sauce");
        double price = ((Number) pizzaJson.get("price")).doubleValue();

        PizzaBuilder builder = new PizzaBuilder()
                .setCrust(crust)
                .setSauce(sauce)
                .setPrice(price);

        // Handle toppings as JSONArray
        Object toppingsObj = pizzaJson.get("toppings");
        if (toppingsObj instanceof JSONArray) {
            JSONArray toppingsArray = (JSONArray) toppingsObj;
            for (Object topping : toppingsArray) {
                builder.addTopping((String) topping);
            }
        }

        Pizza pizza = builder.build();

        // Handle decorators
        if (pizzaJson.containsKey("decorators")) {
            JSONArray decoratorsArray = (JSONArray) pizzaJson.get("decorators");
            for (Object decorator : decoratorsArray) {
                String decoratorType = (String) decorator;
                if ("ExtraCheese".equals(decoratorType)) {
                    pizza = new ExtraCheeseDecorator(pizza);
                } else if ("ExtraSauce".equals(decoratorType)) {
                    pizza = new ExtraSauceDecorator(pizza);
                }
            }
        }

        return pizza;
    }

    private OrderState mapStatusToState(String status) {
        switch (status) {
            case "Placed":
                return new PlacedState();
            case "Preparing":
                return new PreparingState();
            case "Delivered":
                return new DeliveredState();
            default:
                return new PlacedState(); // Default to PlacedState instead of throwing exception
        }
    }

    private void savePizzaToJson(JSONObject pizzaJson, Pizza pizza) {
        pizzaJson.put("crust", pizza.getCrust());
        JSONArray toppingsArray = new JSONArray();
        toppingsArray.addAll(pizza.getToppings());
        pizzaJson.put("toppings", toppingsArray);
        pizzaJson.put("sauce", pizza.getSauce());
        pizzaJson.put("price", pizza.getPrice());

        // Save decorators
        JSONArray decoratorsArray = new JSONArray();
        Pizza currentPizza = pizza;
        while (currentPizza instanceof PizzaDecorator) {
            if (currentPizza instanceof ExtraCheeseDecorator) {
                decoratorsArray.add("ExtraCheese");
            } else if (currentPizza instanceof ExtraSauceDecorator) {
                decoratorsArray.add("ExtraSauce");
            }
            currentPizza = ((PizzaDecorator) currentPizza).getBasePizza();
        }
        if (!decoratorsArray.isEmpty()) {
            pizzaJson.put("decorators", decoratorsArray);
        }
    }

    public void saveOrder(Order order) {
        File orderFile = new File(ORDER_FILE_PATH);
        orderFile.getParentFile().mkdirs();  // Create directories if they don't exist

        List<Order> existingOrders = loadOrders();
        existingOrders.add(order);

        try (FileWriter file = new FileWriter(orderFile)) {
            JSONArray orderArray = new JSONArray();

            for (Order existingOrder : existingOrders) {
                JSONObject orderJson = new JSONObject();
                orderJson.put("status", existingOrder.getStatus());

                JSONArray pizzasJson = new JSONArray();
                for (Pizza pizza : existingOrder.getPizzas()) {
                    JSONObject pizzaJson = new JSONObject();
                    savePizzaToJson(pizzaJson, pizza);
                    pizzasJson.add(pizzaJson);
                }

                orderJson.put("pizzas", pizzasJson);
                orderArray.add(orderJson);
            }

            file.write(orderArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Order loadOrder(JSONObject orderJson) {
        Order order = new Order();
        try {
            // Set order status
            String status = (String) orderJson.get("status");
            order.setState(mapStatusToState(status));

            // Load pizzas with decorators
            if (orderJson.containsKey("pizzas")) {
                JSONArray pizzasJson = (JSONArray) orderJson.get("pizzas");
                for (Object pizzaObj : pizzasJson) {
                    JSONObject pizzaJson = (JSONObject) pizzaObj;
                    Pizza pizza = parsePizza(pizzaJson);
                    order.addPizza(pizza);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return order;
    }


    public List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();
        File orderFile = new File(ORDER_FILE_PATH);

        // Create the directory if it doesn't exist
        orderFile.getParentFile().mkdirs();

        // If file doesn't exist or is empty, initialize it with an empty array
        if (!orderFile.exists() || orderFile.length() == 0) {
            try (FileWriter writer = new FileWriter(orderFile)) {
                writer.write("[]");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return orders;
            }
        }

        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(orderFile)) {
            JSONArray orderArray = (JSONArray) parser.parse(reader);

            for (Object obj : orderArray) {
                JSONObject orderJson = (JSONObject) obj;
                Order order = loadOrder(orderJson);
                orders.add(order);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return orders;
    }
}
