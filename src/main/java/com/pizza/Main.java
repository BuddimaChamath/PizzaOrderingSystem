package com.pizza;

import com.pizza.builder.*;
import com.pizza.chainofresponsibility.*;
import com.pizza.command.*;
import com.pizza.decorator.*;
import com.pizza.delivery.DeliveryEstimator;
import com.pizza.feedback.*;
import com.pizza.observer.*;
import com.pizza.promotion.*;
import com.pizza.strategy.*;
import com.pizza.user.UserManager;
import com.pizza.util.JsonHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {
    private UserManager userManager = new UserManager();
    private Order currentOrder = null;
    private CustomerNotification currentObserver = null;
    private Label loyaltyPointsLabel;
    private final PaymentStrategy[] paymentStrategy = new PaymentStrategy[1];
    private ComboBox<String> favoritesSelect;
    private FeedbackManager feedbackManager = new FeedbackManager();
    private PromotionManager promotionManager = new PromotionManager();
    private DeliveryEstimator deliveryEstimator = new DeliveryEstimator();
    private TextField promoCodeField = new TextField();
    private JsonHandler jsonHandler;
    private Button logoutButton;
    private ToggleGroup orderTypeGroup;
    private RadioButton pickupOption;
    private RadioButton deliveryOption;
    private Label deliveryEstimateLabel;
    private Label promoStatusLabel;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20)); // Add padding around the layout

        root.setStyle("-fx-padding: 15; -fx-background-color: #f9f9f9;");

        // User Management
        TextField nameField = new TextField();
        nameField.setPromptText("Enter Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        Button registerButton = new Button("Register");
        Button loginButton = new Button("Login");
        Label userLabel = new Label("No user logged in.");
        logoutButton = new Button("Logout");
        logoutButton.setDisable(true); // Disabled initially since no user is logged in


        jsonHandler = new JsonHandler();
        feedbackManager = new FeedbackManager(jsonHandler);

        // Initialize loyalty points label with styling
        loyaltyPointsLabel = new Label("Loyalty Points: 0");
        loyaltyPointsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a90e2;");

        Label statusLabel = new Label("Order Status: No order placed yet.");

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(5);

        favoritesSelect = new ComboBox<>();
        favoritesSelect.setPromptText("Select a Favorite");
        Button saveFavoriteButton = new Button("Save as Favorite");
        Button reorderFavoriteButton = new Button("Reorder Favorite");

        saveFavoriteButton.setOnAction(e -> {
            if (currentOrder != null && !currentOrder.getPizzas().isEmpty()) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Save Favorite");
                dialog.setHeaderText("Enter a name for this favorite pizza:");
                dialog.showAndWait().ifPresent(name -> {
                    Pizza favoritePizza = currentOrder.getPizzas().get(0);
                    userManager.addFavoritePizza(name, favoritePizza); // Use the name
                    refreshFavoritesDropdown(); // Update dropdown
                });
            } else {
                showError("No current order to save as favorite.");
            }
        });

        reorderFavoriteButton.setOnAction(e -> {
            String selectedFavorite = favoritesSelect.getValue();
            if (selectedFavorite != null) {
                Pizza favoritePizza = userManager.getCurrentUser().getFavoriteByName(selectedFavorite);
                if (favoritePizza != null) {
                    if (paymentStrategy[0] == null) {
                        showError("Please select a payment method before reordering.");
                        return;
                    }

                    if (orderTypeGroup.getSelectedToggle() == null) {
                        showError("Please select an order type: Pickup or Delivery.");
                        return;
                    }

                    String orderType = pickupOption.isSelected() ? "Pickup" : "Delivery";

                    currentOrder = new Order();
                    currentOrder.addPizza(favoritePizza);

                    // Promo Code Handling
                    String promoDetails = "";
                    String promoCode = promoCodeField.getText().trim();
                    double originalPrice = currentOrder.calculateTotalPrice();
                    double finalPrice = originalPrice;

                    if (!promoCode.isEmpty()) {
                        Optional<Promotion> promotion = promotionManager.getActivePromotion(promoCode);
                        if (promotion.isPresent()) {
                            finalPrice = promotionManager.calculateDiscountedPrice(originalPrice, promoCode);
                            currentOrder.setDiscountedPrice(finalPrice);
                            promoDetails = String.format("""
                        
                        Promotion Applied: %s
                        Original Price: $%.2f
                        Discount: %.0f%%""",
                                    promotion.get().getName(),
                                    originalPrice,
                                    promotion.get().getDiscount() * 100);
                            promoStatusLabel.setText("Promotion applied successfully!");
                            promoStatusLabel.setStyle("-fx-text-fill: green;");
                        } else {
                            promoStatusLabel.setText("Invalid or expired promotion code");
                            promoStatusLabel.setStyle("-fx-text-fill: red;");
                            return; // Don't proceed with invalid promo code
                        }
                    }

                    // Create and attach an observer
                    currentObserver = new CustomerNotification(userManager.getCurrentUser().getName(), orderType);
                    currentOrder.attach(currentObserver);

                    // Execute order
                    PlaceOrderCommand placeOrderCommand = new PlaceOrderCommand(currentOrder, paymentStrategy[0]);
                    placeOrderCommand.execute();

                    // Update loyalty points
                    userManager.processOrder(currentOrder);
                    updateLoyaltyPointsDisplay();

                    // Build detailed order summary
                    StringBuilder orderDetails = new StringBuilder();
                    orderDetails.append("Reordered favorite pizza: ").append(selectedFavorite)
                            .append("\nOrder Type: ").append(orderType)
                            .append(promoDetails.isEmpty() ? "" : promoDetails)
                            .append(String.format("\nFinal Price: $%.2f", finalPrice));

                    if ("Delivery".equals(orderType)) {
                        String estimatedTime = deliveryEstimator.estimateDeliveryTime("default location", currentOrder.getPizzas().size());
                        currentOrder.setDeliveryEstimate(estimatedTime);
                        deliveryEstimateLabel.setText("Estimated Delivery Time: " + estimatedTime);
                        orderDetails.append("\nEstimated Delivery Time: ").append(estimatedTime);
                    }

                    // Display detailed order summary in resultArea
                    resultArea.setText(orderDetails.toString());

                    // Update the status label dynamically based on the order state
                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.seconds(0), event -> {
                                if (currentOrder != null) {
                                    statusLabel.setText(orderType.equals("Pickup")
                                            ? "Order is being prepared for pickup..."
                                            : "Order is being prepared for delivery...");
                                }
                            }),
                            new KeyFrame(Duration.seconds(30), event -> {
                                if (currentOrder != null) {
                                    currentOrder.nextState();
                                    statusLabel.setText(orderType.equals("Pickup")
                                            ? "Order Status: ready for pick up"
                                            : "Order Status: delivered");
                                }
                            })
                    );
                    timeline.play();

                    // Clear promotion fields
                    promoCodeField.clear();
                    promoStatusLabel.setText("");

                } else {
                    showError("Selected favorite pizza could not be found.");
                }
            } else {
                showError("Please select a favorite pizza to reorder.");
            }
        });

        // Order Type Selection
        orderTypeGroup = new ToggleGroup();
        pickupOption = new RadioButton("Pickup");
        deliveryOption = new RadioButton("Delivery");
        pickupOption.setToggleGroup(orderTypeGroup);
        deliveryOption.setToggleGroup(orderTypeGroup);
        deliveryEstimateLabel = new Label("");

        orderTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (deliveryOption.isSelected()) {
                deliveryEstimateLabel.setText("Estimated Delivery Time: 30 minutes");
            } else {
                deliveryEstimateLabel.setText("");
            }
        });

        // User Registration and Login
        registerButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            // Validate name
            if (name.isEmpty()) {
                showError("Name cannot be empty.");
                return;
            }
            if (!name.matches("[a-zA-Z\\s]+")) {
                showError("Name can only contain alphabets and spaces.");
                return;
            }

            // Validate email
            if (email.isEmpty()) {
                showError("Email cannot be empty.");
                return;
            }
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                showError("Invalid email format.");
                return;
            }

            // Check for duplicate email
            if (userManager.getUserByEmail(email) != null) {
                showError("This email is already registered.");
                return;
            }

            try {
                userManager.registerUser(name, email);
                userLabel.setText("Logged in as: " + userManager.getCurrentUser().getName());
                updateLoyaltyPointsDisplay();
                logoutButton.setDisable(false);
                loginButton.setDisable(true);
                registerButton.setDisable(true);
                showSuccess("Registration successful!");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();

            // Validate email
            if (email.isEmpty()) {
                showError("Email cannot be empty.");
                return;
            }
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                showError("Invalid email format.");
                return;
            }

            // Check if the user exists
            if (userManager.getUserByEmail(email) == null) {
                showError("This email is not registered. Please register first.");
                return;
            }

            try {
                userManager.loginUser(email);
                userLabel.setText("Logged in as: " + userManager.getCurrentUser().getName());
                updateLoyaltyPointsDisplay();
                refreshFavoritesDropdown(); // Ensure favorites are loaded on login
                logoutButton.setDisable(false);
                loginButton.setDisable(true);
                registerButton.setDisable(true);
                showSuccess("Login successful!");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        logoutButton.setOnAction(e -> {
            userManager.logout(); // Reset the current user in UserManager
            currentOrder = null; // Clear any active orders
            favoritesSelect.getItems().clear(); // Clear favorites dropdown
            loyaltyPointsLabel.setText("Loyalty Points: 0"); // Reset loyalty points display
            userLabel.setText("No user logged in."); // Update the UI to reflect no user is logged in
            logoutButton.setDisable(true); // Disable the logout button
            loginButton.setDisable(false);
            registerButton.setDisable(false);
            showSuccess("You have been logged out successfully!");
        });

        // Pizza Configuration
        ComboBox<String> crustSelect = new ComboBox<>();
        crustSelect.getItems().addAll("Thin", "Thick", "Stuffed");
        crustSelect.setPromptText("Select Crust");

        CheckBox cheeseCheck = new CheckBox("Cheese");
        CheckBox pepperoniCheck = new CheckBox("Pepperoni");
        CheckBox extraCheeseCheck = new CheckBox("Extra Cheese");
        CheckBox extraSauceCheck = new CheckBox("Extra Sauce");

        ComboBox<String> presetSelect = new ComboBox<>();
        presetSelect.getItems().addAll("Custom", "Margherita", "Pepperoni", "Veggie Delight");
        presetSelect.setPromptText("Select Predefined Pizza");

        presetSelect.setOnAction(e -> {
            String selectedPreset = presetSelect.getValue();
            boolean isCustom = "Custom".equals(selectedPreset);
            crustSelect.setDisable(!isCustom);
            cheeseCheck.setDisable(!isCustom);
            pepperoniCheck.setDisable(!isCustom);
            extraCheeseCheck.setDisable(!isCustom);
            extraSauceCheck.setDisable(!isCustom);
        });

        // Order Controls
        Button orderButton = new Button("Place Order");
        Button cancelOrderButton = new Button("Cancel Order");

        // Payment Configuration
        ComboBox<String> paymentSelect = new ComboBox<>();
        paymentSelect.getItems().addAll("Credit Card", "Digital Wallet");
        paymentSelect.setPromptText("Select Payment Method");

        paymentSelect.setOnAction(e -> {
            String selectedPaymentMethod = paymentSelect.getValue();
            if ("Credit Card".equals(selectedPaymentMethod)) {
                paymentStrategy[0] = new CreditCardPayment();
            } else if ("Digital Wallet".equals(selectedPaymentMethod)) {
                paymentStrategy[0] = new DigitalWalletPayment();
            }
        });

        PizzaOrderDirector director = new PizzaOrderDirector();

        VBox promotionBox = new VBox(5);
        promotionBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label promotionLabel = new Label("Promotion Code");
        promotionLabel.setStyle("-fx-font-weight: bold;");

        promoCodeField = new TextField();
        promoCodeField.setPromptText("Enter promotion code");

        promoStatusLabel = new Label("");
        promoStatusLabel.setStyle("-fx-padding: 5 0 0 0;");

        promotionBox.getChildren().addAll(
                promotionLabel,
                promoCodeField,
                promoStatusLabel
        );

        // Add sample promotions at startup
        promotionManager.addPromotion(new Promotion(
                "Summer Special",
                "20% off all orders",
                0.20,
                LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(30),
                "SUMMER20"
        ));

        promotionManager.addPromotion(new Promotion(
                "New Customer",
                "15% off first order",
                0.15,
                LocalDate.now().minusDays(7),
                LocalDate.now().plusDays(90),
                "NEWCUST15"
        ));

        // Order Button Handler
        orderButton.setOnAction(e -> {
            // Validation checks
            if (userManager.getCurrentUser() == null) {
                showError("Please log in before placing an order!");
                return;
            }

            if (paymentStrategy[0] == null) {
                showError("Please select a payment method.");
                return;
            }

            if (orderTypeGroup.getSelectedToggle() == null) {
                showError("Please select an order type: Pickup or Delivery.");
                return;
            }

            String selectedPreset = presetSelect.getValue();
            if (selectedPreset == null) {
                showError("Please select a pizza option.");
                return;
            }

            Pizza pizza = buildPizza(selectedPreset, director, crustSelect, cheeseCheck, pepperoniCheck, extraCheeseCheck, extraSauceCheck);
            if (pizza == null) {
                showError("Error building pizza.");
                return;
            }

            // Clean up previous order and observer
            if (currentOrder != null && currentObserver != null) {
                currentOrder.detach(currentObserver);
            }

            // Create new order
            currentOrder = new Order();
            currentOrder.addPizza(pizza);

            // Calculate base price
            double originalPrice = currentOrder.calculateTotalPrice();
            double finalPrice = originalPrice;
            String promoDetails = "";

            // Check and apply promotion before order processing
            String promoCode = promoCodeField.getText().trim();
            if (!promoCode.isEmpty()) {
                Optional<Promotion> promotion = promotionManager.getActivePromotion(promoCode);
                if (promotion.isPresent()) {
                    finalPrice = promotionManager.calculateDiscountedPrice(originalPrice, promoCode);
                    currentOrder.setDiscountedPrice(finalPrice);
                    promoDetails = String.format("""
                        
                        Promotion Applied: %s
                        Original Price: $%.2f
                        Discount: %.0f%%""",
                            promotion.get().getName(),
                            originalPrice,
                            promotion.get().getDiscount() * 100);
                    promoStatusLabel.setText("Promotion applied successfully!");
                    promoStatusLabel.setStyle("-fx-text-fill: green;");
                } else {
                    promoStatusLabel.setText("Invalid or expired promotion code");
                    promoStatusLabel.setStyle("-fx-text-fill: red;");
                    return; // Don't proceed with invalid promo code
                }
            }

            // Create and attach observer
            String orderType = pickupOption.isSelected() ? "Pickup" : "Delivery";
            currentObserver = new CustomerNotification(userManager.getCurrentUser().getName(), orderType);
            currentOrder.attach(currentObserver);
            currentOrder.setStatusLabel(statusLabel);

            // Process order
            PlaceOrderCommand placeOrderCommand = new PlaceOrderCommand(currentOrder, paymentStrategy[0]);
            placeOrderCommand.execute();

            // Update loyalty points
            userManager.processOrder(currentOrder);
            updateLoyaltyPointsDisplay();

            // Build and display order summary
            StringBuilder orderDetails = new StringBuilder();
            orderDetails.append("Order placed!");
            orderDetails.append("\nPizza Details: ").append(pizza);
            orderDetails.append(promoDetails);
            orderDetails.append(String.format("\nFinal Price: $%.2f", finalPrice));
            orderDetails.append("\nOrder Type: ").append(orderType);

            if ("Delivery".equals(orderType)) {
                String estimatedTime = deliveryEstimator.estimateDeliveryTime("default location", currentOrder.getPizzas().size());
                currentOrder.setDeliveryEstimate(estimatedTime);
                deliveryEstimateLabel.setText("Estimated Delivery Time: " + estimatedTime);
                orderDetails.append("\nEstimated Delivery Time: ").append(estimatedTime);
            }

            resultArea.setText(orderDetails.toString());

            // Clear promotion fields
            promoCodeField.clear();
            promoStatusLabel.setText("");

            // Order state progression
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(20), event -> {
                        if (currentOrder != null) currentOrder.nextState();
                    }),
                    new KeyFrame(Duration.seconds(30), event -> {
                        if (currentOrder != null) {
                            currentOrder.nextState();
                            if ("Delivery".equals(orderType)) {
                                deliveryEstimateLabel.setText("");
                            }
                        }
                    })
            );
            timeline.play();

            // Clear promotion fields
            promoCodeField.clear();
            promoStatusLabel.setText("");
        });

        // Cancel Button Handler
        cancelOrderButton.setOnAction(e -> {
            if (currentOrder != null) {
                CancelOrderCommand cancelOrderCommand = new CancelOrderCommand(currentOrder, paymentStrategy[0]);
                cancelOrderCommand.execute();
                statusLabel.setText("Order Status: Cancelled");

                // Clean up the observer when cancelling
                if (currentObserver != null) {
                    currentOrder.detach(currentObserver);
                    currentObserver = null;
                }
                currentOrder = null;
                resultArea.clear();
            } else {
                showError("No active order to cancel.");
            }
        });

        // Layout Assembly
        root.getChildren().addAll(
                new Label("User Management"),
                nameField, emailField, registerButton, loginButton, logoutButton, userLabel,
                loyaltyPointsLabel,
                new Label("Order Type:"),
                pickupOption,
                deliveryOption,
                deliveryEstimateLabel,
                new Label("Favorites"),
                favoritesSelect, saveFavoriteButton, reorderFavoriteButton,
                new Label("Predefined Pizza Configurations"), presetSelect,
                new Label("Create Your Pizza"),
                crustSelect, cheeseCheck, pepperoniCheck, extraCheeseCheck, extraSauceCheck,
                new Label("Select Payment Method"), paymentSelect,
                promotionBox, // Add promotion section here, before order controls
                orderButton, cancelOrderButton, resultArea, statusLabel
        );

        // Add new sections
        root.getChildren().add(createFeedbackSection());

        // Create a ScrollPane and add the root content to it
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);  // Ensure the content stretches horizontally

        // Scene Setup
        Scene scene = new Scene(scrollPane, 600, 700);
        primaryStage.setTitle("Pizza Ordering System");
        primaryStage.centerOnScreen();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshFavoritesDropdown() {
        favoritesSelect.getItems().clear();
        if (userManager.getCurrentUser() != null) {
            Map<String, Pizza> favorites = userManager.getCurrentUser().getFavorites();
            favoritesSelect.getItems().addAll(favorites.keySet()); // Add only the names to dropdown
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateLoyaltyPointsDisplay() {
        if (userManager.getCurrentUser() != null) {
            loyaltyPointsLabel.setText("Loyalty Points: " + userManager.getCurrentUser().getLoyaltyPoints());
        } else {
            loyaltyPointsLabel.setText("Loyalty Points: 0");
        }
    }

    private Pizza buildPizza(String selectedPreset, PizzaOrderDirector director,
                             ComboBox<String> crustSelect, CheckBox cheeseCheck,
                             CheckBox pepperoniCheck, CheckBox extraCheeseCheck,
                             CheckBox extraSauceCheck) {
        Pizza pizza;
        if ("Margherita".equals(selectedPreset)) {
            pizza = director.buildMargherita();
        } else if ("Pepperoni".equals(selectedPreset)) {
            pizza = director.buildPepperoni();
        } else if ("Veggie Delight".equals(selectedPreset)) {
            pizza = director.buildVeggieDelight();
        } else if ("Custom".equals(selectedPreset)) {
            PizzaBuilder builder = new PizzaBuilder();

            // Initialize handlers
            CustomizationHandler crustHandler = new CrustHandler();
            CustomizationHandler sauceHandler = new SauceHandler();
            CustomizationHandler toppingHandler = new ToppingHandler();

            // Set up chain
            crustHandler.setNextHandler(sauceHandler);
            sauceHandler.setNextHandler(toppingHandler);

            // Handle crust selection
            if (crustSelect.getValue() != null) {
                crustHandler.handle(builder, "Crust: " + crustSelect.getValue());
            }

            // Handle sauce
            sauceHandler.handle(builder, "Sauce: Tomato");

            // Handle toppings through the chain
            if (cheeseCheck.isSelected()) {
                toppingHandler.handle(builder, "Topping: Cheese");
            }
            if (pepperoniCheck.isSelected()) {
                toppingHandler.handle(builder, "Topping: Pepperoni");
            }

            // Build base pizza
            pizza = builder.build();

            // Apply decorators
            if (extraCheeseCheck.isSelected()) {
                pizza = new ExtraCheeseDecorator(pizza);
            }
            if (extraSauceCheck.isSelected()) {
                pizza = new ExtraSauceDecorator(pizza);
            }
        } else {
            return null;
        }

        // Debug output to verify toppings
        System.out.println("Built pizza with toppings: " + pizza.getToppings());

        return pizza;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox createFeedbackSection() {
        VBox feedbackBox = new VBox(10);
        feedbackBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label feedbackLabel = new Label("Order Feedback");
        feedbackLabel.setStyle("-fx-font-weight: bold;");

        Slider ratingSlider = new Slider(1, 5, 5);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setBlockIncrement(1);
        ratingSlider.setSnapToTicks(true);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Enter your feedback...");
        commentArea.setPrefRowCount(3);

        Button submitFeedbackButton = new Button("Submit Feedback");
        submitFeedbackButton.setOnAction(e -> {
            if (currentOrder != null && userManager.getCurrentUser() != null) {
                int rating = (int) ratingSlider.getValue();
                String comment = commentArea.getText();
                currentOrder.addFeedback(rating, comment, userManager.getCurrentUser().getName());
                feedbackManager.addFeedback(currentOrder.getFeedback());

                // Clear inputs
                ratingSlider.setValue(5);
                commentArea.clear();

                // Show confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Feedback Submitted");
                alert.setHeaderText(null);
                alert.setContentText("Thank you for your feedback!");
                alert.showAndWait();
            } else {
                showError("Please log in and place an order before submitting feedback.");
            }
        });

        // Add top-rated pizzas display
        Label topRatedLabel = new Label("Top Rated Pizzas");
        ListView<String> topRatedList = new ListView<>();
        List<String> topPizzas = feedbackManager.getTopRatedPizzas(5);
        topRatedList.getItems().addAll(topPizzas);

        // Add recent feedback preview
        Label recentFeedbackLabel = new Label("Recent Feedback");
        recentFeedbackLabel.setStyle("-fx-font-weight: bold;");

        ListView<String> recentFeedbackList = new ListView<>();
        recentFeedbackList.setPrefHeight(200);

        List<Feedback> recentFeedbacks = feedbackManager.getRecentFeedback(5);
        List<String> formattedFeedbacks = recentFeedbacks.stream()
                .map(f -> String.format("%s (%d⭐) - %s\n%s\nPizza: %s",
                        f.getCustomerName(),
                        f.getRating(),
                        f.getTimestamp().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")),
                        f.getComment(),
                        f.getPizza().toString()))
                .collect(Collectors.toList());

        recentFeedbackList.getItems().addAll(formattedFeedbacks);

        feedbackBox.getChildren().addAll(
                feedbackLabel,
                new Label("Rating:"),
                ratingSlider,
                new Label("Comments:"),
                commentArea,
                submitFeedbackButton,
                topRatedLabel,
                topRatedList,
                recentFeedbackLabel,
                recentFeedbackList
        );

        return feedbackBox;
    }
}