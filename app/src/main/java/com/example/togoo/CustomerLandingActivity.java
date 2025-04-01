//package com.example.togoo;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Address;
//import android.location.Geocoder;
//import android.os.Bundle;
//import android.os.Looper;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.view.View;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.togoo.adapters.FoodCategoryAdapter;
//import com.example.togoo.adapters.FoodAdapter;
//import com.example.togoo.models.FoodCategory;
//import com.example.togoo.models.FoodItem;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//public class CustomerLandingActivity extends AppCompatActivity {
//
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
//
//    private TextView locationText, viewResultsButton;
//    private ImageButton cartButton;
//    private EditText searchBar;
//    private RecyclerView featuredCategoriesRecyclerView, specialOffersRecyclerView, topPicksRecyclerView, searchSuggestionsRecyclerView;
//    private FusedLocationProviderClient fusedLocationClient;
//    private BottomNavigationView bottomNavigation;
//    private DatabaseReference dbReference;
//    private FoodAdapter searchAdapter; // Adapter for suggestions
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_customer_landing);
//
//        // Initialize UI components
//        locationText = findViewById(R.id.locationText);
//        cartButton = findViewById(R.id.cartButton);
//        searchBar = findViewById(R.id.searchBar);
//        featuredCategoriesRecyclerView = findViewById(R.id.featuredCategoriesRecyclerView);
//        specialOffersRecyclerView = findViewById(R.id.specialOffersRecyclerView);
//        topPicksRecyclerView = findViewById(R.id.topPicksRecyclerView);
//        bottomNavigation = findViewById(R.id.bottomNavigation);
//        dbReference = FirebaseDatabase.getInstance().getReference();
//
//        // Initialize existing UI components
//        locationText = findViewById(R.id.locationText);
//        cartButton = findViewById(R.id.cartButton);
//        searchBar = findViewById(R.id.searchBar);
//        featuredCategoriesRecyclerView = findViewById(R.id.featuredCategoriesRecyclerView);
//        specialOffersRecyclerView = findViewById(R.id.specialOffersRecyclerView);
//        topPicksRecyclerView = findViewById(R.id.topPicksRecyclerView);
//        bottomNavigation = findViewById(R.id.bottomNavigation);
//        dbReference = FirebaseDatabase.getInstance().getReference();
//
//        // Initialize the new search suggestions UI elements
//        searchSuggestionsRecyclerView = findViewById(R.id.searchSuggestionsRecyclerView);
//        viewResultsButton = findViewById(R.id.viewResultsButton);
//        searchAdapter = new FoodAdapter(this, new ArrayList<>(), foodItem -> {
//            // Open FoodDetailActivity when a suggestion is clicked
//            Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
//            intent.putExtra("foodId", foodItem.getId());
//            intent.putExtra("foodDescription", foodItem.getDescription());
//            intent.putExtra("foodImage", foodItem.getImageURL());
//            intent.putExtra("foodPrice", foodItem.getPrice());
//            startActivity(intent);
//        });
//        searchSuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        searchSuggestionsRecyclerView.setAdapter(searchAdapter);
//        searchSuggestionsRecyclerView.setVisibility(View.GONE);
//        viewResultsButton.setVisibility(View.GONE);
//
//        // Initialize location client and fetch location
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        fetchCustomerLocation();
//
//        // Set up cart button to navigate to CartActivity
//        cartButton.setOnClickListener(v -> startActivity(new Intent(CustomerLandingActivity.this, CartActivity.class)));
//
//        // Set up search bar listener
//        searchBar.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* no-op */ }
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String query = s.toString().trim();
//                if (!query.isEmpty()) {
//                    searchMenuItems(query);
//                } else {
//                    searchSuggestionsRecyclerView.setVisibility(View.GONE);
//                    viewResultsButton.setVisibility(View.GONE);
//                }
//            }
//            @Override
//            public void afterTextChanged(Editable s) { /* no-op */ }
//        });
//
//
//        // Set up RecyclerViews
//        setupRecyclerViews();
//
//        // Bottom Navigation setup
//        bottomNavigation.setSelectedItemId(R.id.navigation_home);
//        bottomNavigation.setOnNavigationItemSelectedListener(item -> onNavigationItemSelected(item.getItemId()));
//    }
//
//    // 🔹 Fetch Customer Location
//
//    private void fetchCustomerLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
//            return;
//        }
//
//        LocationRequest locationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Forces GPS for accuracy
//                .setInterval(5000) // Updates every 5 seconds
//                .setFastestInterval(2000);
//
//        LocationCallback locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null || locationResult.getLastLocation() == null) return;
//
//                double latitude = locationResult.getLastLocation().getLatitude();
//                double longitude = locationResult.getLastLocation().getLongitude();
//
//                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//                if (currentUser != null) {
//                    updateUserLocation(currentUser.getUid(), latitude, longitude);
//                }
//
//                // Convert latitude & longitude to a readable address
//                String locationName = getAddressFromCoordinates(latitude, longitude);
//                locationText.setText(locationName);
//            }
//        };
//
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
//    }
//
//
//    private void updateUserLocation(String userId, double latitude, double longitude) {
//        if (userId == null || userId.isEmpty()) {
//            Log.e("FirebaseError", "Invalid user ID.");
//            return;
//        }
//
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//        String[] userTypes = {"customer", "admin", "restaurant", "driver"};
//
//        // Loop through each role to find where the user exists
//        for (String userType : userTypes) {
//            DatabaseReference userRef = rootRef.child(userType).child(userId);
//            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        // Update location in the correct node
//                        DatabaseReference locationRef = userRef.child("location");
//                        locationRef.child("latitude").setValue(latitude);
//                        locationRef.child("longitude").setValue(longitude)
//                                .addOnSuccessListener(aVoid -> Log.d("FirebaseSuccess", "Location updated for " + userType))
//                                .addOnFailureListener(e -> Log.e("FirebaseError", "Failed to update location: " + e.getMessage()));
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e("FirebaseError", "Failed to check user role: " + error.getMessage());
//                }
//            });
//        }
//    }
//
//
//    private String getAddressFromCoordinates(double latitude, double longitude) {
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        List<Address> addresses;
//        try {
//            addresses = geocoder.getFromLocation(latitude, longitude, 1);
//            if (addresses != null && !addresses.isEmpty()) {
//                return addresses.get(0).getLocality(); // Returns city name (e.g., Montreal)
//            } else {
//                // Retry once after a small delay
//                Thread.sleep(1000);
//                addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                if (addresses != null && !addresses.isEmpty()) {
//                    return addresses.get(0).getLocality();
//                }
//            }
//        } catch (IOException | InterruptedException e) {
//            Log.e("GeocoderError", "Error fetching address: " + e.getMessage());
//        }
//        return "Unknown Location";
//    }
//
//
//    // 🔹 Search Menu Items in Firebase
//    private void searchMenuItems(String query) {
//        // Use the "restaurant" node in your database
//        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<FoodItem> prefixMatches = new ArrayList<>();
//                List<FoodItem> substringMatches = new ArrayList<>();
//                String queryLower = query.toLowerCase();
//
//                // Iterate through each restaurant
//                for (DataSnapshot restaurant : snapshot.getChildren()) {
//                    DataSnapshot menuNode = restaurant.child("menu");
//                    if (menuNode.exists()) {
//                        // Iterate through each category under "menu"
//                        for (DataSnapshot categoryNode : menuNode.getChildren()) {
//                            // For each food item under the category
//                            for (DataSnapshot foodSnapshot : categoryNode.getChildren()) {
//                                String foodId = foodSnapshot.getKey(); // UID used as food name
//                                if (foodId != null) {
//                                    // Check if the UID starts with the query (prefix match)
//                                    if (foodId.toLowerCase().startsWith(queryLower)) {
//                                        FoodItem item = foodSnapshot.getValue(FoodItem.class);
//                                        if (item != null) {
//                                            // Overwrite id with the UID
//                                            item = new FoodItem(foodId, item.getDescription(), item.getImageURL(), item.getPrice());
//                                            prefixMatches.add(item);
//                                        }
//                                    } else if (foodId.toLowerCase().contains(queryLower)) {
//                                        // Substring match (UID contains the query elsewhere)
//                                        FoodItem item = foodSnapshot.getValue(FoodItem.class);
//                                        if (item != null) {
//                                            item = new FoodItem(foodId, item.getDescription(), item.getImageURL(), item.getPrice());
//                                            substringMatches.add(item);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // Merge lists with prefixMatches prioritized
//                List<FoodItem> mergedResults = new ArrayList<>();
//                mergedResults.addAll(prefixMatches);
//                mergedResults.addAll(substringMatches);
//
//                if (!mergedResults.isEmpty()) {
//                    // Limit suggestions to 5 items
//                    List<FoodItem> suggestions = new ArrayList<>();
//                    int suggestionCount = Math.min(mergedResults.size(), 5);
//                    for (int i = 0; i < suggestionCount; i++) {
//                        suggestions.add(mergedResults.get(i));
//                    }
//                    // Update suggestions adapter
//                    searchAdapter.updateData(suggestions);
//                    searchSuggestionsRecyclerView.setVisibility(View.VISIBLE);
//
//                    // Update view results button text with total count
//                    viewResultsButton.setText("View all " + mergedResults.size() + " results");
//                    viewResultsButton.setVisibility(View.VISIBLE);
//                    viewResultsButton.setOnClickListener(v -> {
//                        Intent intent = new Intent(CustomerLandingActivity.this, ViewAllActivity.class);
//                        intent.putParcelableArrayListExtra("searchResults", new ArrayList<>(mergedResults));
//                        startActivity(intent);
//                    });
//                } else {
//                    // No results found: clear suggestions and show appropriate message
//                    searchAdapter.updateData(new ArrayList<>());
//                    searchSuggestionsRecyclerView.setVisibility(View.GONE);
//                    viewResultsButton.setText("No results found");
//                    viewResultsButton.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Optionally handle error here
//            }
//        });
//    }
//
//
//
//
//    // 🔹 Set up RecyclerViews
//    private void setupRecyclerViews() {
//        featuredCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        featuredCategoriesRecyclerView.setAdapter(new FoodCategoryAdapter(this, getFeaturedCategories(), category -> {
//            Intent intent = new Intent(CustomerLandingActivity.this, FeaturedCategoryActivity.class);
//            intent.putExtra("selectedCategory", getSearchKeyword(category.getName()));
//            startActivity(intent);
//        }));
//
//        specialOffersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        fetchSpecialOffers();
//
//        topPicksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        fetchTopPicks();
//    }
//
//    // 🔹 Generate Featured Categories
//    private List<FoodCategory> getFeaturedCategories() {
//        List<FoodCategory> categories = new ArrayList<>();
//        categories.add(new FoodCategory("Pizza", R.drawable.pizza));
//        categories.add(new FoodCategory("Burgers", R.drawable.burger));
//        categories.add(new FoodCategory("Sushi", R.drawable.sushi));
//        categories.add(new FoodCategory("Pasta", R.drawable.spaghetti));
//        categories.add(new FoodCategory("Seafood", R.drawable.shrimp));
//        categories.add(new FoodCategory("Salads", R.drawable.salad));
//        categories.add(new FoodCategory("Tacos", R.drawable.tacos));
//        categories.add(new FoodCategory("Desserts", R.drawable.cupcake));
//        return categories;
//    }
//
//    private String getSearchKeyword(String categoryName) {
//        switch (categoryName) {
//            case "Pizza": return "Pizza";
//            case "Burgers": return "Burgers";
//            case "Sushi": return "Sushi";
//            case "Pasta": return "Pasta";
//            case "Seafood": return "Seafood";
//            case "Salads": return "Salads";
//            case "Tacos": return "Tacos";
//            case "Desserts": return "Desserts";
//            default: return "";
//        }
//    }
//
//    private void fetchTopPicks() {
//        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<FoodItem> topPicks = new ArrayList<>();
//
//                for (DataSnapshot restaurant : snapshot.getChildren()) {
//                    for (DataSnapshot pick : restaurant.child("Top Picks").getChildren()) {
//                        String id = pick.getKey(); // Fetch UID (e.g., "Cheeseburger")
//                        String description = pick.child("description").getValue(String.class);
//                        String imageUrl = pick.child("imageURL").getValue(String.class);
//                        Double price = pick.child("price").getValue(Double.class);
//
//                        if (id != null && description != null && imageUrl != null && price != null) {
//                            topPicks.add(new FoodItem(id, description, imageUrl, price));
//                        }
//                    }
//                }
//
//                // Set adapter for RecyclerView
//                topPicksRecyclerView.setAdapter(new FoodAdapter(CustomerLandingActivity.this, topPicks, foodItem -> {
//                    // Navigate to FoodDetailActivity and pass relevant data
//                    Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
//                    intent.putExtra("foodId", foodItem.getId()); // e.g., Cheeseburger
//                    intent.putExtra("foodDescription", foodItem.getDescription()); // Description
//                    intent.putExtra("foodImage", foodItem.getImageURL()); // Image URL
//                    intent.putExtra("foodPrice", foodItem.getPrice()); // Price
//                    startActivity(intent);
//                }));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(CustomerLandingActivity.this, "Failed to load top picks.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void fetchSpecialOffers() {
//        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<FoodItem> specialOffers = new ArrayList<>();
//
//                for (DataSnapshot restaurant : snapshot.getChildren()) {
//                    for (DataSnapshot offer : restaurant.child("Special Offers").getChildren()) {
//                        // Fetch Data Correctly
//                        String id = offer.getKey(); // Fetch UID (e.g., "Apple Pie")
//                        String description = offer.child("description").getValue(String.class);
//                        String imageUrl = offer.child("imageURL").getValue(String.class);
//                        Double price = offer.child("price").getValue(Double.class);
//
//                        if (id != null && description != null && imageUrl != null && price != null) {
//                            specialOffers.add(new FoodItem(id, description, imageUrl, price));
//                        }
//                    }
//                }
//
//                // Set adapter for RecyclerView
//                specialOffersRecyclerView.setAdapter(new FoodAdapter(CustomerLandingActivity.this, specialOffers, foodItem -> {
//                    // Navigate to FoodDetailActivity and pass relevant data
//                    Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
//                    intent.putExtra("foodId", foodItem.getId());
//                    intent.putExtra("foodDescription", foodItem.getDescription());
//                    intent.putExtra("foodImage", foodItem.getImageURL());
//                    intent.putExtra("foodPrice", foodItem.getPrice());
//                    startActivity(intent);
//                }));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(CustomerLandingActivity.this, "Failed to load special offers.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//// 🔹 Handle Bottom Navigation Clicks (Java 11 Fix)
//
//    private boolean onNavigationItemSelected(@NonNull int item) {
//        int id = item;
//
//        if (id == R.id.navigation_home) {
//            return true;
//        } else if (id == R.id.navigation_restaurant) {
//            startActivity(new Intent(this, RestaurantActivity.class));
//            finish();
//            return true;
//        } else if (id == R.id.navigation_browse) {
//            startActivity(new Intent(this, BrowseActivity.class));
//            finish();
//            return true;
//        } else if (id == R.id.navigation_order) {
//            startActivity(new Intent(this, OrderActivity.class));
//            finish();
//            return true;
//        } else if (id == R.id.navigation_account) {
//            startActivity(new Intent(this, AccountActivity.class));
//            finish();
//            return true;
//        }
//        return false;
//    }
//}



