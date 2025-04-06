package com.example.togoo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout notificationsContainer;
    private DatabaseReference ordersRef;
    private String currentUserId;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        notificationsContainer = findViewById(R.id.notificationsContainer);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("ordersByCustomer").child(currentUserId);

        listenForOrderUpdates();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new android.content.Intent(NotificationsActivity.this, AccountActivity.class));
            finish(); // Optional: to remove NotificationsActivity from back stack
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void listenForOrderUpdates() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsContainer.removeAllViews();
                if (!snapshot.exists()) {
                    showMessage("No orders yet.");
                    return;
                }
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    String orderId = orderSnap.getKey();
                    if (orderId != null) {
                        loadOrderNotifications(orderId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Failed to load your orders.");
            }
        });
    }

    private void loadOrderNotifications(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String restaurantId = snapshot.child("restaurant/id").getValue(String.class);
                String driverId = snapshot.child("driver/id").getValue(String.class);
                String orderStatus = snapshot.child("status").getValue(String.class);
                String estimatedTime = snapshot.child("estimatedDeliveryTime").getValue(String.class);

                LinearLayout orderLayout = new LinearLayout(NotificationsActivity.this);
                orderLayout.setOrientation(LinearLayout.VERTICAL);
                orderLayout.setPadding(20, 30, 20, 30);

                TextView orderTitle = new TextView(NotificationsActivity.this);
                orderTitle.setText("Order ID: " + orderId);
                orderTitle.setTextSize(16);
                orderTitle.setTextColor(ContextCompat.getColor(NotificationsActivity.this, android.R.color.black));
                orderLayout.addView(orderTitle);

                DataSnapshot logsSnap = snapshot.child("updateLogs");
                Map<String, List<DataSnapshot>> groupedLogs = new TreeMap<>(Collections.reverseOrder());

                for (DataSnapshot logSnap : logsSnap.getChildren()) {
                    String timestamp = logSnap.child("timestamp").getValue(String.class);
                    if (timestamp != null) {
                        try {
                            String date = dateOnlyFormat.format(inputFormat.parse(timestamp));
                            groupedLogs.putIfAbsent(date, new ArrayList<>());
                            groupedLogs.get(date).add(logSnap);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Sort logs inside each date group by timestamp descending
                for (List<DataSnapshot> logList : groupedLogs.values()) {
                    logList.sort((a, b) -> {
                        try {
                            Date dateA = inputFormat.parse(a.child("timestamp").getValue(String.class));
                            Date dateB = inputFormat.parse(b.child("timestamp").getValue(String.class));
                            return dateB.compareTo(dateA); // latest first
                        } catch (ParseException e) {
                            return 0;
                        }
                    });
                }

                for (Map.Entry<String, List<DataSnapshot>> entry : groupedLogs.entrySet()) {
                    TextView dateHeader = new TextView(NotificationsActivity.this);
                    dateHeader.setText("\n📅 " + entry.getKey());
                    dateHeader.setTextSize(15);
                    dateHeader.setTextColor(ContextCompat.getColor(NotificationsActivity.this, android.R.color.holo_blue_dark));
                    orderLayout.addView(dateHeader);

                    for (DataSnapshot log : entry.getValue()) {
                        String status = log.child("status").getValue(String.class);
                        String note = log.child("note").getValue(String.class);
                        String timestamp = log.child("timestamp").getValue(String.class);

                        TextView logView = new TextView(NotificationsActivity.this);
                        logView.setText(Html.fromHtml("<b>" + status + ":</b> " + note + "<br><small>" + timestamp + "</small>", Html.FROM_HTML_MODE_LEGACY));
                        logView.setPadding(0, 8, 0, 8);
                        orderLayout.addView(logView);
                    }
                }

                if ("delivered".equalsIgnoreCase(orderStatus)) {
                    Button rateBtn = new Button(NotificationsActivity.this);
                    rateBtn.setText("Rate Your Order");
                    rateBtn.setBackgroundColor(ContextCompat.getColor(NotificationsActivity.this, android.R.color.holo_blue_dark));
                    rateBtn.setTextColor(ContextCompat.getColor(NotificationsActivity.this, android.R.color.white));
                    rateBtn.setOnClickListener(v -> showRatingDialog(orderId, restaurantId, driverId));
                    orderLayout.addView(rateBtn);
                }

                if (driverId != null && !driverId.isEmpty()) {
                    Button driverBtn = new Button(NotificationsActivity.this);
                    driverBtn.setText("Know Your Driver");
                    driverBtn.setBackgroundColor(ContextCompat.getColor(NotificationsActivity.this, android.R.color.holo_orange_dark));
                    driverBtn.setTextColor(ContextCompat.getColor(NotificationsActivity.this, android.R.color.white));
                    driverBtn.setOnClickListener(v -> showDriverInfoDialog(driverId, estimatedTime));
                    orderLayout.addView(driverBtn);
                }

                notificationsContainer.addView(orderLayout, 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Failed to load order updates.");
            }
        });
    }

    private void showDriverInfoDialog(String driverId, String estimatedTime) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_know_driver, null);
        ImageView profileImage = dialogView.findViewById(R.id.driverImage);
        ImageView carImage = dialogView.findViewById(R.id.carImage);
        TextView nameText = dialogView.findViewById(R.id.driverName);
        TextView phoneText = dialogView.findViewById(R.id.driverPhone);
        TextView plateText = dialogView.findViewById(R.id.licensePlate);
        TextView carTypeText = dialogView.findViewById(R.id.carType);
        TextView modelText = dialogView.findViewById(R.id.carModel);
        TextView etaText = dialogView.findViewById(R.id.estimatedTime);

        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("driver").child(driverId);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                nameText.setText("Name: " + snap.child("name").getValue(String.class));
                phoneText.setText("Phone: " + snap.child("phone").getValue(String.class));
                plateText.setText("License Plate: " + snap.child("licensePlate").getValue(String.class));
                carTypeText.setText("Car Type: " + snap.child("carBrand").getValue(String.class));
                modelText.setText("Model: " + snap.child("carModel").getValue(String.class));
                etaText.setText("ETA: " + estimatedTime);
                Glide.with(NotificationsActivity.this).load(snap.child("imageURL").getValue(String.class)).into(profileImage);
                Glide.with(NotificationsActivity.this).load(snap.child("carPicture").getValue(String.class)).into(carImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationsActivity.this, "Failed to load driver info", Toast.LENGTH_SHORT).show();
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Your Delivery Driver")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showRatingDialog(String orderId, String restaurantId, String driverId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_order, null);
        RatingBar restaurantRatingBar = dialogView.findViewById(R.id.restaurantRatingBar);
        RatingBar driverRatingBar = dialogView.findViewById(R.id.driverRatingBar);

        new AlertDialog.Builder(this)
                .setTitle("Rate Your Order")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float restaurantRating = restaurantRatingBar.getRating();
                    float driverRating = driverRatingBar.getRating();
                    updateRating("restaurant", restaurantId, restaurantRating);
                    updateRating("driver", driverId, driverRating);
                    Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateRating(String role, String uid, float newRating) {
        DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference(role).child(uid).child("rating");
        ratingRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Double existing = currentData.getValue(Double.class);
                if (existing == null) {
                    currentData.setValue((double) newRating);
                } else {
                    double average = (existing + newRating) / 2.0;
                    currentData.setValue(average);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
            }
        });
    }

    private void showMessage(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(30, 40, 30, 40);
        notificationsContainer.addView(tv);
    }
}
