package com.ignacio.partykneadsapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.OrderItem;

import java.util.List;

public class InnerOrderAdapter extends RecyclerView.Adapter<InnerOrderAdapter.InnerOrderViewHolder> {
    private List<OrderItem> orderItems;

    public InnerOrderAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public InnerOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cartcheckout_items, parent, false);
        return new InnerOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerOrderViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.productName.setText(item.getProductName());
        holder.quantity.setText(String.valueOf(item.getQuantity()));
        holder.totalPrice.setText(item.getTotalPrice());
        // Set any other item details here
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class InnerOrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, quantity, totalPrice;

        public InnerOrderViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
        }
    }
}


