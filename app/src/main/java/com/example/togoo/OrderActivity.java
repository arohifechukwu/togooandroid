package com.example.togoo;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private String currentUserId;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;
    private ImageView imagePreviewInDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ordersContainer = findViewById(R.id.ordersContainer);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchOrders();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.navigation_order);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(this, CustomerLandingActivity.class));
            } else if (id == R.id.navigation_restaurant) {
                startActivity(new Intent(this, RestaurantActivity.class));
            } else if (id == R.id.navigation_account) {
                startActivity(new Intent(this, AccountActivity.class));
            }
            finish();
            return true;
        });
    }

    private void fetchOrders() {
        DatabaseReference customerOrdersRef = FirebaseDatabase.getInstance()
                .getReference("ordersByCustomer").child(currentUserId);

        customerOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pair<String, Long>> orderList = new ArrayList<>();

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    String orderId = orderSnap.getKey();
                    if (orderId != null) {
                        Object timestampObj = orderSnap.child("timestamps").child("placed").getValue();
                        long timestamp = 0;
                        try {
                            timestamp = Long.parseLong(String.valueOf(timestampObj));
                        } catch (Exception ignored) {}

                        orderList.add(new Pair<>(orderId, timestamp));
                    }
                }

                // Sort in descending order (most recent first)
                orderList.sort((a, b) -> Long.compare(b.second, a.second));

                for (Pair<String, Long> entry : orderList) {
                    addOrderToView(entry.first);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void addOrderToView(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<View> allViews = new ArrayList<>();
                List<Pair<String, String>> restaurantInfo = new ArrayList<>();
                List<Pair<String, String>> driverInfo = new ArrayList<>();
                boolean[] dataLoaded = {false, false};

                // Text: Order ID
                TextView title = makeLabel("\uD83D\uDCC5 Order ID: " + orderId);
                title.setTextSize(16);
                allViews.add(title);

                // Food items
                DataSnapshot itemsSnap = snapshot.child("orderDetails/items");
                for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                    String foodId = String.valueOf(itemSnap.child("foodId").getValue());
                    String foodDesc = String.valueOf(itemSnap.child("foodDescription").getValue());
                    String foodImage = String.valueOf(itemSnap.child("foodImage").getValue());
                    String quantity = String.valueOf(itemSnap.child("quantity").getValue());
                    String price = String.valueOf(itemSnap.child("foodPrice").getValue());

                    LinearLayout itemRow = new LinearLayout(OrderActivity.this);
                    itemRow.setOrientation(LinearLayout.HORIZONTAL);
                    itemRow.setPadding(0, 8, 0, 8);

                    ImageView imageView = new ImageView(OrderActivity.this);
                    LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(100, 100);
                    imageView.setLayoutParams(imgParams);
                    Glide.with(OrderActivity.this).load(foodImage).into(imageView);

                    TextView desc = new TextView(OrderActivity.this);
                    desc.setText("\n\u2022 " + foodId + "\n" + foodDesc + "\nQty: " + quantity + "\nUnit Price: $" + price);
                    desc.setPadding(20, 0, 0, 0);

                    itemRow.addView(imageView);
                    itemRow.addView(desc);
                    allViews.add(itemRow);
                }

                // Payment and timestamps
                allViews.add(makeLabel("\uD83D\uDCB3 Payment Method: " + snapshot.child("payment/method").getValue()));
                allViews.add(makeLabel("Tips: $" + snapshot.child("payment/tips").getValue()));
                allViews.add(makeLabel("Subtotal: $" + snapshot.child("payment/subtotalBeforeTax").getValue()));
                allViews.add(makeLabel("Delivery Fee: $" + snapshot.child("payment/deliveryFare").getValue()));
                allViews.add(makeLabel("Total: $" + snapshot.child("payment/total").getValue()));
                allViews.add(makeLabel("Status: " + snapshot.child("payment/status").getValue()));
                allViews.add(makeLabel("Transaction Ref: " + snapshot.child("payment/transactionId").getValue()));
                allViews.add(makeLabel("\uD83D\uDCC6 Placed: " + snapshot.child("timestamps/placed").getValue()));
                allViews.add(makeLabel("\uD83C\uDFC3 Delivered: " + snapshot.child("timestamps/delivered").getValue()));
                allViews.add(makeLabel("\u270D\uFE0F Notes: " + snapshot.child("notes").getValue()));


                // MaterialCardView setup
                MaterialCardView cardView = new MaterialCardView(OrderActivity.this);
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

                LinearLayout finalLayout = new LinearLayout(OrderActivity.this);
                finalLayout.setOrientation(LinearLayout.VERTICAL);
                finalLayout.setPadding(20, 30, 20, 40);
                cardView.addView(finalLayout);

                Runnable maybeAttachToScreen = () -> {
                    if (dataLoaded[0] && dataLoaded[1]) {
                        for (View v : allViews) finalLayout.addView(v);

                        for (Pair<String, String> info : restaurantInfo)
                            finalLayout.addView(makeLabel(info.first + ": " + info.second));

                        for (Pair<String, String> info : driverInfo)
                            finalLayout.addView(makeLabel(info.first + ": " + info.second));

                        Button complaintButton = new Button(OrderActivity.this);
                        complaintButton.setText("Log a Complaint");
                        complaintButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                        complaintButton.setTextColor(getResources().getColor(android.R.color.white));
                        complaintButton.setOnClickListener(v -> showDisputeForm(orderId));
                        finalLayout.addView(complaintButton);

                        ordersContainer.addView(cardView);
                    }
                };

                String restId = snapshot.child("restaurant/id").getValue(String.class);
                if (restId != null) {
                    FirebaseDatabase.getInstance().getReference("restaurant").child(restId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot resSnap) {
                                    restaurantInfo.add(new Pair<>("\uD83C\uDF7D Restaurant", resSnap.child("name").getValue(String.class)));
                                    restaurantInfo.add(new Pair<>("\uD83D\uDCCD Address", resSnap.child("address").getValue(String.class)));
                                    dataLoaded[0] = true;
                                    maybeAttachToScreen.run();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    dataLoaded[0] = true;
                                    maybeAttachToScreen.run();
                                }
                            });
                } else {
                    dataLoaded[0] = true;
                    maybeAttachToScreen.run();
                }

                String driverId = snapshot.child("driver/id").getValue(String.class);
                if (driverId != null) {
                    FirebaseDatabase.getInstance().getReference("driver").child(driverId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot driverSnap) {
                                    driverInfo.add(new Pair<>("\uD83D\uDC64 Driver", driverSnap.child("name").getValue(String.class)));
                                    driverInfo.add(new Pair<>("\uD83D\uDCDE Phone", driverSnap.child("phone").getValue(String.class)));
                                    driverInfo.add(new Pair<>("\uD83D\uDE97 Vehicle",
                                            driverSnap.child("carBrand").getValue(String.class) + " " +
                                                    driverSnap.child("carModel").getValue(String.class)));
                                    driverInfo.add(new Pair<>("\uD83D\uDCCB Plate", driverSnap.child("licensePlate").getValue(String.class)));
                                    dataLoaded[1] = true;
                                    maybeAttachToScreen.run();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    dataLoaded[1] = true;
                                    maybeAttachToScreen.run();
                                }
                            });
                } else {
                    dataLoaded[1] = true;
                    maybeAttachToScreen.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderActivity.this, "Failed to load order details", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private TextView makeLabel(Object text) {
        TextView tv = new TextView(this);
        tv.setText(String.valueOf(text));
        tv.setTextSize(14);
        tv.setPadding(0, 6, 0, 6);
        return tv;
    }


    private void showDisputeForm(String orderId) {
        View formView = LayoutInflater.from(this).inflate(R.layout.dialog_log_complaint, null);
        formView.setAlpha(0f); // Fade-in animation setup
        formView.animate().alpha(1f).setDuration(300).start(); // Animate fade-in

        EditText titleInput = formView.findViewById(R.id.disputeTitle);
        EditText descriptionInput = formView.findViewById(R.id.disputeDescription);
        EditText reasonInput = formView.findViewById(R.id.disputeReason);
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
                    String title = titleInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    String reason = reasonInput.getText().toString().trim();

                    if (selectedImageUri != null) {
                        uploadEvidenceImage(orderId, title, description, reason, selectedImageUri);
                    } else {
                        storeDisputeData(orderId, title, description, reason, null);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void storeDisputeData(String orderId, String title, String description, String reason, @Nullable String imageURL) {
        DatabaseReference disputeRef = FirebaseDatabase.getInstance()
                .getReference("orders").child(orderId).child("dispute");

        DatabaseReference detailsRef = disputeRef.child("details");
        detailsRef.child("disputeTitle").setValue(title);
        detailsRef.child("description").setValue(description);
        if (imageURL != null) detailsRef.child("imageURL").setValue(imageURL);

        disputeRef.child("reason").setValue(reason);
        disputeRef.child("status").setValue("pending");

        // Store timestamp in ISO 8601 format
        String isoTimestamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new java.util.Date());
        disputeRef.child("timestamp").setValue(isoTimestamp);

        Toast.makeText(this, "Dispute submitted.", Toast.LENGTH_SHORT).show();
    }


    private void uploadEvidenceImage(String orderId, String title, String description, String reason, Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("disputes/")
                .child(UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                        storeDisputeData(orderId, title, description, reason, uri.toString())
                )
        ).addOnFailureListener(e -> {
            Toast.makeText(OrderActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
}
