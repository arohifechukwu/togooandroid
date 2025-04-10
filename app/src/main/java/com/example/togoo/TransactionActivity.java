package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {

    private LinearLayout adminOrdersContainer;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.transactionToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // Initialize views
        adminOrdersContainer = findViewById(R.id.adminOrdersContainer);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set current item selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_transaction);

        // Handle bottom nav clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        fetchAllOrdersForAdmin();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navigation_home) {
            startActivity(new Intent(this, AdminLandingActivity.class));
        } else if (id == R.id.navigation_users) {
            startActivity(new Intent(this, UserActivity.class));
        } else if (id == R.id.navigation_approvals) {
            startActivity(new Intent(this, ApprovalActivity.class));
        } else if (id == R.id.navigation_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.navigation_transaction) {
            return true; // Stay on the same screen
        }
        finish();
        return true;
    }

    private void fetchAllOrdersForAdmin() {
        DatabaseReference allOrdersRef = FirebaseDatabase.getInstance().getReference("orders");

        allOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    String orderId = orderSnap.getKey();
                    if (orderId != null) {
                        addOrderCardToAdminView(orderId, orderSnap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addOrderCardToAdminView(String orderId, DataSnapshot snapshot) {
        List<View> allViews = new ArrayList<>();

        TextView title = makeLabel("\uD83D\uDCC5 Order ID: " + orderId);
        title.setTextSize(16);
        allViews.add(title);

        DataSnapshot itemsSnap = snapshot.child("orderDetails/items");
        for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
            String foodDesc = String.valueOf(itemSnap.child("foodDescription").getValue());
            String foodImage = String.valueOf(itemSnap.child("foodImage").getValue());
            String quantity = String.valueOf(itemSnap.child("quantity").getValue());
            String price = String.valueOf(itemSnap.child("foodPrice").getValue());

            LinearLayout itemRow = new LinearLayout(this);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setPadding(0, 8, 0, 8);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(100, 100);
            imageView.setLayoutParams(imgParams);
            Glide.with(this).load(foodImage).into(imageView);

            TextView desc = new TextView(this);
            desc.setText("\n" + foodDesc + "\nQty: " + quantity + "\nUnit Price: $" + price);
            desc.setPadding(20, 0, 0, 0);

            itemRow.addView(imageView);
            itemRow.addView(desc);
            allViews.add(itemRow);
        }

        String status = String.valueOf(snapshot.child("payment/status").getValue());
        TextView statusLabel = makeLabel("Status: " + status);
        statusLabel.setTextSize(16);
        if ("delivered".equalsIgnoreCase(status)) {
            statusLabel.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if ("pending".equalsIgnoreCase(status)) {
            statusLabel.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            statusLabel.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        allViews.add(statusLabel);

        allViews.add(makeLabel("Total: $" + snapshot.child("payment/total").getValue()));
        allViews.add(makeLabel("Payment Method: " + snapshot.child("payment/method").getValue()));
        allViews.add(makeLabel("Placed At: " + snapshot.child("timestamps/placed").getValue()));

        String customerId = snapshot.child("customerId").getValue(String.class);
        if (customerId != null) {
            FirebaseDatabase.getInstance().getReference("users").child(customerId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnap) {
                            allViews.add(makeLabel("\uD83D\uDC64 Customer: " + userSnap.child("username").getValue()));
                            allViews.add(makeLabel("\uD83D\uDCDE Phone: " + userSnap.child("phone").getValue()));
                            renderCard(allViews);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            renderCard(allViews);
                        }
                    });
        } else {
            renderCard(allViews);
        }
    }

    private void renderCard(List<View> views) {
        MaterialCardView cardView = new MaterialCardView(this);
        cardView.setCardElevation(8f);
        cardView.setRadius(24f);
        cardView.setUseCompatPadding(true);
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        cardView.setLayoutParams(params);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 30, 20, 40);
        cardView.addView(layout);

        for (View v : views) {
            layout.addView(v);
        }

        adminOrdersContainer.addView(cardView, 0);
    }

    private TextView makeLabel(Object text) {
        TextView tv = new TextView(this);
        tv.setText(String.valueOf(text));
        tv.setTextSize(14);
        tv.setPadding(0, 6, 0, 6);
        return tv;
    }
}
