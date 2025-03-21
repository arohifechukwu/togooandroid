package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodItem implements Parcelable {
    private String id; // UID
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

    // Parcelable Constructor
    protected FoodItem(Parcel in) {
        id = in.readString();
        description = in.readString();
        imageUrl = in.readString();
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
        dest.writeString(imageUrl);
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
}