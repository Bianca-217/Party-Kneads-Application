package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.CancelCustomerModel;

import java.util.List;
public class CancelCustomerAdapter extends RecyclerView.Adapter<CancelCustomerAdapter.CancelCustomerViewHolder> {
    private List<CancelCustomerModel> canceledOrderList;
    private Context context;

    public CancelCustomerAdapter(List<CancelCustomerModel> canceledOrderList, Context context) {
        this.canceledOrderList = canceledOrderList;
        this.context = context;
    }

    @NonNull
    @Override
    public CancelCustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tocancelitems, parent, false);
        return new CancelCustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CancelCustomerViewHolder holder, int position) {
        CancelCustomerModel order = canceledOrderList.get(position);

        // Bind data to the views
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());
        holder.reason.setText(order.getReason());

        // Load image using Glide
        Glide.with(context)
                .load(order.getImageUrl())
                .into(holder.cakeImage);

        holder.status.setText(order.getStatus());  // Set the "Cancelled" status
    }

    @Override
    public int getItemCount() {
        return canceledOrderList.size();
    }

    // ViewHolder class for canceled orders
    public static class CancelCustomerViewHolder extends RecyclerView.ViewHolder {
        TextView productName, cakeSize, quantity, totalPrice, status, reason;
        ImageView cakeImage;

        public CancelCustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            status = itemView.findViewById(R.id.txtStatus);
            reason = itemView.findViewById(R.id.reason);
        }
    }
}
