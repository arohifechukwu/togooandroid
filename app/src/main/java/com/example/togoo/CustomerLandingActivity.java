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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.view.Gravity;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.FoodCategoryAdapter;
import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.models.FoodCategory;
import com.example.togoo.models.FoodItem;
import com.example.togoo.models.Restaurant;
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
    private ImageButton notificationButton;
    private TextView notificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_landing);

        // Initialize notification UI
        FrameLayout topBarContainer = findViewById(R.id.topBarContainer);
        notificationButton = new ImageButton(this);
        notificationButton.setBackgroundColor(Color.TRANSPARENT);
        notificationButton.setImageResource(R.drawable.ic_notification); // 🔔 Your notification icon
        notificationButton.setContentDescription("Notifications");
        FrameLayout.LayoutParams notifParams = new FrameLayout.LayoutParams(80, 80);
        notifParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        notifParams.setMargins(0, 0, 140, 0); // Padding between cart and notification
        topBarContainer.addView(notificationButton, notifParams);

        notificationBadge = new TextView(this);
        notificationBadge.setBackgroundResource(R.drawable.badge_background); // 🔔 Create red circle drawable
        notificationBadge.setTextColor(Color.WHITE);
        notificationBadge.setTextSize(12);
        notificationBadge.setPadding(8, 2, 8, 2);
        notificationBadge.setVisibility(View.GONE);
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeParams.gravity = Gravity.END;
        badgeParams.setMargins(0, 0, 100, 40);
        topBarContainer.addView(notificationBadge, badgeParams);

        notificationButton.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        listenForNotificationUpdates();

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

