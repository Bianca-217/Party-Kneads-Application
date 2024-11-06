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
        void onCategoryClick(String category);
    }

    public CategoriesAdapter(Context context, List<CategoriesModel> list, OnCategoryClickListener onCategoryClickListener) {
        this.context = context;
        this.list = list;
        this.selectedCategory = list.get(0); // Set default selected category if needed
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.categories, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoriesModel category = list.get(position);

        holder.icon.setImageResource(category.getImage());
        holder.name.setText(category.getName());

        Context context = holder.itemView.getContext();
        if (category.equals(selectedCategory)) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pink));
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.semiwhite));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.footerpink));
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.semiblack));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedCategory = category;
            notifyDataSetChanged();
            // Trigger callback with the selected category name
            onCategoryClickListener.onCategoryClick(category.getName());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

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

