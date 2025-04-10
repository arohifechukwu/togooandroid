package com.example.togoo;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;
import java.util.UUID;


public class MyOrderActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private String currentUserId;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;
    private ImageView imagePreviewInDialog;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("My Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ordersContainer = findViewById(R.id.ordersContainer);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchOrders();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_myorders);
        bottomNavigation.setOnItemSelectedListener(item -> onNavigationItemSelected(item.getItemId()));
    }


    private void fetchOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.orderByChild("customer/id").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Pair<String, Long>> orderList = new ArrayList<>();

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            String orderId = orderSnap.getKey();
                            long placedMillis = 0;

                            Object millisObj = orderSnap.child("timestamps").child("placedMillis").getValue();
                            if (millisObj != null) {
                                placedMillis = Long.parseLong(String.valueOf(millisObj));
                            }

                            Log.d("ORDER_SORT", "Order ID: " + orderId + " — placedMillis: " + placedMillis);
                            orderList.add(new Pair<>(orderId, placedMillis));
                        }

                        // Sort newest first
                        orderList.sort((a, b) -> Long.compare(b.second, a.second));

                        for (Pair<String, Long> entry : orderList) {
                            renderOrder(entry.first);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MyOrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void renderOrder(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String restaurantId = snapshot.child("restaurant/id").getValue(String.class);
                String driverId = snapshot.child("driver/id").getValue(String.class);
                String eta = snapshot.child("estimatedDeliveryTime").getValue(String.class);
                String orderStatus = String.valueOf(snapshot.child("status").getValue(String.class)).trim().toLowerCase();

                MaterialCardView cardView = new MaterialCardView(MyOrderActivity.this);
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

                LinearLayout finalLayout = new LinearLayout(MyOrderActivity.this);
                finalLayout.setOrientation(LinearLayout.VERTICAL);
                finalLayout.setPadding(20, 30, 20, 40);
                cardView.addView(finalLayout);

                finalLayout.addView(makeLabel("\uD83D\uDCC5 Order ID: " + orderId));
                addDivider(finalLayout, "Activity Updates");

                DataSnapshot logsSnap = snapshot.child("updateLogs");
                Map<String, List<DataSnapshot>> groupedLogs = new TreeMap<>(Collections.reverseOrder());
                for (DataSnapshot logSnap : logsSnap.getChildren()) {
                    String timestamp = logSnap.child("timestamp").getValue(String.class);
                    if (timestamp != null) {
                        try {
                            String date = dateOnlyFormat.format(inputFormat.parse(timestamp));
                            groupedLogs.putIfAbsent(date, new ArrayList<>());
                            groupedLogs.get(date).add(logSnap);
                        } catch (ParseException e) { e.printStackTrace(); }
                    }
                }

                for (List<DataSnapshot> logs : groupedLogs.values()) {
                    logs.sort((a, b) -> {
                        try {
                            return inputFormat.parse(b.child("timestamp").getValue(String.class))
                                    .compareTo(inputFormat.parse(a.child("timestamp").getValue(String.class)));
                        } catch (ParseException e) { return 0; }
                    });
                    for (DataSnapshot log : logs) {
                        String status = log.child("status").getValue(String.class);
                        String note = log.child("note").getValue(String.class);
                        String ts = log.child("timestamp").getValue(String.class);
                        TextView logView = new TextView(MyOrderActivity.this);
                        logView.setText(Html.fromHtml("<b>" + status + ":</b> " + note + "<br><small>" + ts + "</small>", Html.FROM_HTML_MODE_LEGACY));
                        logView.setPadding(0, 8, 0, 8);
                        finalLayout.addView(logView);
                    }
                }

                addDivider(finalLayout, "Order Summary");

                for (DataSnapshot itemSnap : snapshot.child("orderDetails/items").getChildren()) {
                    String foodId = String.valueOf(itemSnap.child("foodId").getValue());
                    String foodDesc = String.valueOf(itemSnap.child("foodDescription").getValue());
                    String foodImage = String.valueOf(itemSnap.child("foodImage").getValue());
                    String quantity = String.valueOf(itemSnap.child("quantity").getValue());
                    String price = String.valueOf(itemSnap.child("foodPrice").getValue());

                    LinearLayout row = new LinearLayout(MyOrderActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(0, 8, 0, 8);

                    ImageView image = new ImageView(MyOrderActivity.this);
                    image.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                    Glide.with(MyOrderActivity.this).load(foodImage).into(image);

                    TextView desc = new TextView(MyOrderActivity.this);
                    desc.setText("\n• " + foodId + "\n" + foodDesc + "\nQty: " + quantity + "\nUnit Price: $" + price);
                    desc.setPadding(20, 0, 0, 0);

                    row.addView(image);
                    row.addView(desc);
                    finalLayout.addView(row);
                }

                finalLayout.addView(makeLabel("\uD83D\uDCB3 Payment Method: " + snapshot.child("payment/method").getValue()));
                finalLayout.addView(makeLabel("Tips: $" + snapshot.child("payment/tips").getValue()));
                finalLayout.addView(makeLabel("Subtotal: $" + snapshot.child("payment/subtotalBeforeTax").getValue()));
                finalLayout.addView(makeLabel("Delivery Fee: $" + snapshot.child("payment/deliveryFare").getValue()));
                finalLayout.addView(makeLabel("Total: $" + snapshot.child("payment/total").getValue()));
                finalLayout.addView(makeLabel("Status: " + orderStatus));
                finalLayout.addView(makeLabel("Transaction Ref: " + snapshot.child("payment/transactionId").getValue()));
                finalLayout.addView(makeLabel("\uD83D\uDCC6 Placed: " + snapshot.child("timestamps/placed").getValue()));
                finalLayout.addView(makeLabel("\uD83C\uDFC3 Delivered: " + snapshot.child("timestamps/delivered").getValue()));
                finalLayout.addView(makeLabel("\u270D\uFE0F Notes: " + snapshot.child("notes").getValue()));

                if (restaurantId != null) {
                    FirebaseDatabase.getInstance().getReference("restaurant").child(restaurantId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot resSnap) {
                                    finalLayout.addView(makeLabel("🍽 Restaurant: " + resSnap.child("name").getValue(String.class)));
                                    finalLayout.addView(makeLabel("📍 Address: " + resSnap.child("address").getValue(String.class)));

                                    if (driverId != null) {
                                        FirebaseDatabase.getInstance().getReference("driver").child(driverId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot driverSnap) {
                                                        finalLayout.addView(makeLabel("👤 Driver: " + driverSnap.child("name").getValue(String.class)));
                                                        finalLayout.addView(makeLabel("📞 Phone: " + driverSnap.child("phone").getValue(String.class)));
                                                        finalLayout.addView(makeLabel("🚗 Vehicle: " + driverSnap.child("carBrand").getValue(String.class) + " " + driverSnap.child("carModel").getValue(String.class)));
                                                        finalLayout.addView(makeLabel("📋 Plate: " + driverSnap.child("licensePlate").getValue(String.class)));

                                                        if (driverId != null && !driverId.isEmpty()) {
                                                            Button knowDriverBtn = makeStyledButton("Know Your Driver", android.R.color.holo_orange_dark);
                                                            knowDriverBtn.setOnClickListener(v -> showDriverInfoDialog(driverId, eta));
                                                            finalLayout.addView(knowDriverBtn);
                                                        }

                                                        if ("delivered".equals(orderStatus)) {
                                                            Button rateBtn = makeStyledButton("Rate Your Order", android.R.color.holo_blue_dark);
                                                            rateBtn.setOnClickListener(v -> showRatingDialog(orderId, restaurantId, driverId));
                                                            finalLayout.addView(rateBtn);

                                                            Button complaintBtn = makeStyledButton("Log A Complaint", android.R.color.holo_red_dark);
                                                            complaintBtn.setOnClickListener(v -> showDisputeForm(orderId));
                                                            finalLayout.addView(complaintBtn);
                                                        }

                                                        ordersContainer.addView(cardView);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        ordersContainer.addView(cardView);
                                                    }
                                                });
                                    } else {
                                        ordersContainer.addView(cardView);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    ordersContainer.addView(cardView);
                                }
                            });
                } else {
                    ordersContainer.addView(cardView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyOrderActivity.this, "Failed to load order", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private Button makeStyledButton(String label, int backgroundColorResId) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        button.setBackgroundColor(ContextCompat.getColor(this, backgroundColorResId));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 16, 0, 0);
        button.setLayoutParams(layoutParams);
        return button;
    }




    private void showRatingDialog(String orderId, String restaurantId, String driverId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_order, null);
        RatingBar restaurantRatingBar = dialogView.findViewById(R.id.restaurantRatingBar);
        RatingBar driverRatingBar = dialogView.findViewById(R.id.driverRatingBar);
        EditText restaurantComment = dialogView.findViewById(R.id.restaurantComment);
        EditText driverComment = dialogView.findViewById(R.id.driverComment);

        new AlertDialog.Builder(this)
                .setTitle("Rate Your Order")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    float restRating = restaurantRatingBar.getRating();
                    float drvRating = driverRatingBar.getRating();
                    String restComment = restaurantComment.getText().toString().trim();
                    String drvComment = driverComment.getText().toString().trim();

                    updateRatingWithComment("restaurant", restaurantId, currentUserId, restRating, restComment);
                    updateRatingWithComment("driver", driverId, currentUserId, drvRating, drvComment);

                    Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateRatingWithComment(String role, String uid, String customerId, float rating, String comment) {
        DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference(role).child(uid).child("ratings").child(customerId);

        Map<String, Object> data = new HashMap<>();
        data.put("value", rating);
        data.put("comment", comment);
        data.put("timestamp", System.currentTimeMillis());

        ratingRef.setValue(data);

        // Optional: Calculate new average
        updateAverageRating(role, uid);
    }

    private void updateAverageRating(String role, String uid) {
        DatabaseReference allRatingsRef = FirebaseDatabase.getInstance().getReference(role).child(uid).child("ratings");

        allRatingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float sum = 0;
                int count = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Float value = snap.child("value").getValue(Float.class);
                    if (value != null) {
                        sum += value;
                        count++;
                    }
                }

                float avg = (count == 0) ? 0 : sum / count;
                FirebaseDatabase.getInstance().getReference(role).child(uid).child("rating").setValue(avg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RatingCalc", "Failed to calculate average rating", error.toException());
            }
        });
    }


    private void showDriverInfoDialog(String driverId, String eta) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_know_driver, null);
        ImageView profile = dialogView.findViewById(R.id.driverImage);
        ImageView car = dialogView.findViewById(R.id.carImage);
        TextView name = dialogView.findViewById(R.id.driverName);
        TextView phone = dialogView.findViewById(R.id.driverPhone);
        TextView plate = dialogView.findViewById(R.id.licensePlate);
        TextView type = dialogView.findViewById(R.id.carType);
        TextView model = dialogView.findViewById(R.id.carModel);
        TextView etaView = dialogView.findViewById(R.id.estimatedTime);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driver").child(driverId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                name.setText("Name: " + snap.child("name").getValue(String.class));
                phone.setText("Phone: " + snap.child("phone").getValue(String.class));
                plate.setText("License Plate: " + snap.child("licensePlate").getValue(String.class));
                type.setText("Car Type: " + snap.child("carBrand").getValue(String.class));
                model.setText("Model: " + snap.child("carModel").getValue(String.class));
                etaView.setText("ETA: " + eta);
                Glide.with(MyOrderActivity.this).load(snap.child("imageURL").getValue(String.class)).into(profile);
                Glide.with(MyOrderActivity.this).load(snap.child("carPicture").getValue(String.class)).into(car);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyOrderActivity.this, "Driver info failed", Toast.LENGTH_SHORT).show();
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Your Delivery Driver")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showDisputeForm(String orderId) {
        View formView = LayoutInflater.from(this).inflate(R.layout.dialog_log_complaint, null);
        EditText title = formView.findViewById(R.id.disputeTitle);
        EditText desc = formView.findViewById(R.id.disputeDescription);
        EditText reason = formView.findViewById(R.id.disputeReason);
        imagePreviewInDialog = formView.findViewById(R.id.evidencePreview);
        Button uploadBtn = formView.findViewById(R.id.uploadEvidenceButton);

        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        new AlertDialog.Builder(this)
                .setTitle("Dispute Form")
                .setView(formView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String t = title.getText().toString().trim();
                    String d = desc.getText().toString().trim();
                    String r = reason.getText().toString().trim();
                    if (selectedImageUri != null) uploadEvidence(orderId, t, d, r, selectedImageUri);
                    else storeDispute(orderId, t, d, r, null);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadEvidence(String orderId, String title, String desc, String reason, Uri uri) {
        StorageReference ref = FirebaseStorage.getInstance().getReference("disputes/")
                .child(UUID.randomUUID().toString() + ".jpg");
        ref.putFile(uri).addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(downloadUri ->
                storeDispute(orderId, title, desc, reason, downloadUri.toString())
        ));
    }

    private void storeDispute(String orderId, String title, String desc, String reason, @Nullable String imageURL) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orders").child(orderId).child("dispute");
        ref.child("details/disputeTitle").setValue(title);
        ref.child("details/description").setValue(desc);
        if (imageURL != null) ref.child("details/imageURL").setValue(imageURL);
        ref.child("reason").setValue(reason);
        ref.child("status").setValue("pending");
        ref.child("timestamp").setValue(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));
        Toast.makeText(this, "Dispute submitted", Toast.LENGTH_SHORT).show();
    }

    private void addDivider(LinearLayout parent, String label) {
        TextView title = new TextView(this);
        title.setText("\n== " + label + " ==");
        title.setTextSize(16);
        title.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        title.setPadding(0, 24, 0, 12);
        parent.addView(title);
    }

    private TextView makeBoldLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        tv.setPadding(0, 10, 0, 10);
        return tv;
    }

    private TextView makeLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setPadding(0, 6, 0, 6);
        return tv;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (imagePreviewInDialog != null) {
                imagePreviewInDialog.setVisibility(View.VISIBLE);
                imagePreviewInDialog.setImageURI(selectedImageUri);
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull int item) {
        int id = item;

        if (id == R.id.navigation_home) {
            startActivity(new Intent(this, CustomerLandingActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_restaurant) {
            startActivity(new Intent(this, RestaurantActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_myorders) {
            return true; // Already on this screen
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
