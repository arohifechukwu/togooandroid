//package com.example.togoo.models;
//
//import java.util.Map;
//
//public class Restaurant {
//    private String id;
//    private String name;
//    private String address;
//    private String imageURL;
//    private String email;
//    private double rating;
//    private double distanceKm;
//    private int etaMinutes;
//    private LocationCoordinates location;
//    private String restaurantLicense;
//    private String retailLicense;
//    private Map<String, OperatingHours> operatingHours;
//
//    // 🔹 Default constructor (required for Firebase)
//    public Restaurant() {}
//
//    // 🔹 Main constructor used in app logic (Firebase usually doesn't use this)
//    public Restaurant(String id, String name, String address, String imageURL,
//                      LocationCoordinates location, Map<String, OperatingHours> operatingHours,
//                      double rating, double distanceKm, int etaMinutes) {
//        this.id = id;
//        this.name = name;
//        this.address = address;
//        this.imageURL = imageURL;
//        this.location = location;
//        this.operatingHours = operatingHours;
//        this.rating = rating;
//        this.distanceKm = distanceKm;
//        this.etaMinutes = etaMinutes;
//    }
//
//    // 🔹 Getters
//    public String getId() { return id; }
//    public String getName() { return name; }
//    public String getAddress() { return address; }
//    public String getImageURL() { return imageURL; }
//    public String getEmail() { return email; }
//    public String getRestaurantLicense() { return restaurantLicense; }
//    public String getRetailLicense() { return retailLicense; }
//    public LocationCoordinates getLocation() { return location; }
//    public Map<String, OperatingHours> getOperatingHours() { return operatingHours; }
//    public double getRating() { return rating; }
//    public double getDistanceKm() { return distanceKm; }
//    public int getEtaMinutes() { return etaMinutes; }
//
//    // 🔹 Setters (important for Firebase)
//    public void setId(String id) { this.id = id; }
//    public void setName(String name) { this.name = name; }
//    public void setAddress(String address) { this.address = address; }
//    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
//    public void setEmail(String email) { this.email = email; }
//    public void setRestaurantLicense(String restaurantLicense) { this.restaurantLicense = restaurantLicense; }
//    public void setRetailLicense(String retailLicense) { this.retailLicense = retailLicense; }
//    public void setLocation(LocationCoordinates location) { this.location = location; }
//    public void setOperatingHours(Map<String, OperatingHours> operatingHours) {
//        this.operatingHours = operatingHours;
//    }
//    public void setRating(double rating) { this.rating = rating; }
//    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
//    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }
//
//    // 🔹 Convenience methods
//    public double getLatitudeAsDouble() {
//        return location != null ? location.getLatitudeAsDouble() : 0.0;
//    }
//
//    public double getLongitudeAsDouble() {
//        return location != null ? location.getLongitudeAsDouble() : 0.0;
//    }
//}





package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class Restaurant implements Parcelable {
    private String id;
    private String name;
    private String address;
    private String imageURL;
    private String email;
    private double rating;
    private double distanceKm;
    private int etaMinutes;
    private LocationCoordinates location;
    private String restaurantLicense;
    private String retailLicense;
    private Map<String, OperatingHours> operatingHours;

    // 🔹 Default constructor (required for Firebase)
    public Restaurant() {}

    // 🔹 Main constructor used in app logic (Firebase usually doesn't use this)
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

    // 🔹 Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getImageURL() { return imageURL; }
    public String getEmail() { return email; }
    public String getRestaurantLicense() { return restaurantLicense; }
    public String getRetailLicense() { return retailLicense; }
    public LocationCoordinates getLocation() { return location; }
    public Map<String, OperatingHours> getOperatingHours() { return operatingHours; }
    public double getRating() { return rating; }
    public double getDistanceKm() { return distanceKm; }
    public int getEtaMinutes() { return etaMinutes; }

    // 🔹 Setters (important for Firebase)
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public void setEmail(String email) { this.email = email; }
    public void setRestaurantLicense(String restaurantLicense) { this.restaurantLicense = restaurantLicense; }
    public void setRetailLicense(String retailLicense) { this.retailLicense = retailLicense; }
    public void setLocation(LocationCoordinates location) { this.location = location; }
    public void setOperatingHours(Map<String, OperatingHours> operatingHours) {
        this.operatingHours = operatingHours;
    }
    public void setRating(double rating) { this.rating = rating; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }

    // 🔹 Convenience methods
    public double getLatitudeAsDouble() {
        return location != null ? location.getLatitudeAsDouble() : 0.0;
    }

    public double getLongitudeAsDouble() {
        return location != null ? location.getLongitudeAsDouble() : 0.0;
    }

    // 🔹 Parcelable implementation
    protected Restaurant(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        imageURL = in.readString();
        email = in.readString();
        rating = in.readDouble();
        distanceKm = in.readDouble();
        etaMinutes = in.readInt();
        location = in.readParcelable(LocationCoordinates.class.getClassLoader());
        restaurantLicense = in.readString();
        retailLicense = in.readString();
        // operatingHours is not parcelable
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(imageURL);
        dest.writeString(email);
        dest.writeDouble(rating);
        dest.writeDouble(distanceKm);
        dest.writeInt(etaMinutes);
        dest.writeParcelable(location, flags);
        dest.writeString(restaurantLicense);
        dest.writeString(retailLicense);
        // operatingHours not included
    }
}
