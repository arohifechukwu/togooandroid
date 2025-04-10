package com.example.togoo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.togoo.R;
import java.util.List;
import java.util.Map;

public class DriverNotificationsAdapter extends RecyclerView.Adapter<DriverNotificationsAdapter.ViewHolder> {

    private List<Map<String, Object>> notifications;

    public DriverNotificationsAdapter(List<Map<String, Object>> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> notification = notifications.get(position);
        // Assume each notification contains an "orderId" and "status".
        String orderId = (String) notification.get("orderId");
        String status = (String) notification.get("status");
        String displayText = "Order ID: " + orderId + "\nStatus: " + status;
        holder.notificationText.setText(displayText);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notificationText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationText = itemView.findViewById(R.id.notificationText);
        }
    }
}