//notification badge logic
        final int[] updateCount = {0}; // Wrapper array to allow mutation

        notificationBadge = findViewById(R.id.notificationBadge);
        notificationButton = findViewById(R.id.notificationButton);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference updatesRef = FirebaseDatabase.getInstance().getReference("orders");

        updatesRef.orderByChild("customer/id").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        updateCount[0] = 0;
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            if (orderSnap.hasChild("updateLogs")) {
                                long logCount = orderSnap.child("updateLogs").getChildrenCount();
                                updateCount[0] += (int) logCount;
                            }
                        }

                        if (updateCount[0] > 0) {
                            notificationBadge.setText(String.valueOf(updateCount[0]));
                            notificationBadge.setVisibility(View.VISIBLE);
                        } else {
                            notificationBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("NotificationBadge", "Failed to fetch notifications.");
                    }
                });

        notificationButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerLandingActivity.this, NotificationsActivity.class));
        });

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

    private void listenForNotificationUpdates() {
        if (notificationBadge == null || notificationButton == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference updatesRef = FirebaseDatabase.getInstance().getReference("orders");

        updatesRef.orderByChild("customer/id").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = 0;
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            if (orderSnap.hasChild("updateLogs")) {
                                count += (int) orderSnap.child("updateLogs").getChildrenCount();
                            }
                        }

                        if (count > 0) {
                            notificationBadge.setText(String.valueOf(count));
                            notificationBadge.setVisibility(View.VISIBLE);
                        } else {
                            notificationBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("NotificationBadge", "Failed to fetch notifications.");
                    }
                });
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
        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> prefixMatches = new ArrayList<>();
                List<FoodItem> substringMatches = new ArrayList<>();
                String queryLower = query.toLowerCase();

                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    String restaurantId = restaurant.getKey();
                    DataSnapshot menuNode = restaurant.child("menu");
                    if (menuNode.exists()) {
                        for (DataSnapshot categoryNode : menuNode.getChildren()) {
                            for (DataSnapshot foodSnapshot : categoryNode.getChildren()) {
                                String foodId = foodSnapshot.getKey();
                                if (foodId != null) {
                                    FoodItem item = foodSnapshot.getValue(FoodItem.class);
                                    if (item != null) {
                                        // Corrected constructor call
                                        item = new FoodItem(foodId, item.getDescription(), item.getImageURL(), restaurantId, item.getPrice());
                                        if (foodId.toLowerCase().startsWith(queryLower)) {
                                            prefixMatches.add(item);
                                        } else if (foodId.toLowerCase().contains(queryLower)) {
                                            substringMatches.add(item);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                List<FoodItem> mergedResults = new ArrayList<>();
                mergedResults.addAll(prefixMatches);
                mergedResults.addAll(substringMatches);

                if (!mergedResults.isEmpty()) {
                    List<FoodItem> suggestions = new ArrayList<>();
                    int suggestionCount = Math.min(mergedResults.size(), 5);
                    for (int i = 0; i < suggestionCount; i++) {
                        suggestions.add(mergedResults.get(i));
                    }
                    searchAdapter.updateData(suggestions);
                    searchSuggestionsRecyclerView.setVisibility(View.VISIBLE);

                    viewResultsButton.setText("View all " + mergedResults.size() + " results");
                    viewResultsButton.setVisibility(View.VISIBLE);
                    viewResultsButton.setOnClickListener(v -> {
                        Intent intent = new Intent(CustomerLandingActivity.this, ViewAllActivity.class);
                        intent.putParcelableArrayListExtra("searchResults", new ArrayList<>(mergedResults));
                        startActivity(intent);
                    });
                } else {
                    searchAdapter.updateData(new ArrayList<>());
                    searchSuggestionsRecyclerView.setVisibility(View.GONE);
                    viewResultsButton.setText("No results found");
                    viewResultsButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SearchError", "Failed to search menu items: " + error.getMessage());
                Toast.makeText(CustomerLandingActivity.this, "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
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



    private void setCurrentRestaurant(String restaurantId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);
                if (restaurant != null) {
                    if (restaurant.getId() == null || restaurant.getId().isEmpty()) {
                        restaurant.setId(snapshot.getKey()); // Ensure id is set
                    }
                    RestaurantHelper.setCurrentRestaurant(restaurant);
                } else {
                    Log.e("RestaurantError", "Restaurant data is null for ID: " + restaurantId);
                    Toast.makeText(CustomerLandingActivity.this, "Restaurant not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "Failed to load restaurant: " + error.getMessage());
                Toast.makeText(CustomerLandingActivity.this, "Failed to load restaurant data.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void fetchTopPicks() {
        dbReference.child("restaurant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> topPicks = new ArrayList<>();
                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    String restaurantId = restaurant.getKey();
                    for (DataSnapshot pick : restaurant.child("Top Picks").getChildren()) {
                        FoodItem item = FoodItem.fromSnapshot(pick, restaurantId);
                        if (item != null) {
                            Log.d("FetchTopPicks", "FoodItem: id=" + item.getId() +
                                    ", desc=" + item.getDescription() +
                                    ", image=" + item.getImageURL() +
                                    ", price=" + item.getPrice() +
                                    ", restaurantId=" + item.getRestaurantId());
                            topPicks.add(item);
                        }
                    }
                }
                topPicksRecyclerView.setAdapter(new FoodAdapter(
                        CustomerLandingActivity.this, topPicks, null, foodItem -> {
                    setCurrentRestaurant(foodItem.getRestaurantId());
                    Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageURL());
                    intent.putExtra("foodPrice", foodItem.getPrice());
                    intent.putExtra("restaurantId", foodItem.getRestaurantId());
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
                    String restaurantId = restaurant.getKey();
                    for (DataSnapshot pick : restaurant.child("Special Offers").getChildren()) {
                        FoodItem item = FoodItem.fromSnapshot(pick, restaurantId);
                        if (item != null) {
                            Log.d("FetchSpecialOffers", "FoodItem: id=" + item.getId() +
                                    ", desc=" + item.getDescription() +
                                    ", image=" + item.getImageURL() +
                                    ", price=" + item.getPrice() +
                                    ", restaurantId=" + item.getRestaurantId());
                            specialOffers.add(item);
                        }
                    }
                }
                specialOffersRecyclerView.setAdapter(new FoodAdapter(
                        CustomerLandingActivity.this, specialOffers, null, foodItem -> {
                    setCurrentRestaurant(foodItem.getRestaurantId());
                    Intent intent = new Intent(CustomerLandingActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageURL());
                    intent.putExtra("foodPrice", foodItem.getPrice());
                    intent.putExtra("restaurantId", foodItem.getRestaurantId());
                    startActivity(intent);
                }
                ));
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
        } else if (id == R.id.navigation_myorders) {
            startActivity(new Intent(this, MyOrderActivity.class));
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



