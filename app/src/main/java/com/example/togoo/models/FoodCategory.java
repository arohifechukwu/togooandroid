package com.example.togoo.models;

public class FoodCategory {
    private String name;
    private int imageResId; // Stores Drawable Resource ID
    private String imageUrl; // Stores URL for Online Images

    public FoodCategory() {
        // Default constructor for Firebase
    }

    // Constructor for Drawable Resources
    public FoodCategory(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
        this.imageUrl = null; // No URL for local images
    }

    // Constructor for URL Images (if needed)
    public FoodCategory(String name, String imageUrl) {
        this.name = name;
        this.imageResId = 0; // No drawable resource
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}