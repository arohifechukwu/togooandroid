package com.example.togoo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.togoo.R;
import com.example.togoo.models.CartItem;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {

    private List<CartItem> checkoutItems;
    private Context context;
    private final OnQuantityChangeListener quantityChangeListener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public CheckoutAdapter(List<CartItem> checkoutItems, Context context, OnQuantityChangeListener listener) {
        this.checkoutItems = checkoutItems;
        this.context = context;
        this.quantityChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = checkoutItems.get(position);

        holder.foodName.setText(item.getFoodDescription()); // 🔹 Use correct method
        holder.foodPrice.setText(String.format("$%.2f", item.getFoodPrice()));
        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getFoodImage())
                .placeholder(R.drawable.ic_food_placeholder)
                .into(holder.foodImage);

        // Increase Quantity
        holder.btnIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1); // 🔹 Use setQuantity()
            holder.quantityText.setText(String.valueOf(item.getQuantity()));
            quantityChangeListener.onQuantityChanged();
        });

        // Decrease Quantity
        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1); // 🔹 Use setQuantity()
                holder.quantityText.setText(String.valueOf(item.getQuantity()));
                quantityChangeListener.onQuantityChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkoutItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName, foodPrice, quantityText;
        Button btnIncrease, btnDecrease;

        public ViewHolder(View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            quantityText = itemView.findViewById(R.id.quantityText);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }
}