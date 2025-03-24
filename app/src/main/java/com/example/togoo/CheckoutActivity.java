package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.CheckoutAdapter;
import com.example.togoo.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private TextView subtotalText, gstText, qstText, totalText;
    private Button btnProceedToPayment, btnCancelOrder;

    private List<CartItem> checkoutItemList;
    private CheckoutAdapter checkoutAdapter;

    private static final double GST_RATE = 0.05; // 5% Federal Tax
    private static final double QST_RATE = 0.09975; // 9.975% Quebec Tax

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize Toolbar and set it as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        // Initialize UI components
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        subtotalText = findViewById(R.id.subtotalText);
        gstText = findViewById(R.id.gstText);
        qstText = findViewById(R.id.qstText);
        totalText = findViewById(R.id.totalText);
        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        // Retrieve items passed from previous activity
        checkoutItemList = getIntent().getParcelableArrayListExtra("cartItems");
        if (checkoutItemList == null || checkoutItemList.isEmpty()) {
            Toast.makeText(this, "No items to checkout!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up RecyclerView
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutAdapter = new CheckoutAdapter(checkoutItemList, this, this::calculateTotal);
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        // Calculate totals initially
        calculateTotal();

        // Set up Proceed to Payment button click listener
        btnProceedToPayment.setOnClickListener(v -> proceedToPayment());

        // Set up Cancel Order button click listener: navigate back to CustomerLandingActivity
        btnCancelOrder.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, CustomerLandingActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Handle back navigation from the Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Calculate total price with taxes
    private void calculateTotal() {
        double subtotal = 0.0;
        for (CartItem item : checkoutItemList) {
            subtotal += item.getFoodPrice() * item.getQuantity();
        }

        double gst = subtotal * GST_RATE;
        double qst = subtotal * QST_RATE;
        double total = subtotal + gst + qst;

        subtotalText.setText(String.format("$%.2f", subtotal));
        gstText.setText(String.format("$%.2f", gst));
        qstText.setText(String.format("$%.2f", qst));
        totalText.setText(String.format("$%.2f", total));
    }

    // Proceed to PaymentActivity with current checkout items
    private void proceedToPayment() {
        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
        intent.putParcelableArrayListExtra("checkoutItems", new ArrayList<>(checkoutItemList));
        startActivity(intent);
    }
}