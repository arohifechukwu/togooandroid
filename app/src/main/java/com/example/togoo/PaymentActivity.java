//STRIPE TEST SERVER
//package com.example.togoo;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.stripe.android.PaymentConfiguration;
//import com.stripe.android.paymentsheet.PaymentSheet;
//import com.stripe.android.paymentsheet.PaymentSheetResult;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class PaymentActivity extends AppCompatActivity {
//
//    private Button btnCreditCard, btnPickUp;
//
//    // Replace with your own Stripe keys
//    private String PublishableKey = "pk_test_51NK43EIW21GL6rYr4zBsbEaMAn6Sjt7F5qN4HtGiR8BIbo4IM4XuvsQwPrwakocqBTDohuNw2cgtKCl0BYTx6KtY00wp7NTy0n";
//    private String SecretKey = "sk_test_51NK43EIW21GL6rYr1PwrZqO9LkZKiBRF21U6t3GGXUacCwTTdim0hcy2b4IRow24WUVJ5ypmd2oT47cFUS7ogunx00q8njz1sJ";
//
//    // Stripe API endpoints
//    private String CustomersURL = "https://api.stripe.com/v1/customers";
//    private String EphemeralKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
//    private String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";
//
//    private String CustomerId = null;
//    private String EphemeralKey;
//    private String ClientSecret;
//    private PaymentSheet paymentSheet;
//
//    // Remove the hardcoded "20000"; we'll set this dynamically
//    private String Amount;
//    private String Currency = "usd";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_payment);
//
//        // 1. Retrieve the total passed from CheckoutActivity
//        double checkoutTotal = getIntent().getDoubleExtra("checkoutTotal", 0.0);
//
//        // 2. Convert dollars to cents (Stripe expects amounts in the smallest currency unit)
//        int totalInCents = (int) Math.round(checkoutTotal * 100);
//        // 3. Store it as a string for Stripe parameters
//        Amount = String.valueOf(totalInCents);
//
//        // Initialize Stripe PaymentConfiguration and PaymentSheet
//        PaymentConfiguration.init(this, PublishableKey);
//        paymentSheet = new PaymentSheet(this, this::onPaymentResult);
//
//        // Initialize buttons
//        btnCreditCard = findViewById(R.id.btnCreditCard);
//        btnPickUp = findViewById(R.id.btnPickUp);
//
//        // Set click listener for Credit Card Payment button (Stripe flow)
//        btnCreditCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (CustomerId != null && !CustomerId.isEmpty() &&
//                        ClientSecret != null && !ClientSecret.isEmpty()) {
//                    paymentFlow();
//                } else {
//                    Toast.makeText(PaymentActivity.this,
//                            "Payment not ready. Please try again later.",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        // Set click listener for Pick Up Order button
//        btnPickUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                processPickUp();
//            }
//        });
//
//        // Begin the Stripe customer creation and payment intent setup process
//        createCustomer();
//    }
//
//    // Create a new customer in Stripe
//    private void createCustomer() {
//        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject object = new JSONObject(response);
//                            CustomerId = object.getString("id");
//                            Log.d("Stripe", "Customer created: " + CustomerId);
//                            Toast.makeText(PaymentActivity.this,
//                                    "Customer created",
//                                    Toast.LENGTH_SHORT).show();
//
//                            // After the customer is created, fetch the Ephemeral Key
//                            if (CustomerId != null && !CustomerId.isEmpty()) {
//                                getEphemeralKey();
//                            } else {
//                                Toast.makeText(PaymentActivity.this,
//                                        "Failed to create customer",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Toast.makeText(PaymentActivity.this,
//                                    "Error creating customer: " + e.getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(PaymentActivity.this,
//                        "Error creating customer: " + error.getLocalizedMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + SecretKey);
//                return headers;
//            }
//        };
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(request);
//    }
//
//    // Get an ephemeral key for the customer
//    private void getEphemeralKey() {
//        StringRequest request = new StringRequest(Request.Method.POST, EphemeralKeyURL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject object = new JSONObject(response);
//                            EphemeralKey = object.getString("id");
//                            Log.d("Stripe", "Ephemeral Key: " + EphemeralKey);
//
//                            // After obtaining the ephemeral key, create a payment intent to get the client secret
//                            if (EphemeralKey != null && !EphemeralKey.isEmpty()) {
//                                getClientSecret(CustomerId, EphemeralKey);
//                            } else {
//                                Toast.makeText(PaymentActivity.this,
//                                        "Failed to retrieve ephemeral key",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Toast.makeText(PaymentActivity.this,
//                                    "Error retrieving ephemeral key: " + e.getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(PaymentActivity.this,
//                        "Error retrieving ephemeral key: " + error.getLocalizedMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + SecretKey);
//                headers.put("Stripe-Version", "2022-11-15");
//                return headers;
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("customer", CustomerId);
//                return params;
//            }
//        };
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(request);
//    }
//
//    // Create a payment intent and retrieve the client secret
//    private void getClientSecret(String customerId, String ephemeralKey) {
//        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject object = new JSONObject(response);
//                            ClientSecret = object.getString("client_secret");
//                            Log.d("Stripe", "Client Secret: " + ClientSecret);
//                            Toast.makeText(PaymentActivity.this,
//                                    "Client secret obtained",
//                                    Toast.LENGTH_SHORT).show();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Toast.makeText(PaymentActivity.this,
//                                    "Error retrieving client secret: " + e.getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(PaymentActivity.this,
//                        "Error retrieving client secret: " + error.getLocalizedMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + SecretKey);
//                return headers;
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("customer", customerId);
//                // Use the dynamic amount we obtained from CheckoutActivity
//                params.put("amount", Amount);
//                params.put("currency", Currency);
//                params.put("automatic_payment_methods[enabled]", "true");
//                return params;
//            }
//        };
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(request);
//    }
//
//    // Begin the Stripe PaymentSheet flow
//    private void paymentFlow() {
//        if (ClientSecret != null && !ClientSecret.isEmpty()) {
//            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(
//                    "Stripe Payment",
//                    new PaymentSheet.CustomerConfiguration(CustomerId, EphemeralKey)
//            );
//            paymentSheet.presentWithPaymentIntent(ClientSecret, configuration);
//        } else {
//            Toast.makeText(PaymentActivity.this,
//                    "Client Secret not available",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Placeholder method for processing a pick-up order
//    private void processPickUp() {
//        // Add your pick-up order logic here
//        Toast.makeText(this, "Processing Pick Up Order", Toast.LENGTH_SHORT).show();
//    }
//
//    // Handle the result from PaymentSheet
//    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
//        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
//            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Payment Failed or Canceled", Toast.LENGTH_SHORT).show();
//        }
//    }
//}
//
//
//
//


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
    private static final String BASE_URL = "https://3dc0-45-132-159-141.ngrok-free.app"; // NOT localhost for device

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

        Map<String, Object> restaurantInfo = new HashMap<>();
        restaurantInfo.put("id", restaurant != null ? restaurant.getId() : "null");
        restaurantInfo.put("name", restaurant != null ? restaurant.getName() : "null");
        restaurantInfo.put("address", restaurant != null ? restaurant.getAddress() : "null");

        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("items", cartItems != null ? cartItems : new ArrayList<>());

        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("total", checkoutTotal);
        paymentInfo.put("status", status);
        paymentInfo.put("transactionId", transactionId);

        Map<String, Object> timestamps = new HashMap<>();
        timestamps.put("placed", placedTime);

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
        orderData.put("notes", orderNote != null ? orderNote : ""); // ✅ Correct place for notes

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