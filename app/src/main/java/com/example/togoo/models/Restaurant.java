package com.example.togoo.models;

import java.util.Map;

public class Restaurant {
    private String id;
    private String name;
    private String address;
    private String imageURL;
    private String email;
    private double rating;
    private double distanceKm;
    private int etaMinutes;
    private LocationCoordinates location;
    private Map<String, OperatingHours> operatingHours;

    public Restaurant() {}

    public Restaurant(String id, String name, String address, String imageURL,
                      LocationCoordinates location, Map<String, OperatingHours> operatingHours,
                      double rating, double distanceKm, int etaMinutes) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.imageURL = imageURL;
        this.location = location;
        this.operatingHours = operatingHours;
        this.rating = rating;
        this.distanceKm = distanceKm;
        this.etaMinutes = etaMinutes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getImageURL() { return imageURL; }
    public String getEmail() { return email; }
    public LocationCoordinates getLocation() { return location; }
    public Map<String, OperatingHours> getOperatingHours() { return operatingHours; }
    public double getRating() { return rating; }
    public double getDistanceKm() { return distanceKm; }
    public int getEtaMinutes() { return etaMinutes; }

    public double getLatitudeAsDouble() {
        if (location != null && location.getLatitude() != null) {
            try {
                return Double.parseDouble(location.getLatitude());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0.0;
    }

    public double getLongitudeAsDouble() {
        if (location != null && location.getLongitude() != null) {
            try {
                return Double.parseDouble(location.getLongitude());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0.0;
    }
}
