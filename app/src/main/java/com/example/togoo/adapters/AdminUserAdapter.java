package com.example.togoo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.togoo.R;
import com.example.togoo.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;
    private boolean isApprovalMode;
    private DatabaseReference dbReference;

    public AdminUserAdapter(List<User> userList, Context context, boolean isApprovalMode) {
        this.userList = userList;
        this.context = context;
        this.isApprovalMode = isApprovalMode;
        this.dbReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText(user.getRole());
        holder.userStatus.setText("Status: " + user.getStatus());

        if (isApprovalMode) {
            holder.approveButton.setVisibility(View.VISIBLE);
            holder.declineButton.setVisibility(View.VISIBLE);

            holder.approveButton.setOnClickListener(v -> approveUser(user));
            holder.declineButton.setOnClickListener(v -> declineUser(user));
        } else {
            holder.approveButton.setVisibility(View.GONE);
            holder.declineButton.setVisibility(View.GONE);
        }
    }

    private void approveUser(User user) {
        dbReference.child(user.getRole()).child(user.getUserId()).child("status").setValue("approved");
        userList.remove(user);
        notifyDataSetChanged();
    }

    private void declineUser(User user) {
        dbReference.child(user.getRole()).child(user.getUserId()).removeValue();
        userList.remove(user);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole, userStatus;
        Button approveButton, declineButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userRole = itemView.findViewById(R.id.userRole);
            userStatus = itemView.findViewById(R.id.userStatus);
            approveButton = itemView.findViewById(R.id.approveButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}