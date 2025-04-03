package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.togoo.models.CartItem;
import com.example.togoo.models.Customer;
import com.example.togoo.models.Restaurant;
import com.example.togoo.utils.RestaurantHelper;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class PaymentActivity extends AppCompatActivity {

    private Button btnCreditCard;
    private PaymentSheet paymentSheet;

    private static final String publishableKey = "pk_test_51Pju1z08k0nHIvbw5cvH5RvHpaKxzOJBcNCKKRpkJnXw96nsbEQ3FLKQOUYNVF6w5fff34S2pgn7J3fdzkdEi8Kk003V6xBVlv";
//    private static final String BASE_URL = "http://10.0.2.2:4242"; //Emulator to localhost
    private static final String BASE_URL = "https://a015-91-196-220-86.ngrok-free.app"; //URL generated from secure server per use

    private String customerId, ephemeralKey, clientSecret;
    private double checkoutTotal;
    private List<CartItem> cartItems;
    private Customer customer;
    private Restaurant restaurant;
    private String orderNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        btnCreditCard = findViewById(R.id.btnCreditCard);
        cartItems = getIntent().getParcelableArrayListExtra("checkoutItems");
        checkoutTotal = getIntent().getDoubleExtra("checkoutTotal", 0.0);
        customer = getIntent().getParcelableExtra("currentCustomer");
        restaurant = RestaurantHelper.getCurrentRestaurant();
        orderNote = getIntent().getStringExtra("orderNote");

        if (customer == null) {
            Toast.makeText(this, "Missing customer info", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        PaymentConfiguration.init(this, publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        btnCreditCard.setOnClickListener(v -> {
            if (clientSecret != null && customerId != null && ephemeralKey != null) {
                PaymentSheet.CustomerConfiguration config = new PaymentSheet.CustomerConfiguration(customerId, ephemeralKey);
                PaymentSheet.Configuration sheetConfig = new PaymentSheet.Configuration("ToGoo Checkout", config);
                paymentSheet.presentWithPaymentIntent(clientSecret, sheetConfig);
            } else {
                Toast.makeText(this, "Payment not ready yet", Toast.LENGTH_SHORT).show();
            }
        });

        createCustomer();
    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + "/create-customer",
                response -> {
                    try {
                        customerId = new JSONObject(response).getString("id");
                        new Handler(Looper.getMainLooper()).postDelayed(this::getEphemeralKey, 400);
                    } catch (JSONException e) {
                        showError("Failed to parse customer");
                    }
                }, error -> showError("Failed to create customer"));
        Volley.newRequestQueue(this).add(request);
    }


    private void getEphemeralKey() {
        try {
            JSONObject params = new JSONObject();
            params.put("customerId", customerId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BASE_URL + "/create-ephemeral-key",
                    params,
                    response -> {
                        ephemeralKey = response.optString("secret");
                        Log.d("StripeDebug", "Ephemeral key created: " + ephemeralKey);
                        createPaymentIntent();
                    },
                    error -> {
                        Log.e("StripeDebug", "Failed to get ephemeral key: " + error.toString());
                        showError("Failed to get ephemeral key");
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) {
            showError("Error building request");
        }
    }


    private void createPaymentIntent() {
        try {
            JSONObject params = new JSONObject();
            params.put("amount", (int) (checkoutTotal * 100)); // Stripe requires amount in cents
            params.put("currency", "cad");
            params.put("customerId", customerId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BASE_URL + "/create-payment-intent",
                    params,
                    response -> {
                        clientSecret = response.optString("clientSecret");
                        Toast.makeText(this, "Ready to pay", Toast.LENGTH_SHORT).show();
                        Log.d("StripeDebug", "ClientSecret: " + clientSecret);
                    },
                    error -> {
                        Log.e("StripeDebug", "Failed to create payment intent: " + error.toString());
                        showError("Failed to create payment intent");
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) {
            showError("Error building payment request");
        }
    }

    private void onPaymentResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment success!", Toast.LENGTH_SHORT).show();
            storeOrderToFirebase("succeeded", clientSecret);
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("StripeError", message);
    }




    private void storeOrderToFirebase(String status, String transactionId) {
        // Get the payment method from the Intent extra
        String paymentMethod = getIntent().getStringExtra("paymentMethod");
        if (paymentMethod == null) {
            paymentMethod = "Card"; // default
        }

        String orderId = FirebaseDatabase.getInstance().getReference("orders").push().getKey();
        if (orderId == null) {
            Log.e("FirebaseDebug", "Failed to generate order ID");
            return;
        }

        String placedTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                .format(new java.util.Date());

        Log.d("FirebaseDebug", "Creating order with ID: " + orderId);

        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("id", customer != null ? customer.getId() : "null");
        customerInfo.put("name", customer != null ? customer.getName() : "null");
        customerInfo.put("phone", customer != null ? customer.getPhone() : "null");
        customerInfo.put("address", customer != null ? customer.getAddress() : "null");

        Restaurant restaurant = RestaurantHelper.getCurrentRestaurant();
        Map<String, Object> restaurantInfo = new HashMap<>();
        restaurantInfo.put("id", restaurant.getId());
        restaurantInfo.put("name", restaurant.getName());
        restaurantInfo.put("address", restaurant.getAddress());

        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("items", cartItems != null ? cartItems : new ArrayList<>());

        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("total", checkoutTotal);
        paymentInfo.put("status", status);
        paymentInfo.put("transactionId", transactionId);
        paymentInfo.put("method", paymentMethod);

        Map<String, Object> timestamps = new HashMap<>();
        timestamps.put("placed", placedTime);
        timestamps.put("restaurantAccepted", "pending");
        timestamps.put("driverAssigned", "pending");
        timestamps.put("delivered", "pending");

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", placedTime);
        logEntry.put("status", "placed");
        logEntry.put("note", "Order placed by customer.");

        Map<String, Object> disputeInfo = new HashMap<>();
        disputeInfo.put("status", "none");
        disputeInfo.put("reason", "");
        disputeInfo.put("details", "");

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("customer", customerInfo);
        orderData.put("restaurant", restaurantInfo);
        orderData.put("driver", null);
        orderData.put("orderDetails", orderDetails);
        orderData.put("payment", paymentInfo);
        orderData.put("status", "placed");
        orderData.put("timestamps", timestamps);
        orderData.put("updateLogs", Collections.singletonList(logEntry));
        orderData.put("dispute", disputeInfo);
        orderData.put("notes", orderNote != null ? orderNote : "");

        Log.d("OrderDebug", "restaurant: " + (restaurant != null ? restaurant.getName() : "null"));

        // Write main order
        FirebaseDatabase.getInstance().getReference("orders").child(orderId)
                .setValue(orderData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseDebug", "Order added to /orders");
                    } else {
                        Log.e("FirebaseDebug", "Failed to add to /orders", task.getException());
                    }
                });

        // Link to customer
        FirebaseDatabase.getInstance().getReference("ordersByCustomer").child(customer.getId()).child(orderId)
                .setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseDebug", "Order linked to customer");
                    } else {
                        Log.e("FirebaseDebug", "Failed to link order to customer", task.getException());
                    }
                });

        // Link to restaurant
        FirebaseDatabase.getInstance().getReference("ordersByRestaurant").child(restaurant.getId()).child(orderId)
                .setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseDebug", "Order linked to restaurant");
                    } else {
                        Log.e("FirebaseDebug", "Failed to link order to restaurant", task.getException());
                    }
                });

        Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PaymentActivity.this, SuccessActivity.class);
        startActivity(intent);
        finish();
    }
}