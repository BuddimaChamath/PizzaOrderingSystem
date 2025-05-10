package com.pizza.promotion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PromotionManager {
    private List<Promotion> promotions;

    public PromotionManager() {
        this.promotions = new ArrayList<>();
    }

    public void addPromotion(Promotion promotion) {
        promotions.add(promotion);
    }

    public void removePromotion(String promotionCode) {
        promotions.removeIf(p -> p.getPromotionCode().equals(promotionCode));
    }

    public Optional<Promotion> getActivePromotion(String promotionCode) {
        return promotions.stream()
                .filter(Promotion::isActive)
                .filter(p -> p.getPromotionCode().equals(promotionCode))
                .findFirst();
    }

    public List<Promotion> getAllActivePromotions() {
        return promotions.stream()
                .filter(Promotion::isActive)
                .toList();
    }

    public double calculateDiscountedPrice(double originalPrice, String promotionCode) {
        Optional<Promotion> promotion = getActivePromotion(promotionCode);
        return promotion.map(p -> originalPrice * (1 - p.getDiscount()))
                .orElse(originalPrice);
    }
}