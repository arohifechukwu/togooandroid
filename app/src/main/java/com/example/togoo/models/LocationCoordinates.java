package com.example.togoo.models;

public class LocationCoordinates {
    private String latitude;
    private String longitude;

    public LocationCoordinates() {}

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public double getLatitudeAsDouble() {
        try {
            return Double.parseDouble(latitude);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getLongitudeAsDouble() {
        try {
            return Double.parseDouble(longitude);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
