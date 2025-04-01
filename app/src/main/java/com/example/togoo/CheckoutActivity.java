//package com.example.togoo;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.togoo.adapters.CheckoutAdapter;
//import com.example.togoo.models.CartItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CheckoutActivity extends AppCompatActivity {
//
//    private RecyclerView checkoutRecyclerView;
//    private TextView subtotalText, gstText, qstText, totalText;
//    private Button btnProceedToPayment, btnCancelOrder;
//
//    private List<CartItem> checkoutItemList;
//    private CheckoutAdapter checkoutAdapter;
//
//    private static final double GST_RATE = 0.05; // 5% Federal Tax
//    private static final double QST_RATE = 0.09975; // 9.975% Quebec Tax
//
//    // Field to store the calculated total amount (in dollars)
//    private double totalAmount = 0.0;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_checkout);
//
//        // Initialize Toolbar and set it as the action bar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
//        }
//
//        // Initialize UI components
//        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
//        subtotalText = findViewById(R.id.subtotalText);
//        gstText = findViewById(R.id.gstText);
//        qstText = findViewById(R.id.qstText);
//        totalText = findViewById(R.id.totalText);
//        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);
//        btnCancelOrder = findViewById(R.id.btnCancelOrder);
//
//        // Retrieve items passed from previous activity
//        checkoutItemList = getIntent().getParcelableArrayListExtra("cartItems");
//        if (checkoutItemList == null || checkoutItemList.isEmpty()) {
//            Toast.makeText(this, "No items to checkout!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        // Set up RecyclerView
//        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        checkoutAdapter = new CheckoutAdapter(checkoutItemList, this, this::calculateTotal);
//        checkoutRecyclerView.setAdapter(checkoutAdapter);
//
//        // Calculate totals initially
//        calculateTotal();
//
//        // Set up Proceed to Payment button click listener
//        btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
//
//        // Set up Cancel Order button click listener: navigate back to CustomerLandingActivity
//        btnCancelOrder.setOnClickListener(v -> {
//            Intent intent = new Intent(CheckoutActivity.this, CustomerLandingActivity.class);
//            startActivity(intent);
//            finish();
//        });
//    }
//
//    // Handle back navigation from the Toolbar
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    // Calculate total price with taxes
//    private void calculateTotal() {
//        double subtotal = 0.0;
//        for (CartItem item : checkoutItemList) {
//            subtotal += item.getFoodPrice() * item.getQuantity();
//        }
//
//        double gst = subtotal * GST_RATE;
//        double qst = subtotal * QST_RATE;
//        double total = subtotal + gst + qst;
//
//        // Store the calculated total in a member variable for later use
//        totalAmount = total;
//
//        subtotalText.setText(String.format("$%.2f", subtotal));
//        gstText.setText(String.format("$%.2f", gst));
//        qstText.setText(String.format("$%.2f", qst));
//        totalText.setText(String.format("$%.2f", total));
//    }
//
//    // Proceed to PaymentActivity with current checkout items and the calculated total
//    private void proceedToPayment() {
//        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
//        intent.putParcelableArrayListExtra("checkoutItems", new ArrayList<>(checkoutItemList));
//        intent.putExtra("checkoutTotal", totalAmount); // Pass total amount (in dollars)
//        startActivity(intent);
//    }
//}




