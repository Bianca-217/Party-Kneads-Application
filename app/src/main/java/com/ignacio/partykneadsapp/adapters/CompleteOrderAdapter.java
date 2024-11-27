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
import com.ignacio.partykneadsapp.model.CompleteOrderModel;

import java.util.List;

public class CompleteOrderAdapter extends RecyclerView.Adapter<CompleteOrderAdapter.ViewHolder> {

    private Context context;
    private List<CompleteOrderModel> ordersList;

    // Constructor
    public CompleteOrderAdapter(Context context, List<CompleteOrderModel> ordersList) {
        this.context = context;
        this.ordersList = ordersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.complete_order_seller_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompleteOrderModel order = ordersList.get(position);

        // Bind data to views
        holder.txtUserName.setText(order.getUserName());
        holder.contactNum.setText(order.getContactNum());
        holder.location.setText(order.getLocation());
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());
        holder.status.setText(order.getStatus());

        // Set image using Glide
        Glide.with(context).load(order.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.cakeImage);
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    // ViewHolder class to hold individual views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, contactNum, location, productName, cakeSize, quantity, totalPrice, status;
        ImageView cakeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txtUserName);
            contactNum = itemView.findViewById(R.id.contactNum);
            location = itemView.findViewById(R.id.location);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            status = itemView.findViewById(R.id.txtStatus); // Ensure status TextView ID matches the XML
            cakeImage = itemView.findViewById(R.id.cakeImage);
        }
    }
}
