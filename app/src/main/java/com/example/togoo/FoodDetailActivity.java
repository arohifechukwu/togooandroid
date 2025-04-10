package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.togoo.models.Restaurant;
import com.example.togoo.utils.RestaurantHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        Restaurant restaurant = getIntent().getParcelableExtra("restaurant");
        if (restaurant != null) {
            RestaurantHelper.setCurrentRestaurant(restaurant);
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
            foodId = getIntent().getStringExtra("foodId");
            foodDescStr = getIntent().getStringExtra("foodDescription");
            foodImgUrl = getIntent().getStringExtra("foodImage");
            foodPriceVal = getIntent().getDoubleExtra("foodPrice", 0.0);
            Log.d("FoodDetailActivity", "Intent: id=" + foodId + ", desc=" + foodDescStr +
                    ", image=" + foodImgUrl + ", price=" + foodPriceVal);

            foodName.setText(foodId);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
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

        Restaurant restaurant = RestaurantHelper.getCurrentRestaurant();
        if (restaurant == null) {
            String restaurantId = getIntent().getStringExtra("restaurantId");
            if (restaurantId != null) {
                fetchRestaurantAndProceed(restaurantId, this::addToCartWithRestaurant);
            } else {
                Toast.makeText(this, "Restaurant info missing", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        addToCartWithRestaurant(restaurant);
    }

    private void addToCartWithRestaurant(Restaurant restaurant) {
        String uid = currentUser.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("cart").child(uid);
        CartItem cartItem = new CartItem(
                foodId,
                foodDescStr,
                foodImgUrl,
                restaurant.getId(), // Ensure this is not null
                foodPriceVal,
                1
        );
        Log.d("AddToCart", "CartItem: id=" + cartItem.getFoodId() +
                ", desc=" + cartItem.getFoodDescription() +
                ", image=" + cartItem.getFoodImage() +
                ", price=" + cartItem.getFoodPrice() +
                ", restaurantId=" + cartItem.getRestaurantId());

        cartRef.push().setValue(cartItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FoodDetailActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FoodDetailActivity.this, "Failed to Add to Cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buyNow() {
        Restaurant restaurant = RestaurantHelper.getCurrentRestaurant();
        if (restaurant == null) {
            String restaurantId = getIntent().getStringExtra("restaurantId");
            if (restaurantId != null) {
                fetchRestaurantAndProceed(restaurantId, this::buyNowWithRestaurant);
            } else {
                Toast.makeText(this, "Restaurant info missing", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        buyNowWithRestaurant(restaurant);
    }

    private void buyNowWithRestaurant(Restaurant restaurant) {
        if (restaurant.getId() == null || restaurant.getId().isEmpty()) {
            Toast.makeText(this, "Restaurant data incomplete.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(FoodDetailActivity.this, CheckoutActivity.class);
        CartItem cartItem = new CartItem(
                foodId,
                foodDescStr,
                foodImgUrl,
                restaurant.getId(),
                foodPriceVal,
                1
        );
        ArrayList<CartItem> checkoutList = new ArrayList<>();
        checkoutList.add(cartItem);
        Log.d("BuyNow", "CartItem: id=" + cartItem.getFoodId() +
                ", desc=" + cartItem.getFoodDescription() +
                ", image=" + cartItem.getFoodImage() +
                ", price=" + cartItem.getFoodPrice() +
                ", restaurantId=" + cartItem.getRestaurantId());

        intent.putParcelableArrayListExtra("cartItems", checkoutList);
        intent.putExtra("selectedRestaurant", restaurant);
        startActivity(intent);
    }


    private void fetchRestaurantAndProceed(String restaurantId, Consumer<Restaurant> callback) {
        DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantId);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);
                if (restaurant != null) {
                    if (restaurant.getId() == null || restaurant.getId().isEmpty()) {
                        restaurant.setId(snapshot.getKey()); // Ensure id is set
                    }
                    RestaurantHelper.setCurrentRestaurant(restaurant);
                    callback.accept(restaurant);
                } else {
                    Toast.makeText(FoodDetailActivity.this, "Restaurant info missing", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodDetailActivity.this, "Failed to load restaurant data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add this interface for the callback
    interface Consumer<T> {
        void accept(T t);
    }
}