package com.example.togoo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.FoodCategoryAdapter;
import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.models.FoodCategory;
import com.example.togoo.models.FoodItem;
import com.example.togoo.utils.RestaurantHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerLandingActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView locationText, viewResultsButton;
    private ImageButton cartButton;
    private EditText searchBar;
    private RecyclerView featuredCategoriesRecyclerView, specialOffersRecyclerView, topPicksRecyclerView, searchSuggestionsRecyclerView;
    private FusedLocationProviderClient fusedLocationClient;
    private BottomNavigationView bottomNavigation;
    private DatabaseReference dbReference;
    private FoodAdapter searchAdapter; // Adapter for suggestions

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

        // Initialize the new search suggestions UI elements
        searchSuggestionsRecyclerView = findViewById(R.id.searchSuggestionsRecyclerView);
        viewResultsButton = findViewById(R.id.viewResultsButton);

        searchAdapter = new FoodAdapter(this, new ArrayList<>(), RestaurantHelper.getCurrentRestaurant(), foodItem -> {
            Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
            intent.putExtra("foodId", foodItem.getId());
            intent.putExtra("foodDescription", foodItem.getDescription());
            intent.putExtra("foodImage", foodItem.getImageURL());
            intent.putExtra("foodPrice", foodItem.getPrice());
            startActivity(intent);
        });

        searchSuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionsRecyclerView.setAdapter(searchAdapter);
        searchSuggestionsRecyclerView.setVisibility(View.GONE);
        viewResultsButton.setVisibility(View.GONE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchCustomerLocation();

        cartButton.setOnClickListener(v -> startActivity(new Intent(CustomerLandingActivity.this, CartActivity.class)));

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchMenuItems(query);
                } else {
                    searchSuggestionsRecyclerView.setVisibility(View.GONE);
                    viewResultsButton.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        setupRecyclerViews();

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

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Forces GPS for accuracy
                .setInterval(5000) // Updates every 5 seconds
                .setFastestInterval(2000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) return;

                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    updateUserLocation(currentUser.getUid(), latitude, longitude);
                }

                // Convert latitude & longitude to a readable address
                String locationName = getAddressFromCoordinates(latitude, longitude);
                locationText.setText(locationName);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    private void updateUserLocation(String userId, double latitude, double longitude) {
        if (userId == null || userId.isEmpty()) {
            Log.e("FirebaseError", "Invalid user ID.");
            return;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        String[] userTypes = {"customer", "admin", "restaurant", "driver"};

        // Loop through each role to find where the user exists
        for (String userType : userTypes) {
            DatabaseReference userRef = rootRef.child(userType).child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Update location in the correct node
                        DatabaseReference locationRef = userRef.child("location");
                        locationRef.child("latitude").setValue(latitude);
                        locationRef.child("longitude").setValue(longitude)
                                .addOnSuccessListener(aVoid -> Log.d("FirebaseSuccess", "Location updated for " + userType))
                                .addOnFailureListener(e -> Log.e("FirebaseError", "Failed to update location: " + e.getMessage()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Failed to check user role: " + error.getMessage());
                }
            });
        }
    }


    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality(); // Returns city name (e.g., Montreal)
            } else {
                // Retry once after a small delay
                Thread.sleep(1000);
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    return addresses.get(0).getLocality();
                }
            }
        } catch (IOException | InterruptedException e) {
            Log.e("GeocoderError", "Error fetching address: " + e.getMessage());
        }
        return "Unknown Location";
    }


    // 🔹 Search Menu Items in Firebase
    private void searchMenuItems(String query) {
        // Use the "restaurant" node in your database
        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> prefixMatches = new ArrayList<>();
                List<FoodItem> substringMatches = new ArrayList<>();
                String queryLower = query.toLowerCase();

                // Iterate through each restaurant
                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    DataSnapshot menuNode = restaurant.child("menu");
                    if (menuNode.exists()) {
                        // Iterate through each category under "menu"
                        for (DataSnapshot categoryNode : menuNode.getChildren()) {
                            // For each food item under the category
                            for (DataSnapshot foodSnapshot : categoryNode.getChildren()) {
                                String foodId = foodSnapshot.getKey(); // UID used as food name
                                if (foodId != null) {
                                    // Check if the UID starts with the query (prefix match)
                                    if (foodId.toLowerCase().startsWith(queryLower)) {
                                        FoodItem item = foodSnapshot.getValue(FoodItem.class);
                                        if (item != null) {
                                            // Overwrite id with the UID
                                            item = new FoodItem(foodId, item.getDescription(), item.getImageURL(), item.getPrice());
                                            prefixMatches.add(item);
                                        }
                                    } else if (foodId.toLowerCase().contains(queryLower)) {
                                        // Substring match (UID contains the query elsewhere)
                                        FoodItem item = foodSnapshot.getValue(FoodItem.class);
                                        if (item != null) {
                                            item = new FoodItem(foodId, item.getDescription(), item.getImageURL(), item.getPrice());
                                            substringMatches.add(item);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Merge lists with prefixMatches prioritized
                List<FoodItem> mergedResults = new ArrayList<>();
                mergedResults.addAll(prefixMatches);
                mergedResults.addAll(substringMatches);

                if (!mergedResults.isEmpty()) {
                    // Limit suggestions to 5 items
                    List<FoodItem> suggestions = new ArrayList<>();
                    int suggestionCount = Math.min(mergedResults.size(), 5);
                    for (int i = 0; i < suggestionCount; i++) {
                        suggestions.add(mergedResults.get(i));
                    }
                    // Update suggestions adapter
                    searchAdapter.updateData(suggestions);
                    searchSuggestionsRecyclerView.setVisibility(View.VISIBLE);

                    // Update view results button text with total count
                    viewResultsButton.setText("View all " + mergedResults.size() + " results");
                    viewResultsButton.setVisibility(View.VISIBLE);
                    viewResultsButton.setOnClickListener(v -> {
                        Intent intent = new Intent(CustomerLandingActivity.this, ViewAllActivity.class);
                        intent.putParcelableArrayListExtra("searchResults", new ArrayList<>(mergedResults));
                        startActivity(intent);
                    });
                } else {
                    // No results found: clear suggestions and show appropriate message
                    searchAdapter.updateData(new ArrayList<>());
                    searchSuggestionsRecyclerView.setVisibility(View.GONE);
                    viewResultsButton.setText("No results found");
                    viewResultsButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optionally handle error here
            }
        });
    }




    // 🔹 Set up RecyclerViews
    private void setupRecyclerViews() {
        featuredCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredCategoriesRecyclerView.setAdapter(new FoodCategoryAdapter(this, getFeaturedCategories(), category -> {
            Intent intent = new Intent(CustomerLandingActivity.this, FeaturedCategoryActivity.class);
            intent.putExtra("selectedCategory", getSearchKeyword(category.getName()));
            startActivity(intent);
        }));

        specialOffersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchSpecialOffers();

        topPicksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchTopPicks();
    }

    // 🔹 Generate Featured Categories
    private List<FoodCategory> getFeaturedCategories() {
        List<FoodCategory> categories = new ArrayList<>();
        categories.add(new FoodCategory("Pizza", R.drawable.pizza));
        categories.add(new FoodCategory("Burgers", R.drawable.burger));
        categories.add(new FoodCategory("Sushi", R.drawable.sushi));
        categories.add(new FoodCategory("Pasta", R.drawable.spaghetti));
        categories.add(new FoodCategory("Seafood", R.drawable.shrimp));
        categories.add(new FoodCategory("Salads", R.drawable.salad));
        categories.add(new FoodCategory("Tacos", R.drawable.tacos));
        categories.add(new FoodCategory("Desserts", R.drawable.cupcake));
        return categories;
    }

    private String getSearchKeyword(String categoryName) {
        switch (categoryName) {
            case "Pizza": return "Pizza";
            case "Burgers": return "Burgers";
            case "Sushi": return "Sushi";
            case "Pasta": return "Pasta";
            case "Seafood": return "Seafood";
            case "Salads": return "Salads";
            case "Tacos": return "Tacos";
            case "Desserts": return "Desserts";
            default: return "";
        }
    }

    private void fetchTopPicks() {
        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> topPicks = new ArrayList<>();

                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    for (DataSnapshot pick : restaurant.child("Top Picks").getChildren()) {
                        String id = pick.getKey();
                        String description = pick.child("description").getValue(String.class);
                        String imageUrl = pick.child("imageURL").getValue(String.class);
                        Double price = pick.child("price").getValue(Double.class);

                        if (id != null && description != null && imageUrl != null && price != null) {
                            topPicks.add(new FoodItem(id, description, imageUrl, price));
                        }
                    }
                }

                topPicksRecyclerView.setAdapter(new FoodAdapter(
                        CustomerLandingActivity.this,
                        topPicks,
                        RestaurantHelper.getCurrentRestaurant(),
                        foodItem -> {
                            Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
                            intent.putExtra("foodId", foodItem.getId());
                            intent.putExtra("foodDescription", foodItem.getDescription());
                            intent.putExtra("foodImage", foodItem.getImageURL());
                            intent.putExtra("foodPrice", foodItem.getPrice());
                            startActivity(intent);
                        }
                ));
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
                        String id = offer.getKey();
                        String description = offer.child("description").getValue(String.class);
                        String imageUrl = offer.child("imageURL").getValue(String.class);
                        Double price = offer.child("price").getValue(Double.class);

                        if (id != null && description != null && imageUrl != null && price != null) {
                            specialOffers.add(new FoodItem(id, description, imageUrl, price));
                        }
                    }
                }

                specialOffersRecyclerView.setAdapter(new FoodAdapter(CustomerLandingActivity.this, specialOffers, RestaurantHelper.getCurrentRestaurant(), foodItem -> {
                    Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageURL());
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
//        } else if (id == R.id.navigation_browse) {
//            startActivity(new Intent(this, BrowseActivity.class));
//            finish();
//            return true;
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



