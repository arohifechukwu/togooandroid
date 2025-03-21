package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.CheckoutAdapter;
import com.example.togoo.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private TextView subtotalText, gstText, qstText, totalText;
    private Button btnProceedToPayment;

    private List<CartItem> checkoutItemList;
    private CheckoutAdapter checkoutAdapter;

    private static final double GST_RATE = 0.05; // 5% Federal Tax
    private static final double QST_RATE = 0.09975; // 9.975% Quebec Tax


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize UI components
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        subtotalText = findViewById(R.id.subtotalText);
        gstText = findViewById(R.id.gstText);
        qstText = findViewById(R.id.qstText);
        totalText = findViewById(R.id.totalText);
        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);

        // Retrieve items from FoodAdapter (Buy Now) or CartActivity
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

        // Initial Calculation
        calculateTotal();

        // Proceed to Payment
        btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
    }

    // 🔹 Calculate Total with Taxes
    private void calculateTotal() {
        double subtotal = 0.0;
        for (CartItem item : checkoutItemList) {
            subtotal += item.getFoodPrice() * item.getQuantity();
        }

        double gst = subtotal * GST_RATE;
        double qst = subtotal * QST_RATE;
        double total = subtotal + gst + qst;

        // Update UI
        subtotalText.setText(String.format("$%.2f", subtotal));
        gstText.setText(String.format("$%.2f", gst));
        qstText.setText(String.format("$%.2f", qst));
        totalText.setText(String.format("$%.2f", total));
    }

    // 🔹 Proceed to Payment
    private void proceedToPayment() {
        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
        intent.putParcelableArrayListExtra("checkoutItems", new ArrayList<>(checkoutItemList));
        startActivity(intent);
    }
}