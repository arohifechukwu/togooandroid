package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.togoo.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FoodDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
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

        // Set up Toolbar with custom back icon
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Set custom back icon; ensure ic_back is present in drawable resources
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

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

        // Receive Intent Data from previous activity
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("foodId");  // Food UID used as name
            foodDescStr = getIntent().getStringExtra("foodDescription");
            foodImgUrl = getIntent().getStringExtra("foodImage");
            foodPriceVal = getIntent().getDoubleExtra("foodPrice", 0.0);

            // Set Data to Views
            foodName.setText(foodId);  // Using UID as the displayed name
            foodDescription.setText(foodDescStr);
            foodPrice.setText("$" + foodPriceVal);

            Glide.with(this)
                    .load(foodImgUrl)
                    .placeholder(R.drawable.ic_food_placeholder)
                    .into(foodImage);
        }

        // Add to Cart functionality
        btnAddToCart.setOnClickListener(v -> addToCart());

        // Buy Now functionality
        btnBuyNow.setOnClickListener(v -> buyNow());
    }

    // Handle custom back button press in Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();  // Finish activity and navigate back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addToCart() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to add items to the cart.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("cart").child(uid);

        // Create a new CartItem (with quantity 1)
        CartItem cartItem = new CartItem(foodId, foodDescStr, foodImgUrl, foodPriceVal, 1);

        // Use push() to add a new entry
        cartRef.push().setValue(cartItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FoodDetailActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FoodDetailActivity.this, "Failed to Add to Cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navigate directly to CheckoutActivity with the selected item
    private void buyNow() {
        Intent intent = new Intent(FoodDetailActivity.this, CheckoutActivity.class);

        // Create a CartItem with quantity 1
        CartItem cartItem = new CartItem(foodId, foodDescStr, foodImgUrl, foodPriceVal, 1);
        ArrayList<CartItem> checkoutList = new ArrayList<>();
        checkoutList.add(cartItem);

        intent.putParcelableArrayListExtra("cartItems", checkoutList);
        startActivity(intent);
    }
}