package com.example.togoo.models;

public class FoodCategory {
    private String name;
    private String imageUrl;

    public FoodCategory() {
        // Default constructor required for Firebase
    }

    public FoodCategory(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}