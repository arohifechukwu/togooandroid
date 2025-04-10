package com.example.togoo.utils;

import com.example.togoo.models.Restaurant;

public class RestaurantHelper {

    private static Restaurant currentRestaurant;

    // Set globally accessible restaurant
    public static void setCurrentRestaurant(Restaurant restaurant) {
        currentRestaurant = restaurant;
    }

    // Get globally accessible restaurant
    public static Restaurant getCurrentRestaurant() {
        return currentRestaurant;
    }

    // Optionally resolve if one is missing
    public static Restaurant resolveSelectedRestaurant(Restaurant selectedRestaurant) {
        return selectedRestaurant != null ? selectedRestaurant : currentRestaurant;
    }

    // Optional check
    public static boolean isRestaurantSet() {
        return currentRestaurant != null;
    }
}