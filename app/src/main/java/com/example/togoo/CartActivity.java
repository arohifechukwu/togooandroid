package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.adapters.CartAdapter;
import com.example.togoo.utils.RestaurantHelper;
import com.example.togoo.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private Button btnBuyNow;
    private DatabaseReference cartRef;
    private FirebaseUser currentUser;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize Toolbar and set it as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Set your custom back icon (ensure ic_back exists in your drawable folder)
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }

        // Initialize UI components
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        // Get current user and reference the "cart" node under the user's UID
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cartRef = FirebaseDatabase.getInstance().getReference("cart").child(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItemList, this, cartRef);
        cartRecyclerView.setAdapter(cartAdapter);

        // Load cart items
        loadCartItems();

        // Buy Now Functionality
        btnBuyNow.setOnClickListener(v -> proceedToCheckout());
    }

    // Override the back navigation from the Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Navigate back when back icon is pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Fetch cart items from Firebase and update RecyclerView in real time
    private void loadCartItems() {
        cartRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                CartItem item = snapshot.getValue(CartItem.class);
                if (item != null) {
                    item.setCartItemId(snapshot.getKey()); // Store unique key for deletion
                    cartItemList.add(item);
                    cartAdapter.notifyItemInserted(cartItemList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                CartItem updatedItem = snapshot.getValue(CartItem.class);
                if (updatedItem != null) {
                    updatedItem.setCartItemId(snapshot.getKey());
                    for (int i = 0; i < cartItemList.size(); i++) {
                        if (cartItemList.get(i).getCartItemId().equals(snapshot.getKey())) {
                            cartItemList.set(i, updatedItem);
                            cartAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedKey = snapshot.getKey();
                for (int i = 0; i < cartItemList.size(); i++) {
                    if (cartItemList.get(i).getCartItemId().equals(removedKey)) {
                        cartItemList.remove(i);
                        cartAdapter.notifyItemRemoved(i);
                        cartAdapter.notifyItemRangeChanged(i, cartItemList.size());
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not needed for this scenario
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load cart items.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Proceed to CheckoutActivity with current cart items
    private void proceedToCheckout() {
        if (cartItemList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<CartItem> checkoutItems = new ArrayList<>(cartItemList);
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        intent.putParcelableArrayListExtra("cartItems", checkoutItems);
        intent.putExtra("selectedRestaurant", RestaurantHelper.getCurrentRestaurant());
        startActivity(intent);
    }
}