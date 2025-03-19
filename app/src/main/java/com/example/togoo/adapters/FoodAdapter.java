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
import com.example.togoo.models.FoodItem;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private Context context;
    private List<FoodItem> foodList;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem foodItem);
    }

    public FoodAdapter(Context context, List<FoodItem> foodList, OnFoodClickListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem food = foodList.get(position);

        // Correcting Name Retrieval (Now Uses UID Instead of Description)
        holder.foodName.setText(food.getId()); // Fetches UID as Name (e.g., "Apple Pie")
        holder.foodDescription.setText(food.getDescription()); // Correctly Displays Description
        holder.foodPrice.setText("$" + food.getPrice());

        // Load Image with Glide
        Glide.with(context)
                .load(food.getImageUrl() != null ? food.getImageUrl() : R.drawable.ic_food_placeholder)
                .into(holder.foodImage);

        holder.itemView.setOnClickListener(v -> listener.onFoodClick(food));
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName, foodDescription, foodPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription); // Added Description
            foodPrice = itemView.findViewById(R.id.foodPrice);
        }
    }
}