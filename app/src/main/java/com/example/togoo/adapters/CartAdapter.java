package com.example.togoo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.togoo.R;
import com.example.togoo.models.CartItem;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private DatabaseReference cartRef;

    public CartAdapter(List<CartItem> cartItems, Context context, DatabaseReference cartRef) {
        this.cartItems = cartItems;
        this.context = context;
        this.cartRef = cartRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.foodName.setText(item.getFoodDescription());
        holder.foodPrice.setText("$" + item.getFoodPrice());

        Glide.with(context)
                .load(item.getFoodImage())
                .placeholder(R.drawable.ic_food_placeholder)
                .into(holder.foodImage);

        // Delete item correctly using its unique push key
        holder.btnDelete.setOnClickListener(v -> {
            String key = item.getCartItemId();
            if (key != null) {
                cartRef.child(key).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Item removed successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to remove item!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Item key not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName, foodPrice;
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}