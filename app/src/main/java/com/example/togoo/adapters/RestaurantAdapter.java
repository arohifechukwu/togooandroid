package com.example.togoo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.togoo.R;
import com.example.togoo.models.Restaurant;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private Context context;
    private List<Restaurant> restaurantList;
    private OnRestaurantClickListener listener;

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList, OnRestaurantClickListener listener) {
        this.context = context;
        this.restaurantList = restaurantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.restaurantName.setText(restaurant.getName());
        holder.restaurantLocation.setText(restaurant.getAddress());

        // Set image
        Glide.with(context)
                .load(restaurant.getImageURL())
                .placeholder(R.drawable.ic_restaurant_placeholder)
                .into(holder.restaurantImage);

        // Set rating if available
        if (holder.restaurantRating != null) {
            holder.restaurantRating.setText("\u2B50 " + restaurant.getRating());
        }

        // Set distance and ETA (if passed separately from adapter or can be computed)
        if (holder.restaurantDistance != null && restaurant.getDistanceKm() > 0) {
            holder.restaurantDistance.setText(String.format("%.1f km", restaurant.getDistanceKm()));
        }
        if (holder.restaurantETA != null && restaurant.getEtaMinutes() > 0) {
            holder.restaurantETA.setText(restaurant.getEtaMinutes() + " mins");
        }

        holder.itemView.setOnClickListener(v -> listener.onRestaurantClick(restaurant));
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView restaurantImage;
        TextView restaurantName, restaurantLocation, restaurantRating, restaurantDistance, restaurantETA;

        public ViewHolder(View itemView) {
            super(itemView);
            restaurantImage = itemView.findViewById(R.id.restaurantImage);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantLocation = itemView.findViewById(R.id.restaurantLocation);
            restaurantRating = itemView.findViewById(R.id.restaurantRating);
            restaurantDistance = itemView.findViewById(R.id.restaurantDistance);
            restaurantETA = itemView.findViewById(R.id.restaurantETA);
        }
    }
}
