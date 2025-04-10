package com.example.togoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.togoo.adapters.AdminUserAdapter;
import com.example.togoo.models.User;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ApprovalActivity extends AppCompatActivity {

    private RecyclerView approvalsRecyclerView;
    private TextView noApprovalsText;
    private AdminUserAdapter adminUserAdapter;
    private List<User> pendingUsers;
    private DatabaseReference dbReference;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);

        dbReference = FirebaseDatabase.getInstance().getReference();

        approvalsRecyclerView = findViewById(R.id.approvalsRecyclerView);
        noApprovalsText = findViewById(R.id.noApprovalsText);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        approvalsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingUsers = new ArrayList<>();
        adminUserAdapter = new AdminUserAdapter(pendingUsers, this, true); // true for approval mode
        approvalsRecyclerView.setAdapter(adminUserAdapter);

        fetchPendingApprovals();

        // ✅ Setup bottom navigation listener
        bottomNavigationView.setSelectedItemId(R.id.navigation_approvals);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void fetchPendingApprovals() {
        pendingUsers.clear();
        checkPendingUsers("driver");
        checkPendingUsers("restaurant");
    }

    private void checkPendingUsers(String node) {
        dbReference.child(node).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && "pending".equals(userSnapshot.child("status").getValue(String.class))) {
                        user.setUserId(userSnapshot.getKey());
                        pendingUsers.add(user);
                    }
                }
                adminUserAdapter.notifyDataSetChanged();
                updateNoApprovalsVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ApprovalActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNoApprovalsVisibility() {
        if (pendingUsers.isEmpty()) {
            noApprovalsText.setVisibility(View.VISIBLE);
            approvalsRecyclerView.setVisibility(View.GONE);
        } else {
            noApprovalsText.setVisibility(View.GONE);
            approvalsRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * ✅ Handle Bottom Navigation Selection Properly
     */
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navigation_home) {
            startActivity(new Intent(this, AdminLandingActivity.class));
            finish(); // ✅ Close current activity
            return true;
        } else if (id == R.id.navigation_users) {
            startActivity(new Intent(this, UserActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_approvals) {
            return true; // ✅ Stay on this page
        } else if (id == R.id.navigation_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_transaction) {
            startActivity(new Intent(this, TransactionActivity.class));
            finish();
            return true;
        }
        return false;
    }
}