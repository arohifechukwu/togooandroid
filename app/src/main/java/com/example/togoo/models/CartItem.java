package com.example.togoo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String cartItemId; // Unique key from Firebase push()
    private String foodId;
    private String foodDescription;
    private String foodImage;
    private double foodPrice;
    private int quantity;
    private String restaurantId;

    public CartItem() { }

    public CartItem(String foodId, String foodDescription, String foodImage, String restaurantId, double foodPrice, int quantity) {
        this.foodId = foodId;
        this.foodDescription = foodDescription;
        this.foodImage = foodImage;
        this.restaurantId = restaurantId;
        this.foodPrice = foodPrice;
        this.quantity = quantity;
    }

    protected CartItem(Parcel in) {
        cartItemId = in.readString();
        foodId = in.readString();
        foodDescription = in.readString();
        foodImage = in.readString();
        restaurantId = in.readString();
        foodPrice = in.readDouble();
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    // Getters and setters
    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public String getFoodDescription() { return foodDescription; }
    public void setFoodDescription(String foodDescription) { this.foodDescription = foodDescription; }
    public String getFoodImage() { return foodImage; }
    public void setFoodImage(String foodImage) { this.foodImage = foodImage; }
    public double getFoodPrice() { return foodPrice; }
    public void setFoodPrice(double foodPrice) { this.foodPrice = foodPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cartItemId);
        dest.writeString(foodId);
        dest.writeString(foodDescription);
        dest.writeString(foodImage);
        dest.writeString(restaurantId);
        dest.writeDouble(foodPrice);
        dest.writeInt(quantity);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}