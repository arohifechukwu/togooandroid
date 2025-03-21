package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.togoo.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView foodImage;
    private TextView foodName, foodDescription, foodPrice;
    private Button btnAddToCart, btnBuyNow;
    private DatabaseReference cartRef;
    private FirebaseUser currentUser;

    private String foodId, foodDescStr, foodImgUrl;
    private double foodPriceVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // Initialize UI components
        foodImage = findViewById(R.id.foodImage);
        foodName = findViewById(R.id.foodName);
        foodDescription = findViewById(R.id.foodDescription);
        foodPrice = findViewById(R.id.foodPrice);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        // Get logged-in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(currentUser.getUid());
        }

        // Receive Intent Data from CustomerLandingActivity
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("foodId");  // Food name (UID)
            foodDescStr = getIntent().getStringExtra("foodDescription");  // Corrected
            foodImgUrl = getIntent().getStringExtra("foodImage");
            foodPriceVal = getIntent().getDoubleExtra("foodPrice", 0.0);

            // Set Data to Views
            foodName.setText(foodId);  // Using foodId as the name
            foodDescription.setText(foodDescStr);
            foodPrice.setText("$" + foodPriceVal);

            Glide.with(this)
                    .load(foodImgUrl)
                    .placeholder(R.drawable.ic_food_placeholder)
                    .into(foodImage);
        }

        // Add to Cart Functionality
        btnAddToCart.setOnClickListener(v -> addToCart());

        // Buy Now Functionality
        btnBuyNow.setOnClickListener(v -> buyNow());
    }

    // 🔹 Add food item to Firebase Cart
    private void addToCart() {
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to add items to the cart.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userCartRef = FirebaseDatabase.getInstance()
                .getReference("cart")
                .child(currentUser.getUid());

        // Create a new CartItem (quantity initially 1)
        CartItem cartItem = new CartItem(foodId, foodDescStr, foodImgUrl, foodPriceVal, 1);

        // Use push() to add a new entry so each duplicate gets its own unique key
        DatabaseReference newItemRef = userCartRef.push();
        newItemRef.setValue(cartItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FoodDetailActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FoodDetailActivity.this, "Failed to Add to Cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Navigate to Checkout with selected item
    private void buyNow() {
        Intent intent = new Intent(FoodDetailActivity.this, CheckoutActivity.class);

        // Create CartItem with 1 quantity
        CartItem cartItem = new CartItem(foodId, foodDescStr, foodImgUrl, foodPriceVal, 1);

        ArrayList<CartItem> checkoutList = new ArrayList<>();
        checkoutList.add(cartItem);

        // Pass cart item to CheckoutActivity
        intent.putParcelableArrayListExtra("cartItems", checkoutList);
        startActivity(intent);
    }
}