package com.example.togoo;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import java.util.Map;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.togoo.models.LocationCoordinates;
import com.example.togoo.models.OperatingHours;
import com.example.togoo.models.Restaurant;
import com.google.firebase.database.GenericTypeIndicator;

import com.bumptech.glide.Glide;
import com.example.togoo.adapters.FoodAdapter;
import com.example.togoo.adapters.RestaurantAdapter;
import com.example.togoo.models.FoodItem;
import com.example.togoo.models.Restaurant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class RestaurantPageActivity extends AppCompatActivity {

    private ImageView restaurantImage;
    private TextView restaurantName, restaurantDistance, operatingHoursStatus;
    private RecyclerView featuredItemsRecyclerView, moreToExploreRecyclerView;
    private LinearLayout menuContainer;

    private String restaurantId;
    private Restaurant currentRestaurant;
    private DatabaseReference restaurantRef;
    private DatabaseReference usersRef;

    // Assume current user's coordinates (fetched elsewhere or via helper method)
    private double userLatitude, userLongitude;

    // Adapters for horizontal RecyclerViews
    private FoodAdapter featuredItemsAdapter, moreToExploreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(RestaurantPageActivity.this, RestaurantActivity.class));
            finish();
        });

        // Initialize views
        restaurantImage = findViewById(R.id.restaurantImage);
        restaurantName = findViewById(R.id.restaurantName);
        restaurantDistance = findViewById(R.id.restaurantDistance);
        operatingHoursStatus = findViewById(R.id.operatingHoursStatus);
        featuredItemsRecyclerView = findViewById(R.id.featuredItemsRecyclerView);
        menuContainer = findViewById(R.id.menuContainer);
        moreToExploreRecyclerView = findViewById(R.id.moreToExploreRecyclerView);

        // Get restaurantId (passed via Intent)
        restaurantId = getIntent().getStringExtra("restaurantId");
        if (TextUtils.isEmpty(restaurantId)) {
            finish();
            return;
        }

        restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantId);
        usersRef = FirebaseDatabase.getInstance().getReference();

        // Fetch restaurant details
        fetchRestaurantDetails();

        // Fetch the logged-on user's location and update the distance view
        fetchUserLocation(new LocationCallback() {
            @Override
            public void onLocationFetched(double lat, double lon) {
                userLatitude = lat;
                userLongitude = lon;
                updateDistanceDisplay();
                fetchMoreToExplore(); //accurate location update
            }
        });

        // Setup horizontal RecyclerViews
        featuredItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        moreToExploreRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Load dynamic sections
        fetchFeaturedItems();
        fetchMenuSections();
    }

    // Callback interface for fetching user location
    private interface LocationCallback {
        void onLocationFetched(double lat, double lon);
    }



    private void fetchRestaurantDetails() {
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String id = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String imageUrl = snapshot.child("imageURL").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);

                    // Location
                    Object latObj = snapshot.child("location").child("latitude").getValue();
                    Object lonObj = snapshot.child("location").child("longitude").getValue();

                    String latStr = (latObj instanceof Double) ? String.valueOf(latObj) : (String) latObj;
                    String lonStr = (lonObj instanceof Double) ? String.valueOf(lonObj) : (String) lonObj;
                    LocationCoordinates location = new LocationCoordinates();
                    location.setLatitude(latStr != null ? latStr : "0");
                    location.setLongitude(lonStr != null ? lonStr : "0");

                    // Rating
                    Double ratingVal = snapshot.child("rating").getValue(Double.class);
                    double rating = ratingVal != null ? ratingVal : 4.5;

                    // Operating hours
                    Map<String, OperatingHours> operatingHours = snapshot.child("operatingHours")
                            .getValue(new GenericTypeIndicator<Map<String, OperatingHours>>() {});

                    // Construct Restaurant object
                    currentRestaurant = new Restaurant(
                            id != null ? id : "",
                            name != null ? name : "Unnamed",
                            address != null ? address : "Unknown location",
                            imageUrl != null ? imageUrl : "",
                            location,
                            operatingHours,
                            rating,
                            0, // distanceKm placeholder
                            0  // etaMinutes placeholder
                    );

                    // Set UI
                    if (!imageUrl.isEmpty()) {
                        Glide.with(RestaurantPageActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_restaurant_placeholder)
                                .into(restaurantImage);
                    } else {
                        restaurantImage.setImageResource(R.drawable.ic_restaurant_placeholder);
                    }

                    restaurantName.setText(name != null ? name : "Unnamed Restaurant");
                    updateDistanceDisplay();
                    fetchAndValidateOperatingHours();

                } else {
                    restaurantName.setText("Restaurant not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                restaurantName.setText("Error loading restaurant.");
            }
        });
    }

    // Fetch the logged-on user's coordinates; added null-check for current user.
    private void fetchUserLocation(final LocationCallback callback) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User not logged in; use fallback coordinates (or handle appropriately)
            callback.onLocationFetched(0, 0);
            return;
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer").child(currentUserId).child("location");
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double lat = snapshot.child("latitude").getValue(Double.class);
                Double lon = snapshot.child("longitude").getValue(Double.class);
                if (lat != null && lon != null) {
                    callback.onLocationFetched(lat, lon);
                } else {
                    callback.onLocationFetched(0, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Update distance display using restaurant and user coordinates.
    private void updateDistanceDisplay() {
        if (currentRestaurant == null || (userLatitude == 0 && userLongitude == 0)) {
            restaurantDistance.setText("Distance: N/A");
            return;
        }
        double restLat = currentRestaurant.getLatitudeAsDouble();
        double restLon = currentRestaurant.getLongitudeAsDouble();
        float[] results = new float[1];
        Location.distanceBetween(userLatitude, userLongitude, restLat, restLon, results);
        float distanceInMeters = results[0];
        String distanceText = String.format(Locale.getDefault(), "%.1f km", distanceInMeters / 1000);
        restaurantDistance.setText(distanceText);
    }

    // Fetch and validate operating hours against Montreal time.
    private void fetchAndValidateOperatingHours() {
        restaurantRef.child("operatingHours").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                dayFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                String currentDay = dayFormat.format(new Date());
                DataSnapshot daySnapshot = snapshot.child(currentDay);
                if (daySnapshot.exists()) {
                    String openTime = daySnapshot.child("open").getValue(String.class);
                    String closeTime = daySnapshot.child("close").getValue(String.class);
                    if (openTime != null && closeTime != null) {
                        String status = getOperatingHoursStatus(openTime, closeTime);
                        operatingHoursStatus.setText(status);
                    } else {
                        operatingHoursStatus.setText("Hours not available");
                    }
                } else {
                    operatingHoursStatus.setText("Hours not available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Compare current Montreal time with open and close times.
    private String getOperatingHoursStatus(String open, String close) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault()); // 24-hour format
        sdf.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
        try {
            // Get current time in Montreal
            Date now = new Date();
            String currentTimeString = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(now);

            Date nowTime = sdf.parse(currentTimeString);
            Date openTime = sdf.parse(open);
            Date closeTime = sdf.parse(close);

            if (nowTime == null || openTime == null || closeTime == null) return "Hours unavailable";

            long nowMillis = nowTime.getTime();
            long openMillis = openTime.getTime();
            long closeMillis = closeTime.getTime();

            if (nowMillis < openMillis) {
                return "Available at " + open;
            } else if (nowMillis > closeMillis) {
                return "Closed";
            } else {
                return "Closes at " + close;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "Hours unavailable";
        }
    }

    // Fetch Featured Items from "Special Offers" node.

    private void fetchFeaturedItems() {
        restaurantRef.child("Special Offers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> featuredItems = new ArrayList<>();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String foodId = itemSnap.getKey();
                    FoodItem rawItem = itemSnap.getValue(FoodItem.class);

                    if (foodId != null && rawItem != null) {
                        // Set the UID (name) manually so it's not null in FoodDetailActivity
                        FoodItem item = new FoodItem(foodId, rawItem.getDescription(), rawItem.getImageURL(), rawItem.getPrice());
                        featuredItems.add(item);
                    }
                }

                featuredItemsAdapter = new FoodAdapter(RestaurantPageActivity.this, featuredItems, foodItem -> {
                    Intent intent = new Intent(RestaurantPageActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageURL());
                    intent.putExtra("foodPrice", foodItem.getPrice());
                    startActivity(intent);
                });

                featuredItemsRecyclerView.setAdapter(featuredItemsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }


    // Fetch Menu Sections
private void fetchMenuSections() {
    restaurantRef.child("menu").addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            menuContainer.removeAllViews();

            for (DataSnapshot sectionSnap : snapshot.getChildren()) {
                String sectionTitle = sectionSnap.getKey();
                List<FoodItem> sectionItems = new ArrayList<>();

                for (DataSnapshot itemSnap : sectionSnap.getChildren()) {
                    String foodId = itemSnap.getKey();
                    FoodItem rawItem = itemSnap.getValue(FoodItem.class);

                    if (foodId != null && rawItem != null) {
                        // Explicitly set the ID (name/UID) from Firebase key
                        FoodItem item = new FoodItem(foodId, rawItem.getDescription(), rawItem.getImageURL(), rawItem.getPrice());
                        sectionItems.add(item);
                    }
                }

                // Inflate section view
                View sectionView = getLayoutInflater().inflate(R.layout.section_menu, menuContainer, false);
                TextView sectionTitleText = sectionView.findViewById(R.id.sectionTitle);
                RecyclerView sectionRecyclerView = sectionView.findViewById(R.id.sectionRecyclerView);

                sectionTitleText.setText(sectionTitle);
                sectionRecyclerView.setLayoutManager(new LinearLayoutManager(RestaurantPageActivity.this, LinearLayoutManager.HORIZONTAL, false));

                // Setup adapter with click listener
                FoodAdapter sectionAdapter = new FoodAdapter(RestaurantPageActivity.this, sectionItems, foodItem -> {
                    Intent intent = new Intent(RestaurantPageActivity.this, FoodDetailActivity.class);
                    intent.putExtra("foodId", foodItem.getId());
                    intent.putExtra("foodDescription", foodItem.getDescription());
                    intent.putExtra("foodImage", foodItem.getImageURL());
                    intent.putExtra("foodPrice", foodItem.getPrice());
                    startActivity(intent);
                });

                sectionRecyclerView.setAdapter(sectionAdapter);
                menuContainer.addView(sectionView);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            // Optionally log or show error
        }
    });
}

    // Fetch 7 random restaurants for "More to explore".
    // This is done to avoid the user from having to search for restaurants.
    private void fetchMoreToExplore() {
        DatabaseReference restaurantsRef = FirebaseDatabase.getInstance().getReference("restaurant");
        restaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Restaurant> allRestaurants = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String id = snap.getKey();
                    String name = snap.child("name").getValue(String.class);
                    String address = snap.child("address").getValue(String.class);
                    String imageUrl = snap.child("imageURL").getValue(String.class);

                    Object latObj = snap.child("location").child("latitude").getValue();
                    Object lonObj = snap.child("location").child("longitude").getValue();

                    if (latObj == null || lonObj == null) continue;

                    double restLat, restLon;
                    try {
                        restLat = Double.parseDouble(latObj.toString());
                        restLon = Double.parseDouble(lonObj.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        continue;
                    }

                    LocationCoordinates location = new LocationCoordinates();
                    location.setLatitude(latObj.toString());
                    location.setLongitude(lonObj.toString());

                    // Optional: rating
                    Double ratingVal = snap.child("rating").getValue(Double.class);
                    double rating = ratingVal != null ? ratingVal : 4.5;

                    // Optional: operatingHours
                    Map<String, OperatingHours> operatingHours = snap.child("operatingHours")
                            .getValue(new GenericTypeIndicator<Map<String, OperatingHours>>() {});

                    // Build the restaurant object
                    Restaurant r = new Restaurant(
                            id != null ? id : "",
                            name != null ? name : "Unnamed",
                            address != null ? address : "Unknown",
                            imageUrl != null ? imageUrl : "",
                            location,
                            operatingHours,
                            rating,
                            0,
                            0
                    );

                    // Compute distance and ETA
                    float[] results = new float[1];
                    Location.distanceBetween(userLatitude, userLongitude, restLat, restLon, results);
                    double distanceKm = results[0] / 1000.0;
                    int etaMinutes = (int) ((distanceKm / 40.0) * 60);

                    r.setDistanceKm(distanceKm);
                    r.setEtaMinutes(etaMinutes);

                    // Exclude the current restaurant
                    if (restaurantId != null && !r.getId().equals(restaurantId)) {
                        allRestaurants.add(r);
                    }
                }

                allRestaurants.sort((r1, r2) -> Double.compare(r1.getDistanceKm(), r2.getDistanceKm()));
                List<Restaurant> top7Nearby = allRestaurants.subList(0, Math.min(7, allRestaurants.size()));
                Log.d("MoreToExplore", "Loaded " + top7Nearby.size() + " restaurants");

                moreToExploreRecyclerView.setLayoutManager(new LinearLayoutManager(
                        RestaurantPageActivity.this, LinearLayoutManager.HORIZONTAL, false
                ));

                RestaurantAdapter moreAdapter = new RestaurantAdapter(RestaurantPageActivity.this, top7Nearby, restaurant -> {
                    Intent intent = new Intent(RestaurantPageActivity.this, RestaurantPageActivity.class);
                    intent.putExtra("restaurantId", restaurant.getId());
                    startActivity(intent);
                });

                moreToExploreRecyclerView.setAdapter(moreAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MoreToExplore", "Failed to fetch restaurants: " + error.getMessage());
            }
        });
    }

}