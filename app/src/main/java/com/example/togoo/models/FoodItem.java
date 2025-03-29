package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodItem implements Parcelable {
    private String id;             // UID
    private String description;    // Description of the food
    private String imageURL;       // Corrected field name to match Firebase
    private double price;

    public FoodItem() {}

    public FoodItem(String id, String description, String imageURL, double price) {
        this.id = id;
        this.description = description;
        this.imageURL = imageURL;
        this.price = price;
    }

    // Parcelable constructor
    protected FoodItem(Parcel in) {
        id = in.readString();
        description = in.readString();
        imageURL = in.readString();
        price = in.readDouble();
    }

    public static final Creator<FoodItem> CREATOR = new Creator<FoodItem>() {
        @Override
        public FoodItem createFromParcel(Parcel in) {
            return new FoodItem(in);
        }

        @Override
        public FoodItem[] newArray(int size) {
            return new FoodItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(imageURL);
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getImageURL() { return imageURL; }  // Updated getter name
    public double getPrice() { return price; }

    // Optional: Setters if needed
    public void setId(String id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public void setPrice(double price) { this.price = price; }
}