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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;

import com.example.togoo.adapters.UserAdapter;
import com.example.togoo.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private TextView noRecordsText;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference dbReference;
    private FirebaseFirestore firestore;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        dbReference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        noRecordsText = findViewById(R.id.noRecordsText);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        usersRecyclerView.setAdapter(userAdapter);

        fetchUsers();

        // Setup bottom navigation listener
        bottomNavigationView.setSelectedItemId(R.id.navigation_users);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void fetchUsers() {
        userList.clear();
        checkUsersInNode("customer");
        checkUsersInNode("driver");
        checkUsersInNode("restaurant");
        checkUsersInNode("admin");
        fetchUsersFromFirestore();
    }


    // Fetch users from Firestore (if applicable)
    private void fetchUsersFromFirestore() {
        firestore.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
                updateNoRecordsVisibility();
            }
        });
    }

    private void checkUsersInNode(String node) {
        dbReference.child(node).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(userSnapshot.getKey());
                        user.setRole(node);
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
                updateNoRecordsVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(this, AdminLandingActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_users) {
            startActivity(new Intent(this, UserActivity.class));
            finish();
            return true;
        } else if (id == R.id.navigation_approvals) {
            startActivity(new Intent(this, ApprovalActivity.class));
            finish();
            return true;
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