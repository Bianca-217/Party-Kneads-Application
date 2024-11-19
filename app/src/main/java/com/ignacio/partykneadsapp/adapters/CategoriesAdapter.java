package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.CategoriesModel;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private Context context;
    private List<CategoriesModel> list;
    private CategoriesModel selectedCategory;
    private OnCategoryClickListener onCategoryClickListener;

    // Define an interface for category selection callback
    public interface OnCategoryClickListener {
        void onCategoryClick(String category);  // Pass the selected category name
    }

    // Constructor for CategoriesAdapter
    public CategoriesAdapter(Context context, List<CategoriesModel> list, OnCategoryClickListener onCategoryClickListener) {
        this.context = context;
        this.list = list;
        this.selectedCategory = list.get(0); // Set default selected category to the first category
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the category item layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.categories, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the category at the current position
        CategoriesModel category = list.get(position);

        // Set category name and image
        holder.icon.setImageResource(category.getImage());
        holder.name.setText(category.getName());

        // Check if the current category is the selected one
        Context context = holder.itemView.getContext();
        if (category.equals(selectedCategory)) {
            // Highlight the selected category
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pink));
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.semiwhite));
        } else {
            // Default styling for other categories
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.footerpink));
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.semiblack));
        }

        // Handle category click event
        holder.itemView.setOnClickListener(v -> {
            // Update selected category
            selectedCategory = category;
            notifyDataSetChanged();  // Update the UI to reflect the selected category

            // Trigger callback with the selected category name
            if (onCategoryClickListener != null) {
                onCategoryClickListener.onCategoryClick(category.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the total number of categories
        return list.size();
    }

    // ViewHolder class to hold category item views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.catIcon);
            name = itemView.findViewById(R.id.caText);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
