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

import com.example.togoo.adapters.AdminUserAdapter;
import com.example.togoo.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AdminLandingActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private TextView noRecordsText;
    private AdminUserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference dbReference;
    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigationView;
    private String adminUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_landing);

        auth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        noRecordsText = findViewById(R.id.noRecordsText);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(userList, this, false); // ✅ Include third parameter
        usersRecyclerView.setAdapter(userAdapter);

        validateAdminAccess();

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void validateAdminAccess() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Unauthorized Access!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        adminUID = auth.getCurrentUser().getUid();
        DatabaseReference adminRef = dbReference.child("admin").child(adminUID);

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && "admin".equals(snapshot.child("role").getValue(String.class))) {
                    fetchUsers();
                } else {
                    Toast.makeText(AdminLandingActivity.this, "Access Denied: Not an Admin", Toast.LENGTH_SHORT).show();
                    auth.signOut();
                    startActivity(new Intent(AdminLandingActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminLandingActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUsers() {
        userList.clear();
        checkUsersInNode("customer");
        checkUsersInNode("driver");
        checkUsersInNode("restaurant");
    }

    private void checkUsersInNode(String nodeName) {
        dbReference.child(nodeName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            String status = userSnapshot.child("status").getValue(String.class);
                            if ("approved".equals(status)) { // ✅ Show only approved users
                                user.setUserId(userSnapshot.getKey());
                                userList.add(user);
                            }
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
                updateNoRecordsVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminLandingActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNoRecordsVisibility() {
        if (userList.isEmpty()) {
            noRecordsText.setVisibility(View.VISIBLE);
            usersRecyclerView.setVisibility(View.GONE);
        } else {
            noRecordsText.setVisibility(View.GONE);
            usersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navigation_home) {
            return true;
        } else if (id == R.id.navigation_users) {
            startActivity(new Intent(this, UserActivity.class));
            return true;
        } else if (id == R.id.navigation_approvals) {
            startActivity(new Intent(this, ApprovalActivity.class));
            return true;
        } else if (id == R.id.navigation_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.navigation_transaction) {
            startActivity(new Intent(this, TransactionActivity.class));
            return true;
        }
        return false;
    }
}


