package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

public class FoodItem implements Parcelable {
    private String id;             // UID
    private String description;
    private String imageURL;
    private double price;
    private String restaurantId;
    private String parentNode;     // e.g., "menu", "Top Picks"
    private String category;       // e.g., "Pizza", "Burgers" (null for non-menu items)

    public FoodItem() {}

    public FoodItem(String id, String description, String imageURL, String restaurantId, double price) {
        this.id = id;
        this.description = description;
        this.imageURL = imageURL;
        this.restaurantId = restaurantId;
        this.price = price;
    }

    // Explicitly map fields from Firebase snapshot
    public static FoodItem fromSnapshot(DataSnapshot snapshot, String restaurantId) {
        String id = snapshot.getKey();
        String description = snapshot.child("description").getValue(String.class);
        String imageURL = snapshot.child("imageURL").getValue(String.class);
        Double price = snapshot.child("price").getValue(Double.class);
        if (id != null && description != null && imageURL != null && price != null) {
            return new FoodItem(id, description, imageURL, restaurantId, price);
        }
        return null;
    }

    // Parcelable constructor
    protected FoodItem(Parcel in) {
        restaurantId = in.readString();  // Match write order
        id = in.readString();
        description = in.readString();
        imageURL = in.readString();
        price = in.readDouble();
        parentNode = in.readString();
        category = in.readString();
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
        dest.writeString(restaurantId != null ? restaurantId : "");  // Handle nulls
        dest.writeString(id != null ? id : "");
        dest.writeString(description != null ? description : "");
        dest.writeString(imageURL != null ? imageURL : "");
        dest.writeDouble(price);
        dest.writeString(parentNode != null ? parentNode : "");
        dest.writeString(category != null ? category : "");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public String getRestaurantId() { return restaurantId; }
    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getImageURL() { return imageURL; }
    public double getPrice() { return price; }
    public String getParentNode() { return parentNode; }
    public String getCategory() { return category; }

    // Setters
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setId(String id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public void setPrice(Object price) {
        if (price instanceof String) {
            try {
                this.price = Double.parseDouble((String) price);
            } catch (NumberFormatException e) {
                this.price = 0.0;
                Log.e("FoodItem", "Invalid price format: " + price);
            }
        } else if (price instanceof Double) {
            this.price = (Double) price;
        } else {
            this.price = 0.0;
        }
    }
    public void setParentNode(String parentNode) { this.parentNode = parentNode; }
    public void setCategory(String category) { this.category = category; }
}