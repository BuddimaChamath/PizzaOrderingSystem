package com.pizza.delivery;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DeliveryEstimator {
    private static final int BASE_PREPARATION_TIME = 10; // minutes
    private static final int BASE_DELIVERY_TIME = 10; // minutes

    public String estimateDeliveryTime(String location, int orderSize) {
        // In a real system, this would use mapping service APIs and traffic data
        int preparationTime = calculatePreparationTime(orderSize);
        int deliveryTime = calculateDeliveryTime(location);

        LocalTime estimatedTime = LocalTime.now().plusMinutes(preparationTime + deliveryTime);
        return estimatedTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private int calculatePreparationTime(int orderSize) {
        return BASE_PREPARATION_TIME + (5 * (orderSize - 1));
    }

    private int calculateDeliveryTime(String location) {
        // Mock implementation - in real system would use mapping service
        // For now, just return base delivery time
        return BASE_DELIVERY_TIME;
    }
}