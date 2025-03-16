package com.example.togoo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.R;
import com.example.togoo.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;
    private DatabaseReference dbReference;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.dbReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText(user.getRole());
        holder.userStatus.setText("Status: " + user.getStatus());

        String userNode = user.getRole(); // Fetch node dynamically (customer, driver, restaurant)

        // Suspend or Reactivate Logic
        if ("suspended".equals(user.getStatus())) {
            holder.suspendButton.setVisibility(View.GONE);
            holder.reactivateButton.setVisibility(View.VISIBLE);
        } else {
            holder.suspendButton.setVisibility(View.VISIBLE);
            holder.reactivateButton.setVisibility(View.GONE);
        }

        holder.suspendButton.setOnClickListener(v -> updateUserStatus(userNode, user, "suspended"));
        holder.reactivateButton.setOnClickListener(v -> updateUserStatus(userNode, user, "active"));
        holder.deleteButton.setOnClickListener(v -> deleteUser(userNode, user));
    }

    private void updateUserStatus(String userNode, User user, String status) {
        dbReference.child(userNode).child(user.getUserId()).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    user.setStatus(status);
                    notifyDataSetChanged();
                    Toast.makeText(context, "User status updated to " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update user status", Toast.LENGTH_SHORT).show());
    }


    private void deleteUser(String userNode, User user) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(context, "Authentication error: No user logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user.getUserId().equals(currentUser.getUid())) {
            Toast.makeText(context, "You cannot delete your own account!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("admin".equals(user.getRole())) {
            Toast.makeText(context, "Admin accounts cannot be deleted!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Get authentication token before calling the Firebase function
        currentUser.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Toast.makeText(context, "Failed to get authentication token.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String idToken = task.getResult().getToken(); // ✅ Token for authentication

                    FirebaseFunctions functions = FirebaseFunctions.getInstance();

                    // ✅ Call the Firebase Cloud Function with authentication token
                    functions.getHttpsCallable("deleteUser")
                            .call(Collections.singletonMap("userId", user.getUserId()))
                            .addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    // ✅ Delete user from Realtime Database
                                    dbReference.child(userNode).child(user.getUserId()).removeValue()
                                            .addOnSuccessListener(aVoid2 -> {
                                                userList.remove(user);
                                                notifyDataSetChanged();
                                                Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(context, "Failed to delete user from database", Toast.LENGTH_SHORT).show()
                                            );
                                } else {
                                    Exception e = deleteTask.getException();
                                    Toast.makeText(context, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole, userStatus;
        Button suspendButton, reactivateButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userRole = itemView.findViewById(R.id.userRole);
            userStatus = itemView.findViewById(R.id.userStatus);
            suspendButton = itemView.findViewById(R.id.suspendButton);
            reactivateButton = itemView.findViewById(R.id.reactivateButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}