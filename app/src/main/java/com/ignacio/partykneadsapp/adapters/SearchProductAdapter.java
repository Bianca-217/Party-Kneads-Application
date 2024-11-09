package com.ignacio.partykneadsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.ignacio.partykneadsapp.model.SearchProduct;

import java.util.List;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ProductViewHolder> {

    private List<SearchProduct> productList;  // Use Product as the model class

    // Constructor to pass the list of products
    public SearchProductAdapter(List<SearchProduct> productList) {
        this.productList = productList;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate your custom layout for RecyclerView items
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        // Bind data to the ViewHolder
        SearchProduct product = productList.get(position);
        holder.nameTextView.setText(product.getName());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder to hold references to item views
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;

        public ProductViewHolder(View itemView) {
            super(itemView);
            // Initialize your views here
            nameTextView = itemView.findViewById(android.R.id.text1);
            descriptionTextView = itemView.findViewById(android.R.id.text2);
        }
    }
}
