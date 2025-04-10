package com.example.togoo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

public class AdminReportActivity extends AppCompatActivity {

    private LinearLayout reportsContainer;
    // Date formats for processing timestamps.
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Bottom Navigation (optional)
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report);

        reportsContainer = findViewById(R.id.reportsContainer);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Fetch and display all reports (orders)
        fetchReports();

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> onNavigationItemSelected(item.getItemId()));
        }
    }

    // Fetch orders (reports) from the "orders" node in Firebase
    private void fetchReports() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pair<String, Long>> reportList = new ArrayList<>();
                // Iterate all orders, regardless of status
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    String orderId = orderSnap.getKey();
                    long placedMillis = 0;
                    Object millisObj = orderSnap.child("timestamps").child("placedMillis").getValue();
                    if (millisObj != null) {
                        placedMillis = Long.parseLong(String.valueOf(millisObj));
                    }
                    reportList.add(new Pair<>(orderId, placedMillis));
                }
                // Sort newest orders first
                Collections.sort(reportList, (a, b) -> Long.compare(b.second, a.second));
                for (Pair<String, Long> entry : reportList) {
                    renderReport(entry.first);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminReportActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Render a single order as a report card.
    private void renderReport(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String orderStatus = String.valueOf(snapshot.child("status").getValue(String.class)).trim().toLowerCase();

                // Create a MaterialCardView for the report
                MaterialCardView cardView = new MaterialCardView(AdminReportActivity.this);
                cardView.setCardElevation(8f);
                cardView.setRadius(24f);
                cardView.setUseCompatPadding(true);
                cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 24);
                cardView.setLayoutParams(params);

                // Create an inner layout to hold report details
                LinearLayout innerLayout = new LinearLayout(AdminReportActivity.this);
                innerLayout.setOrientation(LinearLayout.VERTICAL);
                innerLayout.setPadding(20, 30, 20, 40);
                cardView.addView(innerLayout);

                // Basic order information
                innerLayout.addView(makeLabel("\uD83D\uDCC5 Order ID: " + orderId));

                // Update logs section
                addDivider(innerLayout, "Activity Updates");
                DataSnapshot logsSnap = snapshot.child("updateLogs");
                Map<String, List<DataSnapshot>> groupedLogs = new TreeMap<>(Collections.reverseOrder());
                for (DataSnapshot logSnap : logsSnap.getChildren()) {
                    String timestamp = logSnap.child("timestamp").getValue(String.class);
                    if (timestamp != null) {
                        try {
                            String date = dateOnlyFormat.format(inputFormat.parse(timestamp));
                            if (!groupedLogs.containsKey(date)) {
                                groupedLogs.put(date, new ArrayList<>());
                            }
                            groupedLogs.get(date).add(logSnap);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (List<DataSnapshot> logs : groupedLogs.values()) {
                    Collections.sort(logs, (a, b) -> {
                        try {
                            return inputFormat.parse(b.child("timestamp").getValue(String.class))
                                    .compareTo(inputFormat.parse(a.child("timestamp").getValue(String.class)));
                        } catch (ParseException e) {
                            return 0;
                        }
                    });
                    for (DataSnapshot log : logs) {
                        String status = log.child("status").getValue(String.class);
                        String note = log.child("note").getValue(String.class);
                        String ts = log.child("timestamp").getValue(String.class);
                        TextView logView = new TextView(AdminReportActivity.this);
                        logView.setText(Html.fromHtml("<b>" + status + ":</b> " + note + "<br><small>" + ts + "</small>",
                                Html.FROM_HTML_MODE_LEGACY));
                        logView.setPadding(0, 8, 0, 8);
                        innerLayout.addView(logView);
                    }
                }

                // Order summary section
                addDivider(innerLayout, "Order Summary");
                for (DataSnapshot itemSnap : snapshot.child("orderDetails").child("items").getChildren()) {
                    String foodId = String.valueOf(itemSnap.child("foodId").getValue());
                    String foodDesc = String.valueOf(itemSnap.child("foodDescription").getValue());
                    // Optionally include an image by using Glide if you have an ImageView.
                    String quantity = String.valueOf(itemSnap.child("quantity").getValue());
                    String price = String.valueOf(itemSnap.child("foodPrice").getValue());

                    LinearLayout row = new LinearLayout(AdminReportActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(0, 8, 0, 8);

                    TextView itemDetails = new TextView(AdminReportActivity.this);
                    itemDetails.setText("\n• " + foodId + "\n" + foodDesc + "\nQty: " + quantity + "\nUnit Price: $" + price);
                    itemDetails.setPadding(20, 0, 0, 0);
                    row.addView(itemDetails);
                    innerLayout.addView(row);
                }

                // Payment and additional order info
                innerLayout.addView(makeLabel("\uD83D\uDCB3 Payment Method: " + snapshot.child("payment").child("method").getValue()));
                innerLayout.addView(makeLabel("Tips: $" + snapshot.child("payment").child("tips").getValue()));
                innerLayout.addView(makeLabel("Subtotal: $" + snapshot.child("payment").child("subtotalBeforeTax").getValue()));
                innerLayout.addView(makeLabel("Delivery Fee: $" + snapshot.child("payment").child("deliveryFare").getValue()));
                innerLayout.addView(makeLabel("Total: $" + snapshot.child("payment").child("total").getValue()));
                innerLayout.addView(makeLabel("Status: " + orderStatus));
                innerLayout.addView(makeLabel("Transaction Ref: " + snapshot.child("payment").child("transactionId").getValue()));
                innerLayout.addView(makeLabel("\uD83D\uDCC6 Placed: " + snapshot.child("timestamps").child("placed").getValue()));
                innerLayout.addView(makeLabel("\uD83C\uDFC3 Delivered: " + snapshot.child("timestamps").child("delivered").getValue()));
                innerLayout.addView(makeLabel("\u270D\uFE0F Notes: " + snapshot.child("notes").getValue()));

                // Add the Review button
                Button reviewBtn = makeStyledButton("Review", android.R.color.holo_green_dark);
                reviewBtn.setOnClickListener(v -> showReviewDialog(orderId));
                innerLayout.addView(reviewBtn);

                // Add the report card to the container layout.
                reportsContainer.addView(cardView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminReportActivity.this, "Failed to load report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Create a simple TextView label.
    private TextView makeLabel(String text) {
        TextView tv = new TextView(AdminReportActivity.this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setPadding(0, 6, 0, 6);
        return tv;
    }

    // Add a divider label to the given layout.
    private void addDivider(LinearLayout parent, String label) {
        TextView divider = new TextView(AdminReportActivity.this);
        divider.setText("\n== " + label + " ==");
        divider.setTextSize(16);
        divider.setTextColor(ContextCompat.getColor(AdminReportActivity.this, android.R.color.holo_blue_dark));
        divider.setPadding(0, 24, 0, 12);
        parent.addView(divider);
    }

    // Create a styled button with the given label and background color.
    private Button makeStyledButton(String label, int backgroundColorResId) {
        Button button = new Button(AdminReportActivity.this);
        button.setText(label);
        button.setTextColor(ContextCompat.getColor(AdminReportActivity.this, android.R.color.white));
        button.setBackgroundColor(ContextCompat.getColor(AdminReportActivity.this, backgroundColorResId));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    // Show a dialog to add a review. The dialog is defined in dialog_review.xml.
    private void showReviewDialog(String orderId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        EditText commentEditText = dialogView.findViewById(R.id.reviewComment);
        Spinner targetSpinner = dialogView.findViewById(R.id.reviewTargetSpinner);

        // Populate the spinner with options.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Driver", "Customer", "Restaurant"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetSpinner.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Review")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String comment = commentEditText.getText().toString().trim();
                    String reviewTarget = targetSpinner.getSelectedItem().toString();
                    if (comment.isEmpty()) {
                        Toast.makeText(AdminReportActivity.this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                    } else {
                        submitReview(orderId, comment, reviewTarget);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Save the review into the order's "orderReview" child node.
    private void submitReview(String orderId, String comment, String reviewTarget) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("comment", comment);
        reviewData.put("reviewTarget", reviewTarget);
        reviewData.put("timestamp", System.currentTimeMillis());

        orderRef.child("orderReview").setValue(reviewData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminReportActivity.this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminReportActivity.this, "Submission failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Optional bottom navigation item handling.
    private boolean onNavigationItemSelected(int itemId) {
        if (itemId == R.id.navigation_home) {
            startActivity(new Intent(AdminReportActivity.this, AdminLandingActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.navigation_users) {
            startActivity(new Intent(AdminReportActivity.this, UserActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.navigation_approvals) {
            startActivity(new Intent(AdminReportActivity.this, ApprovalActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.navigation_transaction) {
            startActivity(new Intent(AdminReportActivity.this, TransactionActivity.class));
            finish();
            return true;
        }
        return false;
    }
}
