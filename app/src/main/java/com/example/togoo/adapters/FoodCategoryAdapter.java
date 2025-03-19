////package com.example.togoo.adapters;
////
////import android.content.Context;
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////import android.widget.ImageView;
////import android.widget.TextView;
////
////import androidx.annotation.NonNull;
////import androidx.recyclerview.widget.RecyclerView;
////
////import com.bumptech.glide.Glide;
////import com.example.togoo.R;
////import com.example.togoo.models.FoodCategory;
////
////import java.util.List;
////
////public class FoodCategoryAdapter extends RecyclerView.Adapter<FoodCategoryAdapter.ViewHolder> {
////
////    private Context context;
////    private List<FoodCategory> categoryList;
////    private OnCategoryClickListener listener;
////
////    public interface OnCategoryClickListener {
////        void onCategoryClick(FoodCategory category);
////    }
////
////    public FoodCategoryAdapter(Context context, List<FoodCategory> categoryList, OnCategoryClickListener listener) {
////        this.context = context;
////        this.categoryList = categoryList;
////        this.listener = listener;
////    }
////
////    @NonNull
////    @Override
////    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////        View view = LayoutInflater.from(context).inflate(R.layout.item_food_category, parent, false);
////        return new ViewHolder(view);
////    }
////
////    @Override
////    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
////        FoodCategory category = categoryList.get(position);
////        holder.categoryName.setText(category.getName());
////
////        // Load image using Glide
////        Glide.with(context)
////                .load(category.getImageUrl())
////                .placeholder(R.drawable.ic_food_placeholder) // Default image
////                .into(holder.categoryImage);
////
////        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
////    }
////
////    @Override
////    public int getItemCount() {
////        return categoryList.size();
////    }
////
////    public static class ViewHolder extends RecyclerView.ViewHolder {
////        ImageView categoryImage;
////        TextView categoryName;
////
////        public ViewHolder(View itemView) {
////            super(itemView);
////            categoryImage = itemView.findViewById(R.id.categoryImage);
////            categoryName = itemView.findViewById(R.id.categoryName);
////        }
////    }
////}
//
//
//
//
//
//
//package com.example.togoo.adapters;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.example.togoo.R;
//import com.example.togoo.models.FoodCategory;
//
//import java.util.List;
//
//public class FoodCategoryAdapter extends RecyclerView.Adapter<FoodCategoryAdapter.ViewHolder> {
//
//    private Context context;
//    private List<FoodCategory> categoryList;
//    private OnCategoryClickListener listener;
//
//    public interface OnCategoryClickListener {
//        void onCategoryClick(FoodCategory category);
//    }
//
//    public FoodCategoryAdapter(Context context, List<FoodCategory> categoryList, OnCategoryClickListener listener) {
//        this.context = context;
//        this.categoryList = categoryList;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_food_category, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        FoodCategory category = categoryList.get(position);
//        holder.categoryName.setText(category.getName());
//
//        Glide.with(context)
//                .load(category.getImageUrl() != null ? category.getImageUrl() : R.drawable.ic_food_category_placeholder)
//                .into(holder.categoryImage);
//
//        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
//    }
//
//    @Override
//    public int getItemCount() {
//        return categoryList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView categoryImage;
//        TextView categoryName;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            categoryImage = itemView.findViewById(R.id.categoryImage);
//            categoryName = itemView.findViewById(R.id.categoryName);
//        }
//    }
//}





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
import com.example.togoo.models.FoodCategory;

import java.util.List;

public class FoodCategoryAdapter extends RecyclerView.Adapter<FoodCategoryAdapter.ViewHolder> {

    private Context context;
    private List<FoodCategory> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(FoodCategory category);
    }

    public FoodCategoryAdapter(Context context, List<FoodCategory> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodCategory category = categoryList.get(position);
        holder.categoryName.setText(category.getName());

        if (category.hasImageUrl()) {
            // Load image from URL
            Glide.with(context)
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.ic_food_category_placeholder)
                    .into(holder.categoryImage);
        } else {
            // Load local drawable
            holder.categoryImage.setImageResource(category.getImageResId());
        }

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}