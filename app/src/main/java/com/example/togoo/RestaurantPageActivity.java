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
import com.example.togoo.utils.RestaurantHelper;
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
    private TextView restaurantName, restaurantRating, restaurantAddress, restaurantDistance, operatingHoursStatus;
    private RecyclerView featuredItemsRecyclerView, moreToExploreRecyclerView;
    private LinearLayout menuContainer;

    private String restaurantId;
    private Restaurant currentRestaurant;
    private DatabaseReference restaurantRef;
    private DatabaseReference usersRef;

    private double userLatitude, userLongitude;

    private FoodAdapter featuredItemsAdapter, moreToExploreAdapter;
    private int commentsShown = 0;
    private final int COMMENTS_BATCH = 5;

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

        restaurantImage = findViewById(R.id.restaurantImage);
        restaurantName = findViewById(R.id.restaurantName);
        restaurantRating = findViewById(R.id.restaurantRating);
        restaurantAddress = findViewById(R.id.restaurantAddress);
        restaurantDistance = findViewById(R.id.restaurantDistance);
        operatingHoursStatus = findViewById(R.id.operatingHoursStatus);
        featuredItemsRecyclerView = findViewById(R.id.featuredItemsRecyclerView);
        menuContainer = findViewById(R.id.menuContainer);
        moreToExploreRecyclerView = findViewById(R.id.moreToExploreRecyclerView);

        // Retrieve restaurantId from Intent
        restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId == null) {
            // Handle the case where restaurantId is not provided
            Log.e("RestaurantPageActivity", "restaurantId not provided in Intent");
            finish(); // Or set a default value
            return;
        }

        restaurantRef = FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantId);
        usersRef = FirebaseDatabase.getInstance().getReference();

        fetchRestaurantDetails();

        fetchUserLocation(new LocationCallback() {
            @Override
            public void onLocationFetched(double lat, double lon) {
                userLatitude = lat;
                userLongitude = lon;
                updateDistanceDisplay();
                fetchMoreToExplore();
            }
        });

        featuredItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        moreToExploreRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        fetchFeaturedItems();
        fetchMenuSections();
    }

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

                    Object latObj = snapshot.child("location").child("latitude").getValue();
                    Object lonObj = snapshot.child("location").child("longitude").getValue();

                    String latStr = (latObj instanceof Double) ? String.valueOf(latObj) : (String) latObj;
                    String lonStr = (lonObj instanceof Double) ? String.valueOf(lonObj) : (String) lonObj;
                    LocationCoordinates location = new LocationCoordinates();
                    location.setLatitude(Double.valueOf(latStr != null ? latStr : "0"));
                    location.setLongitude(Double.valueOf(lonStr != null ? lonStr : "0"));

                    Double ratingVal = snapshot.child("rating").getValue(Double.class);
                    double rating = ratingVal != null ? ratingVal : 4.5;

                    Map<String, OperatingHours> operatingHours = snapshot.child("operatingHours")
                            .getValue(new GenericTypeIndicator<Map<String, OperatingHours>>() {});

                    currentRestaurant = new Restaurant(
                            id != null ? id : "",
                            name != null ? name : "Unnamed",
                            address != null ? address : "Unknown location",
                            imageUrl != null ? imageUrl : "",
                            location,
                            operatingHours,
                            rating,
                            0,
                            0
                    );

                    RestaurantHelper.setCurrentRestaurant(currentRestaurant);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(RestaurantPageActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_restaurant_placeholder)
                                .into(restaurantImage);
                    } else {
                        restaurantImage.setImageResource(R.drawable.ic_restaurant_placeholder);
                    }

                    restaurantName.setText(name != null ? name : "Unnamed Restaurant");
                    restaurantRating.setText("⭐ " + String.format(Locale.getDefault(), "%.1f", currentRestaurant.getRating()));
                    restaurantAddress.setText(address != null ? address : "Address unavailable");
                    updateDistanceDisplay();
                    fetchAndValidateOperatingHours();

                    LinearLayout commentsContainer = findViewById(R.id.commentsContainer);
                    commentsContainer.removeAllViews(); // Clear old comments if any

                    DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                            .getReference("restaurant")
                            .child(currentRestaurant.getId())
                            .child("ratings");

                    commentsRef.orderByChild("timestamp").limitToLast(3)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<DataSnapshot> commentList = new ArrayList<>();
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        commentList.add(child);
                                    }

                                    // Sort newest first
                                    commentList.sort((a, b) -> {
                                        Long t1 = a.child("timestamp").getValue(Long.class);
                                        Long t2 = b.child("timestamp").getValue(Long.class);
                                        return Long.compare(t2 != null ? t2 : 0, t1 != null ? t1 : 0);
                                    });

                                    for (DataSnapshot commentSnap : commentList) {
                                        String comment = commentSnap.child("comment").getValue(String.class);
                                        Float rating = commentSnap.child("value").getValue(Float.class);

                                        if (comment != null && !comment.isEmpty()) {
                                            TextView commentView = new TextView(RestaurantPageActivity.this);
                                            commentView.setText("⭐ " + rating + " — " + comment);
                                            commentView.setTextSize(14);
                                            commentView.setPadding(0, 8, 0, 8);
                                            commentsContainer.addView(commentView);
                                            fetchReviewComments(0);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Comments", "Failed to load comments", error.toException());
                                }
                            });

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

    private void fetchUserLocation(final LocationCallback callback) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
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

    private String getOperatingHoursStatus(String open, String close) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
        try {
            Date now = new Date();
            String currentTimeString = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now);

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



    private void fetchFeaturedItems() {
        restaurantRef.child("Special Offers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<FoodItem> featuredItems = new ArrayList<>();
                String restaurantId = RestaurantHelper.getCurrentRestaurant().getId(); // Get restaurantId from RestaurantHelper

                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String foodId = itemSnap.getKey();
                    FoodItem rawItem = itemSnap.getValue(FoodItem.class);

                    if (foodId != null && rawItem != null) {
                        // Use updated FoodItem constructor with restaurantId
                        FoodItem item = new FoodItem(
                                foodId,
                                rawItem.getDescription(),
                                rawItem.getImageURL(),
                                restaurantId,
                                rawItem.getPrice()
                        );
                        featuredItems.add(item);
                    }
                }

                featuredItemsAdapter = new FoodAdapter(
                        RestaurantPageActivity.this,
                        featuredItems,
                        RestaurantHelper.getCurrentRestaurant(),
                        foodItem -> {
                            Intent intent = new Intent(RestaurantPageActivity.this, FoodDetailActivity.class);
                            intent.putExtra("foodId", foodItem.getId());
                            intent.putExtra("foodDescription", foodItem.getDescription());
                            intent.putExtra("foodImage", foodItem.getImageURL());
                            intent.putExtra("foodPrice", foodItem.getPrice());
                            intent.putExtra("selectedRestaurant", RestaurantHelper.getCurrentRestaurant());
                            startActivity(intent);
                        }
                );

                featuredItemsRecyclerView.setAdapter(featuredItemsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RestaurantPageActivity", "Failed to load featured items: " + error.getMessage());
            }
        });
    }



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
                            // Use the five-parameter constructor with restaurantId
                            FoodItem item = new FoodItem(
                                    foodId,
                                    rawItem.getDescription(),
                                    rawItem.getImageURL(),
                                    restaurantId,
                                    rawItem.getPrice()
                            );
                            sectionItems.add(item);
                        }
                    }

                    View sectionView = getLayoutInflater().inflate(R.layout.section_menu, menuContainer, false);
                    TextView sectionTitleText = sectionView.findViewById(R.id.sectionTitle);
                    RecyclerView sectionRecyclerView = sectionView.findViewById(R.id.sectionRecyclerView);

                    sectionTitleText.setText(sectionTitle);
                    sectionRecyclerView.setLayoutManager(new LinearLayoutManager(
                            RestaurantPageActivity.this,
                            LinearLayoutManager.HORIZONTAL,
                            false
                    ));

                    FoodAdapter sectionAdapter = new FoodAdapter(
                            RestaurantPageActivity.this,
                            sectionItems,
                            RestaurantHelper.getCurrentRestaurant(),
                            foodItem -> {
                                Intent intent = new Intent(RestaurantPageActivity.this, FoodDetailActivity.class);
                                intent.putExtra("foodId", foodItem.getId());
                                intent.putExtra("foodDescription", foodItem.getDescription());
                                intent.putExtra("foodImage", foodItem.getImageURL());
                                intent.putExtra("foodPrice", foodItem.getPrice());
                                intent.putExtra("selectedRestaurant", RestaurantHelper.getCurrentRestaurant());
                                startActivity(intent);
                            }
                    );

                    sectionRecyclerView.setAdapter(sectionAdapter);
                    menuContainer.addView(sectionView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RestaurantPageActivity", "Database error: " + error.getMessage());
                // Optionally, inform the user about the error
            }
        });
    }


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
                    location.setLatitude(Double.valueOf(latObj.toString()));
                    location.setLongitude(Double.valueOf(lonObj.toString()));

                    Double ratingVal = snap.child("rating").getValue(Double.class);
                    double rating = ratingVal != null ? ratingVal : 4.5;

                    Map<String, OperatingHours> operatingHours = snap.child("operatingHours")
                            .getValue(new GenericTypeIndicator<Map<String, OperatingHours>>() {});

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

                    float[] results = new float[1];
                    Location.distanceBetween(userLatitude, userLongitude, restLat, restLon, results);
                    double distanceKm = results[0] / 1000.0;
                    int etaMinutes = (int) ((distanceKm / 40.0) * 60);

                    r.setDistanceKm(distanceKm);
                    r.setEtaMinutes(etaMinutes);

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


    private void fetchReviewComments(int startIndex) {
        LinearLayout commentsContainer = findViewById(R.id.commentsContainer);
        TextView summaryText = findViewById(R.id.reviewSummary);
        TextView viewMore = findViewById(R.id.viewMoreComments);

        DatabaseReference ratingsRef = FirebaseDatabase.getInstance()
                .getReference("restaurant")
                .child(currentRestaurant.getId())
                .child("ratings");

        ratingsRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DataSnapshot> allComments = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String comment = child.child("comment").getValue(String.class);
                    if (comment != null && !comment.isEmpty()) {
                        allComments.add(child);
                    }
                }

                int totalComments = allComments.size();
                summaryText.setText(totalComments + " review" + (totalComments != 1 ? "s" : ""));

                // Sort newest first
                allComments.sort((a, b) -> {
                    Long t1 = a.child("timestamp").getValue(Long.class);
                    Long t2 = b.child("timestamp").getValue(Long.class);
                    return Long.compare(t2 != null ? t2 : 0, t1 != null ? t1 : 0);
                });

                int endIndex = Math.min(startIndex + COMMENTS_BATCH, totalComments);

                for (int i = startIndex; i < endIndex; i++) {
                    DataSnapshot commentSnap = allComments.get(i);
                    String comment = commentSnap.child("comment").getValue(String.class);
                    Float rating = commentSnap.child("value").getValue(Float.class);
                    String userId = commentSnap.getKey(); // customerId

                    TextView commentView = new TextView(RestaurantPageActivity.this);
                    commentView.setText("⭐ " + rating + " — " + comment);
                    commentView.setTextSize(14);
                    commentView.setPadding(0, 8, 0, 8);
                    commentsContainer.addView(commentView);

                    DatabaseReference customerRef = FirebaseDatabase.getInstance()
                            .getReference("customer")
                            .child(userId);

                    customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("name").getValue(String.class);
                            commentView.setText("⭐ " + rating + " — " + (name != null ? name : "User") + ": " + comment);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            commentView.setText("⭐ " + rating + " — User: " + comment);
                        }
                    });
                }

                commentsShown += COMMENTS_BATCH;

                if (commentsShown < totalComments) {
                    viewMore.setVisibility(View.VISIBLE);
                    viewMore.setOnClickListener(v -> fetchReviewComments(commentsShown));
                } else {
                    viewMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ReviewComments", "Failed to fetch comments", error.toException());
            }
        });
    }

}