package com.pizza.observer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class CustomerNotification implements OrderObserver {
    private String customerName;
    private String orderType;
    private String lastStatus; // Add this to track last notification

    public CustomerNotification(String customerName, String orderType) {
        this.customerName = customerName;
        this.orderType = orderType;
        this.lastStatus = "";
    }

    @Override
    public void update(String status) {
        // Prevent duplicate notifications for the same status
        if (status.equals(lastStatus)) {
            return;
        }

        String message;
        if ("Delivered".equals(status) && "Pickup".equals(orderType)) {
            message = "Notification to " + customerName + ": Your order is ready for pickup.";
        } else if ("Delivered".equals(status)) {
            message = "Notification to " + customerName + ": Your order has been delivered.";
        } else {
            message = "Notification to " + customerName + ": Your order is " + status + ".";
        }

        // Show the message in an Alert dialog
        showAlert(message);
        lastStatus = status;
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Order Notification");
            alert.setHeaderText(null); // No header
            alert.setContentText(message);
            alert.showAndWait(); // Wait for the user to close the dialog
        });
    }
}