//package com.example.togoo;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.togoo.adapters.CheckoutAdapter;
//import com.example.togoo.models.CartItem;
//import com.example.togoo.models.Customer;
//import com.example.togoo.models.Restaurant;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CheckoutActivity extends AppCompatActivity {
//
//    private RecyclerView checkoutRecyclerView;
//    private TextView subtotalText, gstText, qstText, totalText;
//    private Button btnProceedToPayment, btnCancelOrder;
//
//    private List<CartItem> checkoutItemList;
//    private CheckoutAdapter checkoutAdapter;
//
//    private static final double GST_RATE = 0.05;
//    private static final double QST_RATE = 0.09975;
//    private double totalAmount = 0.0;
//
//    private Customer currentCustomer;
//    private Restaurant selectedRestaurant;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_checkout);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
//        }
//
//        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
//        subtotalText = findViewById(R.id.subtotalText);
//        gstText = findViewById(R.id.gstText);
//        qstText = findViewById(R.id.qstText);
//        totalText = findViewById(R.id.totalText);
//        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);
//        btnCancelOrder = findViewById(R.id.btnCancelOrder);
//
//        checkoutItemList = getIntent().getParcelableArrayListExtra("cartItems");
//        currentCustomer = getIntent().getParcelableExtra("currentCustomer");
//        selectedRestaurant = getIntent().getParcelableExtra("selectedRestaurant");
//
//        if (checkoutItemList == null || checkoutItemList.isEmpty()) {
//            Toast.makeText(this, "No items to checkout!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        checkoutAdapter = new CheckoutAdapter(checkoutItemList, this, this::calculateTotal);
//        checkoutRecyclerView.setAdapter(checkoutAdapter);
//
//        calculateTotal();
//
//        btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
//
//        btnCancelOrder.setOnClickListener(v -> {
//            Intent intent = new Intent(CheckoutActivity.this, CustomerLandingActivity.class);
//            startActivity(intent);
//            finish();
//        });
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void calculateTotal() {
//        double subtotal = 0.0;
//        for (CartItem item : checkoutItemList) {
//            subtotal += item.getFoodPrice() * item.getQuantity();
//        }
//
//        double gst = subtotal * GST_RATE;
//        double qst = subtotal * QST_RATE;
//        double total = subtotal + gst + qst;
//
//        totalAmount = total;
//
//        subtotalText.setText(String.format("$%.2f", subtotal));
//        gstText.setText(String.format("$%.2f", gst));
//        qstText.setText(String.format("$%.2f", qst));
//        totalText.setText(String.format("$%.2f", total));
//    }
//
//    private void proceedToPayment() {
//        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
//        intent.putParcelableArrayListExtra("checkoutItems", new ArrayList<>(checkoutItemList));
//        intent.putExtra("checkoutTotal", totalAmount);
//        intent.putExtra("currentCustomer", currentCustomer);
//        intent.putExtra("selectedRestaurant", selectedRestaurant);
//        startActivity(intent);
//    }
//}



//
//package com.example.togoo;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.togoo.adapters.CheckoutAdapter;
//import com.example.togoo.models.CartItem;
//import com.example.togoo.models.Customer;
//import com.example.togoo.models.Restaurant;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CheckoutActivity extends AppCompatActivity {
//
//    private RecyclerView checkoutRecyclerView;
//    private TextView subtotalText, gstText, qstText, totalText;
//    private EditText orderNoteInput;
//    private Button btnProceedToPayment, btnCancelOrder;
//
//    private List<CartItem> checkoutItemList;
//    private CheckoutAdapter checkoutAdapter;
//
//    private static final double GST_RATE = 0.05;
//    private static final double QST_RATE = 0.09975;
//    private double totalAmount = 0.0;
//
//    private Customer currentCustomer;
//    private Restaurant selectedRestaurant;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_checkout);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
//        }
//
//        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
//        subtotalText = findViewById(R.id.subtotalText);
//        gstText = findViewById(R.id.gstText);
//        qstText = findViewById(R.id.qstText);
//        totalText = findViewById(R.id.totalText);
//        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);
//        btnCancelOrder = findViewById(R.id.btnCancelOrder);
//        orderNoteInput = findViewById(R.id.orderNoteInput);
//
//        checkoutItemList = getIntent().getParcelableArrayListExtra("cartItems");
//        currentCustomer = getIntent().getParcelableExtra("currentCustomer");
//        selectedRestaurant = getIntent().getParcelableExtra("selectedRestaurant");
//
//        if (checkoutItemList == null || checkoutItemList.isEmpty()) {
//            Toast.makeText(this, "No items to checkout!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        checkoutAdapter = new CheckoutAdapter(checkoutItemList, this, this::calculateTotal);
//        checkoutRecyclerView.setAdapter(checkoutAdapter);
//
//        calculateTotal();
//
//        btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
//
//        btnCancelOrder.setOnClickListener(v -> {
//            Intent intent = new Intent(CheckoutActivity.this, CustomerLandingActivity.class);
//            startActivity(intent);
//            finish();
//        });
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void calculateTotal() {
//        double subtotal = 0.0;
//        for (CartItem item : checkoutItemList) {
//            subtotal += item.getFoodPrice() * item.getQuantity();
//        }
//
//        double gst = subtotal * GST_RATE;
//        double qst = subtotal * QST_RATE;
//        double total = subtotal + gst + qst;
//
//        totalAmount = total;
//
//        subtotalText.setText(String.format("$%.2f", subtotal));
//        gstText.setText(String.format("$%.2f", gst));
//        qstText.setText(String.format("$%.2f", qst));
//        totalText.setText(String.format("$%.2f", total));
//    }
//
//    private void proceedToPayment() {
//        String orderNote = orderNoteInput.getText().toString().trim();
//
//        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
//        intent.putParcelableArrayListExtra("checkoutItems", new ArrayList<>(checkoutItemList));
//        intent.putExtra("checkoutTotal", totalAmount);
//        intent.putExtra("currentCustomer", currentCustomer);
//        intent.putExtra("selectedRestaurant", selectedRestaurant);
//        intent.putExtra("orderNote", orderNote);
//        startActivity(intent);
//    }
//}



//package com.example.togoo;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.MenuItem;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.togoo.adapters.CheckoutAdapter;
//import com.example.togoo.models.CartItem;
//import com.example.togoo.models.Customer;
//import com.example.togoo.models.Restaurant;
//import com.example.togoo.models.User;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CheckoutActivity extends AppCompatActivity {
//
//    private RecyclerView checkoutRecyclerView;
//    private TextView subtotalText, gstText, qstText, totalText;
//    private EditText orderNoteInput;
//    private Button btnProceedToPayment, btnCancelOrder;
//
//    private List<CartItem> checkoutItemList;
//    private CheckoutAdapter checkoutAdapter;
//
//    private static final double GST_RATE = 0.05;
//    private static final double QST_RATE = 0.09975;
//    private double totalAmount = 0.0;
//
//    private Customer currentCustomer;
//    private Restaurant selectedRestaurant;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_checkout);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
//        }
//
//        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
//        subtotalText = findViewById(R.id.subtotalText);
//        gstText = findViewById(R.id.gstText);
//        qstText = findViewById(R.id.qstText);
//        totalText = findViewById(R.id.totalText);
//        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);
//        btnCancelOrder = findViewById(R.id.btnCancelOrder);
//        orderNoteInput = findViewById(R.id.orderNoteInput);
//
//        checkoutItemList = getIntent().getParcelableArrayListExtra("cartItems");
//        selectedRestaurant = getIntent().getParcelableExtra("selectedRestaurant");
//
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer").child(uid);
//
//        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User user = snapshot.getValue(User.class);
//
//                if (user != null) {
//                    currentCustomer = new Customer(
//                            uid,  // ✅ safely assign UID as ID
//                            user.getName(),
//                            user.getPhone(),
//                            user.getAddress()
//                    );
//
//                    Log.d("CheckoutDebug", "✅ Fetched and converted user to Customer: " + currentCustomer.getId());
//
//                    if (checkoutItemList == null || checkoutItemList.isEmpty()) {
//                        Toast.makeText(CheckoutActivity.this, "No items to checkout!", Toast.LENGTH_SHORT).show();
//                        finish();
//                        return;
//                    }
//
//                    checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(CheckoutActivity.this));
//                    checkoutAdapter = new CheckoutAdapter(checkoutItemList, CheckoutActivity.this, CheckoutActivity.this::calculateTotal);
//                    checkoutRecyclerView.setAdapter(checkoutAdapter);
//
//                    calculateTotal();
//
//                    btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
//
//                } else {
//                    Log.e("CheckoutDebug", "❌ User not found in customer node for UID: " + uid);
//                    Toast.makeText(CheckoutActivity.this, "Customer info missing.", Toast.LENGTH_LONG).show();
//                    finish();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("CheckoutDebug", "❌ Firebase error: " + error.getMessage());
//                Toast.makeText(CheckoutActivity.this, "Database error. Please try again.", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        });
//
//        btnCancelOrder.setOnClickListener(v -> {
//            Intent intent = new Intent(CheckoutActivity.this, CustomerLandingActivity.class);
//            startActivity(intent);
//            finish();
//        });    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void calculateTotal() {
//        double subtotal = 0.0;
//        for (CartItem item : checkoutItemList) {
//            subtotal += item.getFoodPrice() * item.getQuantity();
//        }
//
//        double gst = subtotal * GST_RATE;
//        double qst = subtotal * QST_RATE;
//        double total = subtotal + gst + qst;
//
//        totalAmount = total;
//
//        subtotalText.setText(String.format("$%.2f", subtotal));
//        gstText.setText(String.format("$%.2f", gst));
//        qstText.setText(String.format("$%.2f", qst));
//        totalText.setText(String.format("$%.2f", total));
//    }
//
//    private void proceedToPayment() {
//        String orderNote = orderNoteInput.getText().toString().trim();
//
//        Log.d("CheckoutDebug", "currentCustomer: " + currentCustomer);
//        Log.d("CheckoutDebug", "Sending restaurant: " + selectedRestaurant);
//
//        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
//        intent.putParcelableArrayListExtra("checkoutItems", new ArrayList<>(checkoutItemList));
//        intent.putExtra("checkoutTotal", totalAmount);
//        intent.putExtra("currentCustomer", currentCustomer);
//        intent.putExtra("selectedRestaurant", selectedRestaurant);
//        intent.putExtra("orderNote", orderNote);
//        startActivity(intent);
//    }
//}



package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.CheckoutAdapter;
import com.example.togoo.utils.RestaurantHelper;
import com.example.togoo.models.CartItem;
import com.example.togoo.models.Customer;
import com.example.togoo.models.Restaurant;
import com.example.togoo.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private TextView subtotalText, gstText, qstText, totalText;
    private EditText orderNoteInput;
    private Button btnProceedToPayment, btnCancelOrder;

    private List<CartItem> checkoutItemList;
    private CheckoutAdapter checkoutAdapter;

    private static final double GST_RATE = 0.05;
    private static final double QST_RATE = 0.09975;
    private double totalAmount = 0.0;

    private Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        subtotalText = findViewById(R.id.subtotalText);
        gstText = findViewById(R.id.gstText);
        qstText = findViewById(R.id.qstText);
        totalText = findViewById(R.id.totalText);
        btnProceedToPayment = findViewById(R.id.btnProceedToPayment);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        orderNoteInput = findViewById(R.id.orderNoteInput);

        checkoutItemList = getIntent().getParcelableArrayListExtra("cartItems");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customer").child(uid);

        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user != null) {
                    currentCustomer = new Customer(
                            uid,
                            user.getName(),
                            user.getPhone(),
                            user.getAddress()
                    );

                    Log.d("CheckoutDebug", "✅ Fetched and converted user to Customer: " + currentCustomer.getId());

                    if (checkoutItemList == null || checkoutItemList.isEmpty()) {
                        Toast.makeText(CheckoutActivity.this, "No items to checkout!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(CheckoutActivity.this));
                    checkoutAdapter = new CheckoutAdapter(checkoutItemList, CheckoutActivity.this, CheckoutActivity.this::calculateTotal);
                    checkoutRecyclerView.setAdapter(checkoutAdapter);

                    calculateTotal();

                    btnProceedToPayment.setOnClickListener(v -> proceedToPayment());

                } else {
                    Log.e("CheckoutDebug", "❌ User not found in customer node for UID: " + uid);
                    Toast.makeText(CheckoutActivity.this, "Customer info missing.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CheckoutDebug", "❌ Firebase error: " + error.getMessage());
                Toast.makeText(CheckoutActivity.this, "Database error. Please try again.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        btnCancelOrder.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, CustomerLandingActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateTotal() {
        double subtotal = 0.0;
        for (CartItem item : checkoutItemList) {
            subtotal += item.getFoodPrice() * item.getQuantity();
        }

        double gst = subtotal * GST_RATE;
        double qst = subtotal * QST_RATE;
        double total = subtotal + gst + qst;

        totalAmount = total;

        subtotalText.setText(String.format("$%.2f", subtotal));
        gstText.setText(String.format("$%.2f", gst));
        qstText.setText(String.format("$%.2f", qst));
        totalText.setText(String.format("$%.2f", total));
    }

    private void proceedToPayment() {
        String orderNote = orderNoteInput.getText().toString().trim();

        Log.d("CheckoutDebug", "currentCustomer: " + currentCustomer);
        Log.d("CheckoutDebug", "Sending restaurant: " + RestaurantHelper.getCurrentRestaurant());

        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
        intent.putParcelableArrayListExtra("checkoutItems", new ArrayList<>(checkoutItemList));
        intent.putExtra("checkoutTotal", totalAmount);
        intent.putExtra("currentCustomer", currentCustomer);
        intent.putExtra("selectedRestaurant", RestaurantHelper.getCurrentRestaurant());
        intent.putExtra("orderNote", orderNote);
        startActivity(intent);
    }
}