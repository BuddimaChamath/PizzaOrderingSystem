package com.pizza.promotion;

import java.time.LocalDate;

public class Promotion {
    private String name;
    private String description;
    private double discount;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private String promotionCode;

    public Promotion(String name, String description, double discount,
                     LocalDate startDate, LocalDate endDate, String promotionCode) {
        this.name = name;
        this.description = description;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.promotionCode = promotionCode;
        this.isActive = isWithinPromotionPeriod();
    }

    public boolean isWithinPromotionPeriod() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    // Getters and setters
    public double getDiscount() { return discount; }

    public String getPromotionCode() { return promotionCode; }

    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }

    public boolean isActive() { return isActive; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setDiscount(double discount) { this.discount = discount; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        this.isActive = isWithinPromotionPeriod();
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        this.isActive = isWithinPromotionPeriod();
    }
}
