package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.CupcakeModel;
import java.util.List;

public class CupcakeSizeAdapter extends RecyclerView.Adapter<CupcakeSizeAdapter.CupcakeSizeViewHolder> {

    private List<CupcakeModel> cupcakeSizes;
    private OnItemClickListener listener;
    private CupcakeModel selectedCupcakeSize; // Track the selected cupcake size

    // Define an interface for item click
    public interface OnItemClickListener {
        void onItemClick(CupcakeModel cupcakeSize);
    }

    public CupcakeSizeAdapter(List<CupcakeModel> cupcakeSizes, OnItemClickListener listener) {
        this.cupcakeSizes = cupcakeSizes;
        this.listener = listener;
        this.selectedCupcakeSize = cupcakeSizes.get(0); // Default to the first item (Single)
    }

    @NonNull
    @Override
    public CupcakeSizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cupcake_sizes, parent, false); // Use your XML layout file name
        return new CupcakeSizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CupcakeSizeViewHolder holder, int position) {
        CupcakeModel cupcakeSize = cupcakeSizes.get(position);
        holder.cupcakeSizesTextView.setText(cupcakeSize.getSize() + ": " + cupcakeSize.getPieces());

        // Set background and text color based on selection
        Context context = holder.itemView.getContext();
        CardView cardView = holder.itemView.findViewById(R.id.cupcakeCardView);

        if (cupcakeSize.equals(selectedCupcakeSize)) {
            // Set background color for selected item
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pink));
            holder.cupcakeSizesTextView.setTextColor(ContextCompat.getColor(context, R.color.semiwhite));
        } else {
            // Set background color for unselected items
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.footerpink));
            holder.cupcakeSizesTextView.setTextColor(ContextCompat.getColor(context, R.color.semiblack));
        }

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            selectedCupcakeSize = cupcakeSize; // Update selected size
            notifyDataSetChanged(); // Notify adapter to refresh the views
            listener.onItemClick(cupcakeSize); // Trigger the item click listener
        });
    }

    @Override
    public int getItemCount() {
        return cupcakeSizes.size();
    }

    static class CupcakeSizeViewHolder extends RecyclerView.ViewHolder {
        TextView cupcakeSizesTextView;

        CupcakeSizeViewHolder(@NonNull View itemView) {
            super(itemView);
            cupcakeSizesTextView = itemView.findViewById(R.id.cupcakeSizes);
        }
    }

    // Method to get the currently selected cupcake size
    public CupcakeModel getSelectedCupcakeSize() {
        return selectedCupcakeSize;
    }
}
