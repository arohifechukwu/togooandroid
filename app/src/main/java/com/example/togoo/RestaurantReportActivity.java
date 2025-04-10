package com.example.togoo;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RestaurantReportActivity extends AppCompatActivity {

    private LinearLayout reportContainer;
    private String restaurantId;  // Assumes the logged-in restaurant's UID
    private static final String TAG = "RestaurantReportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_report);

        // Set up the Toolbar with Back button.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up Bottom Navigation.
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_reports); // set the report item selected
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_orders) {
                startActivity(new Intent(this, RestaurantLandingActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_new) {
                startActivity(new Intent(this, RestaurantNewActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_reports) {
                return true;
            } else if (id == R.id.navigation_manage) {
                startActivity(new Intent(this, RestaurantManageActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_account) {
                startActivity(new Intent(this, RestaurantAccountActivity.class));
                finish();
                return true;
            }
            return false;
        });

        reportContainer = findViewById(R.id.restaurantReportsContainer);
        restaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchRestaurantOrders();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle Toolbar back button press (Up navigation)
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetch orders for this restaurant from the Firebase node "ordersByRestaurant/<restaurantId>".
     */
    private void fetchRestaurantOrders() {
        DatabaseReference restaurantOrdersRef = FirebaseDatabase.getInstance()
                .getReference("ordersByRestaurant")
                .child(restaurantId);

        restaurantOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pair<String, Long>> orderList = new ArrayList<>();

                // Iterate over each order for this restaurant.
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    String orderId = orderSnap.getKey();
                    if (orderId != null) {
                        Object timestampObj = orderSnap.child("timestamps").child("placed").getValue();
                        long timestamp = 0;
                        try {
                            timestamp = Long.parseLong(String.valueOf(timestampObj));
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing timestamp for order " + orderId, e);
                        }
                        orderList.add(new Pair<>(orderId, timestamp));
                    }
                }

                // Sort orders in descending order (most recent first)
                orderList.sort((a, b) -> Long.compare(b.second, a.second));

                for (Pair<String, Long> entry : orderList) {
                    addRestaurantOrderToView(entry.first);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantReportActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Retrieve details for a specific order and create a card view displaying:
     * - Order ID
     * - Restaurant Rating (from "restaurantRating")
     * - Restaurant Review (from "restaurantReview")
     * - Driver details (if available)
     * - Order status, timestamps, and order items.
     */
    private void addRestaurantOrderToView(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<View> allViews = new ArrayList<>();

                // Order ID label.
                TextView title = makeLabel("Order ID: " + orderId);
                title.setTextSize(16);
                allViews.add(title);

                // Restaurant Rating.
                String restaurantRating = snapshot.child("restaurantRating").getValue(String.class);
                if (restaurantRating == null) {
                    Double ratingVal = snapshot.child("restaurantRating").getValue(Double.class);
                    restaurantRating = (ratingVal != null) ? String.valueOf(ratingVal) : "Not Rated";
                }
                Log.d(TAG, "Restaurant rating for order " + orderId + ": " + restaurantRating);
                allViews.add(makeLabel("Restaurant Rating: " + restaurantRating));

                // Restaurant Review.
                String restaurantReview = snapshot.child("restaurantReview").getValue(String.class);
                if (restaurantReview == null || restaurantReview.trim().isEmpty()) {
                    restaurantReview = "No review provided";
                }
                allViews.add(makeLabel("Restaurant Review: " + restaurantReview));

                // Driver Details.
                if (snapshot.hasChild("driver")) {
                    DataSnapshot driverSnap = snapshot.child("driver");
                    String driverName = driverSnap.child("name").getValue(String.class);
                    String driverPhone = driverSnap.child("phone").getValue(String.class);
                    String driverAssigned = driverSnap.child("assignmentTimestamp").getValue(String.class);
                    if (driverName == null) driverName = "Unknown";
                    if (driverPhone == null) driverPhone = "Unknown";
                    if (driverAssigned == null) driverAssigned = "N/A";
                    allViews.add(makeLabel("Driver Name: " + driverName));
                    allViews.add(makeLabel("Driver Phone: " + driverPhone));
                    allViews.add(makeLabel("Driver Assigned On: " + driverAssigned));
                } else {
                    allViews.add(makeLabel("Driver: Not Assigned"));
                }

                // Order Status.
                String statusStr = snapshot.child("payment").child("status").getValue(String.class);
                if (statusStr == null) {
                    statusStr = "Unknown";
                }
                allViews.add(makeLabel("Status: " + statusStr));

                // Timestamps.
                allViews.add(makeLabel("Placed: " + snapshot.child("timestamps").child("placed").getValue()));
                allViews.add(makeLabel("Delivered: " + snapshot.child("timestamps").child("delivered").getValue()));

                // Order Items.
                DataSnapshot itemsSnap = snapshot.child("orderDetails").child("items");
                if (itemsSnap.exists()) {
                    allViews.add(makeLabel("Order Items:"));
                    for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                        String foodId = itemSnap.child("foodId").getValue(String.class);
                        String foodDesc = itemSnap.child("foodDescription").getValue(String.class);
                        Double foodPrice = itemSnap.child("foodPrice").getValue(Double.class);
                        Long quantity = itemSnap.child("quantity").getValue(Long.class);

                        if (foodId == null) foodId = "Unknown Item";
                        if (foodDesc == null) foodDesc = "";
                        if (foodPrice == null) foodPrice = 0.0;
                        if (quantity == null) quantity = 0L;

                        String itemDetails = "- " + foodId + " (" + foodDesc + ") - $" + foodPrice + " x " + quantity;
                        allViews.add(makeLabel(itemDetails));
                    }
                }

                // Create a MaterialCardView to hold the order details.
                MaterialCardView cardView = new MaterialCardView(RestaurantReportActivity.this);
                cardView.setCardElevation(8f);
                cardView.setRadius(24f);
                cardView.setUseCompatPadding(true);
                cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 24);
                cardView.setLayoutParams(params);

                LinearLayout layout = new LinearLayout(RestaurantReportActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(20, 30, 20, 40);
                cardView.addView(layout);

                for (View v : allViews) {
                    layout.addView(v);
                }

                // Add the card view to the container.
                reportContainer.addView(cardView, 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RestaurantReportActivity.this, "Failed to load order details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Helper method to create a styled TextView label.
     */
    private TextView makeLabel(String text) {
        TextView tv = new TextView(RestaurantReportActivity.this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setPadding(0, 6, 0, 6);
        return tv;
    }
}
