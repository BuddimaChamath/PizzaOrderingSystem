package com.pizza.feedback;

import com.pizza.builder.Pizza;
import java.time.LocalDateTime;

public class Feedback {
    private int rating;
    private String comment;
    private Pizza pizza;
    private LocalDateTime timestamp;
    private String customerName;

    public Feedback(int rating, String comment, Pizza pizza, String customerName) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
        this.comment = comment;
        this.pizza = pizza;
        this.customerName = customerName;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Pizza getPizza() { return pizza; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getTimestamp() { return timestamp; }
}