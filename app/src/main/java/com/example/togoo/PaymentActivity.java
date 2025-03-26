package com.example.togoo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private Button btnCreditCard, btnPickUp;

    // Replace with your own Stripe keys
    private String PublishableKey = "pk_test_51NK43EIW21GL6rYr4zBsbEaMAn6Sjt7F5qN4HtGiR8BIbo4IM4XuvsQwPrwakocqBTDohuNw2cgtKCl0BYTx6KtY00wp7NTy0n";
    private String SecretKey = "sk_test_51NK43EIW21GL6rYr1PwrZqO9LkZKiBRF21U6t3GGXUacCwTTdim0hcy2b4IRow24WUVJ5ypmd2oT47cFUS7ogunx00q8njz1sJ";

    // Stripe API endpoints
    private String CustomersURL = "https://api.stripe.com/v1/customers";
    private String EphemeralKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId = null;
    private String EphemeralKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;

    // Remove the hardcoded "20000"; we'll set this dynamically
    private String Amount;
    private String Currency = "usd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // 1. Retrieve the total passed from CheckoutActivity
        double checkoutTotal = getIntent().getDoubleExtra("checkoutTotal", 0.0);

        // 2. Convert dollars to cents (Stripe expects amounts in the smallest currency unit)
        int totalInCents = (int) Math.round(checkoutTotal * 100);
        // 3. Store it as a string for Stripe parameters
        Amount = String.valueOf(totalInCents);

        // Initialize Stripe PaymentConfiguration and PaymentSheet
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Initialize buttons
        btnCreditCard = findViewById(R.id.btnCreditCard);
        btnPickUp = findViewById(R.id.btnPickUp);

        // Set click listener for Credit Card Payment button (Stripe flow)
        btnCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CustomerId != null && !CustomerId.isEmpty() &&
                        ClientSecret != null && !ClientSecret.isEmpty()) {
                    paymentFlow();
                } else {
                    Toast.makeText(PaymentActivity.this,
                            "Payment not ready. Please try again later.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for Pick Up Order button
        btnPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPickUp();
            }
        });

        // Begin the Stripe customer creation and payment intent setup process
        createCustomer();
    }

    // Create a new customer in Stripe
    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            CustomerId = object.getString("id");
                            Log.d("Stripe", "Customer created: " + CustomerId);
                            Toast.makeText(PaymentActivity.this,
                                    "Customer created",
                                    Toast.LENGTH_SHORT).show();

                            // After the customer is created, fetch the Ephemeral Key
                            if (CustomerId != null && !CustomerId.isEmpty()) {
                                getEphemeralKey();
                            } else {
                                Toast.makeText(PaymentActivity.this,
                                        "Failed to create customer",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PaymentActivity.this,
                                    "Error creating customer: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PaymentActivity.this,
                        "Error creating customer: " + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Get an ephemeral key for the customer
    private void getEphemeralKey() {
        StringRequest request = new StringRequest(Request.Method.POST, EphemeralKeyURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EphemeralKey = object.getString("id");
                            Log.d("Stripe", "Ephemeral Key: " + EphemeralKey);

                            // After obtaining the ephemeral key, create a payment intent to get the client secret
                            if (EphemeralKey != null && !EphemeralKey.isEmpty()) {
                                getClientSecret(CustomerId, EphemeralKey);
                            } else {
                                Toast.makeText(PaymentActivity.this,
                                        "Failed to retrieve ephemeral key",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PaymentActivity.this,
                                    "Error retrieving ephemeral key: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PaymentActivity.this,
                        "Error retrieving ephemeral key: " + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Stripe-Version", "2022-11-15");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Create a payment intent and retrieve the client secret
    private void getClientSecret(String customerId, String ephemeralKey) {
        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            Log.d("Stripe", "Client Secret: " + ClientSecret);
                            Toast.makeText(PaymentActivity.this,
                                    "Client secret obtained",
                                    Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PaymentActivity.this,
                                    "Error retrieving client secret: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PaymentActivity.this,
                        "Error retrieving client secret: " + error.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                // Use the dynamic amount we obtained from CheckoutActivity
                params.put("amount", Amount);
                params.put("currency", Currency);
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Begin the Stripe PaymentSheet flow
    private void paymentFlow() {
        if (ClientSecret != null && !ClientSecret.isEmpty()) {
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(
                    "Stripe Payment",
                    new PaymentSheet.CustomerConfiguration(CustomerId, EphemeralKey)
            );
            paymentSheet.presentWithPaymentIntent(ClientSecret, configuration);
        } else {
            Toast.makeText(PaymentActivity.this,
                    "Client Secret not available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Placeholder method for processing a pick-up order
    private void processPickUp() {
        // Add your pick-up order logic here
        Toast.makeText(this, "Processing Pick Up Order", Toast.LENGTH_SHORT).show();
    }

    // Handle the result from PaymentSheet
    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Payment Failed or Canceled", Toast.LENGTH_SHORT).show();
        }
    }
}
