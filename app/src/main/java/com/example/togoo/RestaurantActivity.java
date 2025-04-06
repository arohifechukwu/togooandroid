package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.location.Location;
import android.widget.Toast;

import com.google.firebase.database.GenericTypeIndicator;
import com.example.togoo.models.OperatingHours;
import com.example.togoo.models.LocationCoordinates;

import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.adapters.FoodCategoryAdapter;
import com.example.togoo.adapters.RestaurantAdapter;
import com.example.togoo.models.FoodCategory;
import com.example.togoo.models.FoodItem;
import com.example.togoo.models.Restaurant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends AppCompatActivity {

    private ImageButton cartButton;
    private EditText searchBar;
    private TextView viewResultsButton;
    private RecyclerView featuredCategoriesRecyclerView, searchSuggestionsRecyclerView, restaurantGridRecyclerView;
    private FoodAdapter searchAdapter;
    private List<Restaurant> restaurantList = new ArrayList<>();
    private DatabaseReference dbRef;
    private BottomNavigationView bottomNavigation;
    private Restaurant selectedRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        cartButton = findViewById(R.id.cartButton);
        searchBar = findViewById(R.id.searchBar);
        viewResultsButton = findViewById(R.id.viewResultsButton);
        searchSuggestionsRecyclerView = findViewById(R.id.searchSuggestionsRecyclerView);
        featuredCategoriesRecyclerView = findViewById(R.id.featuredCategoriesRecyclerView);
        restaurantGridRecyclerView = findViewById(R.id.restaurantGridRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        dbRef = FirebaseDatabase.getInstance().getReference("restaurant");

        cartButton.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        selectedRestaurant = getIntent().getParcelableExtra("selectedRestaurant");

        featuredCategoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        featuredCategoriesRecyclerView.setAdapter(new FoodCategoryAdapter(this, getFeaturedCategories(), category -> {
            Intent intent = new Intent(RestaurantActivity.this, FeaturedCategoryActivity.class);
            intent.putExtra("selectedCategory", category.getName());
            startActivity(intent);
        }));


        searchAdapter = new FoodAdapter(this, new ArrayList<>(), selectedRestaurant, foodItem -> {
            Intent intent = new Intent(RestaurantActivity.this, FoodDetailActivity.class);
            intent.putExtra("foodId", foodItem.getId());
            intent.putExtra("foodDescription", foodItem.getDescription());
            intent.putExtra("foodImage", foodItem.getImageURL());
            intent.putExtra("foodPrice", foodItem.getPrice());
            startActivity(intent);
        });

        searchSuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionsRecyclerView.setAdapter(searchAdapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchMenuItems(query);
                } else {
                    searchSuggestionsRecyclerView.setVisibility(View.GONE);
                    viewResultsButton.setVisibility(View.GONE);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        bottomNavigation.setSelectedItemId(R.id.navigation_restaurant);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> onNavigationItemSelected(item.getItemId()));

        fetchRestaurants();
    }


    private void searchMenuItems(String query) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> prefixMatches = new ArrayList<>();
                List<FoodItem> substringMatches = new ArrayList<>();
                String queryLower = query.toLowerCase();

                for (DataSnapshot restaurant : snapshot.getChildren()) {
                    String restaurantId = restaurant.getKey();
                    DataSnapshot menuNode = restaurant.child("menu");
                    if (menuNode.exists()) {
                        for (DataSnapshot category : menuNode.getChildren()) {
                            for (DataSnapshot foodSnap : category.getChildren()) {
                                String foodId = foodSnap.getKey();
                                if (foodId != null) {
                                    FoodItem item = foodSnap.getValue(FoodItem.class);
                                    if (item != null) {
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
                    searchAdapter.updateData(mergedResults.subList(0, Math.min(5, mergedResults.size())));
                    searchSuggestionsRecyclerView.setVisibility(View.VISIBLE);
                    viewResultsButton.setText("View all " + mergedResults.size() + " results");
                    viewResultsButton.setVisibility(View.VISIBLE);
                    viewResultsButton.setOnClickListener(v -> {
                        Intent intent = new Intent(RestaurantActivity.this, ViewAllActivity.class);
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
                Toast.makeText(RestaurantActivity.this, "Failed to search items", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchRestaurants() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) return;

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String[] roles = {"customer", "driver", "restaurant"};
                for (String role : roles) {
                    if (snapshot.child(role).hasChild(currentUserId)) {
                        Double userLat = snapshot.child(role).child(currentUserId).child("location").child("latitude").getValue(Double.class);
                        Double userLon = snapshot.child(role).child(currentUserId).child("location").child("longitude").getValue(Double.class);

                        if (userLat != null && userLon != null) {
                            loadRestaurants(userLat, userLon);
                        } else {
                            // Optional: fallback to default location or show error
                            loadRestaurants(0.0, 0.0);
                        }
                        break; // Exit the loop once role is found
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle if needed
            }
        });
    }

    private void loadRestaurants(double userLat, double userLon) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                new Thread(() -> {
                    List<Restaurant> tempList = new ArrayList<>();

                    for (DataSnapshot restaurantSnap : snapshot.getChildren()) {
                        String id = restaurantSnap.getKey();
                        String name = restaurantSnap.child("name").getValue(String.class);
                        String imageUrl = restaurantSnap.child("imageURL").getValue(String.class);

                        // 🔧 Get lat/lon as Strings and parse manually
                        Object latObj = restaurantSnap.child("location").child("latitude").getValue();
                        Object lonObj = restaurantSnap.child("location").child("longitude").getValue();

                        String latStr = (latObj instanceof Double) ? String.valueOf(latObj) : (String) latObj;
                        String lonStr = (lonObj instanceof Double) ? String.valueOf(lonObj) : (String) lonObj;

                        double latitude = 0.0;
                        double longitude = 0.0;
                        try {
                            if (latStr != null) latitude = Double.parseDouble(latStr);
                            if (lonStr != null) longitude = Double.parseDouble(lonStr);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        LocationCoordinates location = new LocationCoordinates();
                        location.setLatitude(Double.valueOf(latStr != null ? latStr : "0"));
                        location.setLongitude(Double.valueOf(lonStr != null ? lonStr : "0"));

                        String addressString = getAddressFromCoordinates(latitude, longitude);

                        Map<String, OperatingHours> operatingHours = restaurantSnap.child("operatingHours")
                                .getValue(new GenericTypeIndicator<Map<String, OperatingHours>>() {});

                        Double ratingValue = restaurantSnap.child("rating").getValue(Double.class);
                        double rating = ratingValue != null ? ratingValue : 4.5;

                        // Distance + ETA
                        float[] results = new float[1];
                        Location.distanceBetween(userLat, userLon, latitude, longitude, results);
                        double distanceKm = results[0] / 1000.0;
                        int etaMinutes = (int) ((distanceKm / 40.0) * 60); // assuming 40km/h driving

                        if (name != null) {
                            Restaurant restaurant = new Restaurant(
                                    id,
                                    name,
                                    addressString,
                                    imageUrl != null ? imageUrl : "",
                                    location,
                                    operatingHours,
                                    rating,
                                    distanceKm,
                                    etaMinutes
                            );
                            tempList.add(restaurant);
                        }
                    }

                    runOnUiThread(() -> {
                        restaurantList.clear();
                        restaurantList.addAll(tempList);
                        restaurantGridRecyclerView.setLayoutManager(new GridLayoutManager(RestaurantActivity.this, 2));
                        restaurantGridRecyclerView.setAdapter(new RestaurantAdapter(RestaurantActivity.this, restaurantList, restaurant -> {
                            Intent intent = new Intent(RestaurantActivity.this, RestaurantPageActivity.class);
                            intent.putExtra("restaurantId", restaurant.getId());
                            startActivity(intent);
                        }));
                    });
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });
    }

    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(RestaurantActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder builder = new StringBuilder();
                if (address.getThoroughfare() != null) builder.append(address.getThoroughfare());
                if (address.getLocality() != null) builder.append(", ").append(address.getLocality());
                if (address.getAdminArea() != null) builder.append(", ").append(address.getAdminArea());
                return builder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }

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

    private boolean onNavigationItemSelected(@NonNull int item) {
        if (item == R.id.navigation_restaurant) return true;
        if (item == R.id.navigation_home) startActivity(new Intent(this, CustomerLandingActivity.class));
        else if (item == R.id.navigation_myorders) startActivity(new Intent(this, MyOrderActivity.class));
        else if (item == R.id.navigation_order) startActivity(new Intent(this, OrderActivity.class));
        else if (item == R.id.navigation_account) startActivity(new Intent(this, AccountActivity.class));
        finish();
        return true;
    }
}