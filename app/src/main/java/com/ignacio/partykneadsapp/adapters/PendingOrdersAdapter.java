package com.ignacio.partykneadsapp.adapters;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.PendingOrdersModel;

import java.util.List;
public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.PendingOrdersViewHolder> {

    private Context context;
    private List<PendingOrdersModel> ordersList;

    public PendingOrdersAdapter(Context context, List<PendingOrdersModel> ordersList) {
        this.context = context;
        this.ordersList = ordersList;
    }

    @NonNull
    @Override
    public PendingOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pending_orders_items, parent, false);
        return new PendingOrdersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingOrdersViewHolder holder, int position) {
        PendingOrdersModel order = ordersList.get(position);
        holder.txtUserName.setText(order.getUserName());
        holder.contactNum.setText(order.getContactNum());
        holder.location.setText(order.getLocation());
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());

        Glide.with(context)
                .load(order.getImageURL())
                .into(holder.cakeImage);

        // Set OnClickListener for btnAcceptOrder
        holder.btnAcceptOrder.setOnClickListener(v -> {
            updateOrderStatus(order, "preparing order", position);
        });
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class PendingOrdersViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, contactNum, location, productName, cakeSize, quantity, totalPrice;
        ImageView cakeImage;
        Button btnAcceptOrder; // Button reference

        public PendingOrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            contactNum = itemView.findViewById(R.id.contactNum);
            location = itemView.findViewById(R.id.location);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            btnAcceptOrder = itemView.findViewById(R.id.btnAcceptOrder); // Initialize the button
        }
    }

    // Make this method public to be accessible
    public void updateOrderStatus(PendingOrdersModel order, String newStatus, int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user's UID
        CollectionReference ordersRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("Orders");

        String orderId = order.getOrderId(); // Adjust this according to your model

        // Update the status in Firestore
        ordersRef.document(orderId).update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Remove the item from the local list
                    ordersList.remove(position); // Assuming pendingOrdersList is your local list
                    notifyItemRemoved(position); // Notify the adapter that the item has been removed
                    Toast.makeText(context, "Order status updated to preparing order", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update order status", Toast.LENGTH_SHORT).show();
                });
    }

}
