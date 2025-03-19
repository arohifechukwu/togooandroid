package com.example.togoo.models;

public class FoodItem {
    private String id; // UID (e.g., "Apple Pie")
    private String description; // Correct Field for Description
    private String imageUrl;
    private double price;

    public FoodItem() { }

    public FoodItem(String id, String description, String imageUrl, double price) {
        this.id = id;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getId() { return id; }
    public String getDescription() { return description; } // Fixed Naming Issue
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
}