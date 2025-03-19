package com.example.togoo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.FoodCategoryAdapter;
import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.models.FoodCategory;
import com.example.togoo.models.FoodItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CustomerLandingActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView locationText;
    private ImageButton cartButton;
    private EditText searchBar;
    private RecyclerView featuredCategoriesRecyclerView, specialOffersRecyclerView, topPicksRecyclerView;
    private FusedLocationProviderClient fusedLocationClient;
    private BottomNavigationView bottomNavigation;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_landing);

        // Initialize UI components
        locationText = findViewById(R.id.locationText);
        cartButton = findViewById(R.id.cartButton);
        searchBar = findViewById(R.id.searchBar);
        featuredCategoriesRecyclerView = findViewById(R.id.featuredCategoriesRecyclerView);
        specialOffersRecyclerView = findViewById(R.id.specialOffersRecyclerView);
        topPicksRecyclerView = findViewById(R.id.topPicksRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        dbReference = FirebaseDatabase.getInstance().getReference();

        // Initialize location client and fetch location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchCustomerLocation();

        // Set up cart button to navigate to CartActivity
        cartButton.setOnClickListener(v -> startActivity(new Intent(CustomerLandingActivity.this, CartActivity.class)));

        // Set up search bar listener for menu search across all restaurants
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* no-op */ }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    searchMenuItems(s.toString().trim());
                }
            }
            @Override
            public void afterTextChanged(Editable s) { /* no-op */ }
        });

        // Set up RecyclerViews
        setupRecyclerViews();

        // Bottom Navigation setup
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> onNavigationItemSelected(item.getItemId()));
    }

    // 🔹 Fetch Customer Location
    private void fetchCustomerLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                String locStr = "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude();
                locationText.setText(locStr);
            } else {
                locationText.setText("Location Unavailable");
            }
        });
    }

    // 🔹 Search Menu Items in Firebase
    private void searchMenuItems(String query) {
        dbReference.child("restaurants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> searchResults = new ArrayList<>();
                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    for (DataSnapshot menuItem : restaurant.child("menu").getChildren()) {
                        FoodItem item = menuItem.getValue(FoodItem.class);
                        if (item != null && item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                            searchResults.add(item);
                        }
                    }
                }
                if (!searchResults.isEmpty()) {
                    Intent intent = new Intent(CustomerLandingActivity.this, SearchActivity.class);
                    intent.putExtra("searchResults", new ArrayList<>(searchResults));
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // 🔹 Set up RecyclerViews
    // 🔹 Set up RecyclerViews
    private void setupRecyclerViews() {
        featuredCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredCategoriesRecyclerView.setAdapter(new FoodCategoryAdapter(this, getFeaturedCategories(), category -> {
            // Handle category click (navigate to category-specific menu)
        }));

        specialOffersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchSpecialOffers();

        topPicksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchTopPicks();
    }

    // 🔹 Generate Featured Categories
    private List<FoodCategory> getFeaturedCategories() {
        List<FoodCategory> categories = new ArrayList<>();
        categories.add(new FoodCategory("Pizza 🍕", R.drawable.ic_food_category_placeholder));
        categories.add(new FoodCategory("Burgers 🍔", R.drawable.ic_food_category_placeholder));
        categories.add(new FoodCategory("Sushi 🍣", R.drawable.ic_food_category_placeholder));
        categories.add(new FoodCategory("Pasta 🍝", R.drawable.ic_food_category_placeholder));
        categories.add(new FoodCategory("Fried Chicken 🍗", R.drawable.ic_food_category_placeholder));
        categories.add(new FoodCategory("BBQ & Grilled 🍖", R.drawable.ic_food_category_placeholder));
        categories.add(new FoodCategory("Tacos 🌮", R.drawable.ic_food_category_placeholder));
        categories.add(new FoodCategory("Sandwiches & Subs 🥪", R.drawable.ic_food_category_placeholder));
        return categories;
    }

    private void fetchTopPicks() {
        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> topPicks = new ArrayList<>();

                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    for (DataSnapshot pick : restaurant.child("Top Picks").getChildren()) {
                        String id = pick.getKey(); // Fetch UID (e.g., "Cheeseburger")
                        String description = pick.child("description").getValue(String.class);
                        String imageUrl = pick.child("imageURL").getValue(String.class);
                        Double price = pick.child("price").getValue(Double.class);

                        if (id != null && description != null && imageUrl != null && price != null) {
                            topPicks.add(new FoodItem(id, description, imageUrl, price));
                        }
                    }
                }

                // Set adapter for RecyclerView
                topPicksRecyclerView.setAdapter(new FoodAdapter(CustomerLandingActivity.this, topPicks, foodItem -> {
                    // Navigate to FoodDetailActivity and pass relevant data
                    Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId()); // e.g., Cheeseburger
                    intent.putExtra("foodDescription", foodItem.getDescription()); // Description
                    intent.putExtra("foodImage", foodItem.getImageUrl()); // Image URL
                    intent.putExtra("foodPrice", foodItem.getPrice()); // Price
                    startActivity(intent);
                }));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerLandingActivity.this, "Failed to load top picks.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchSpecialOffers() {
        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> specialOffers = new ArrayList<>();

                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    for (DataSnapshot offer : restaurant.child("Special Offers").getChildren()) {
                        // Fetch Data Correctly
                        String id = offer.getKey(); // Fetch UID (e.g., "Apple Pie")
                        String description = offer.child("description").getValue(String.class);
                        String imageUrl = offer.child("imageURL").getValue(String.class);
                        Double price = offer.child("price").getValue(Double.class);

                        if (id != null && description != null && imageUrl != null && price != null) {
                            specialOffers.add(new FoodItem(id, description, imageUrl, price));
                        }
                    }
                }

                // Set adapter for RecyclerView
                specialOffersRecyclerView.setAdapter(new FoodAdapter(CustomerLandingActivity.this, specialOffers, foodItem -> {
                    // Navigate to FoodDetailActivity and pass relevant data
                    Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageUrl());
                    intent.putExtra("foodPrice", foodItem.getPrice());
                    startActivity(intent);
                }));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerLandingActivity.this, "Failed to load special offers.", Toast.LENGTH_SHORT).show();
            }
        });
    }


// 🔹 Handle Bottom Navigation Clicks (Java 11 Fix)

    private boolean onNavigationItemSelected(@NonNull int item) {
        int id = item;

        if (id == R.id.navigation_home) {
            return true;
        } else if (id == R.id.navigation_restaurant) {
            startActivity(new Intent(this, RestaurantActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_browse) {
            startActivity(new Intent(this, BrowseActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_order) {
            startActivity(new Intent(this, OrderActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_account) {
            startActivity(new Intent(this, AccountActivity.class));
            finish();
            return true;
        }
        return false;
    }
}
