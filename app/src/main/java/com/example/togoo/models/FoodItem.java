package com.example.togoo.models;

public class FoodItem {
    private String name;
    private String imageUrl;
    private double price;

    public FoodItem() {
        // Default constructor required for Firebase
    }

    public FoodItem(String name, String imageUrl, double price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }
}