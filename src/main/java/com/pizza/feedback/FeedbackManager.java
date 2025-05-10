package com.pizza.feedback;

import com.pizza.util.JsonHandler;
import java.util.*;

public class FeedbackManager {
    private List<Feedback> feedbackList;
    private Map<String, List<Feedback>> pizzaFeedbackMap;
    private JsonHandler jsonHandler;


    public FeedbackManager() {
        this.feedbackList = new ArrayList<>();
        this.pizzaFeedbackMap = new HashMap<>();
    }

    public List<String> getTopRatedPizzas(int limit) {
        return pizzaFeedbackMap.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        entry.getValue().stream()
                                .mapToInt(Feedback::getRating)
                                .average()
                                .orElse(0.0)
                ))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Feedback> getRecentFeedback(int limit) {
        return feedbackList.stream()
                .sorted(Comparator.comparing(Feedback::getTimestamp).reversed())
                .limit(limit)
                .toList();
    }

    public FeedbackManager(JsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        this.feedbackList = new ArrayList<>();
        this.pizzaFeedbackMap = new HashMap<>();

        // Load existing feedbacks
        List<Feedback> loadedFeedbacks = jsonHandler.loadFeedbacks();
        if (loadedFeedbacks != null) {
            for (Feedback feedback : loadedFeedbacks) {
                // Add to feedback list
                feedbackList.add(feedback);

                // Add to pizza feedback map
                String pizzaType = feedback.getPizza().toString();
                pizzaFeedbackMap.computeIfAbsent(pizzaType, k -> new ArrayList<>())
                        .add(feedback);
            }
        }
    }

    public void addFeedback(Feedback feedback) {
        feedbackList.add(feedback);

        String pizzaType = feedback.getPizza().toString();
        pizzaFeedbackMap.computeIfAbsent(pizzaType, k -> new ArrayList<>())
                .add(feedback);

        // Save to JSON file
        jsonHandler.saveFeedbacks(feedbackList);
    }

//    public double getAverageRating(String pizzaType) {
//        List<Feedback> pizzaFeedback = pizzaFeedbackMap.get(pizzaType);
//        if (pizzaFeedback == null || pizzaFeedback.isEmpty()) {
//            return 0.0;
//        }
//        return pizzaFeedback.stream()
//                .mapToInt(Feedback::getRating)
//                .average()
//                .orElse(0.0);
//    }
//
//    public List<Feedback> getFeedbackForPizza(String pizzaType) {
//        return pizzaFeedbackMap.getOrDefault(pizzaType, new ArrayList<>());
//    }
//
//    public List<Feedback> getFeedbackByCustomer(String customerName) {
//        return feedbackList.stream()
//                .filter(f -> f.getCustomerName().equals(customerName))
//                .toList();
//    }

}