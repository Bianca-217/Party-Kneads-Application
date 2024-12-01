package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.DeliverOrderModel;
import com.bumptech.glide.Glide;
import com.ignacio.partykneadsapp.model.NotificationViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeliverOrderAdapter extends RecyclerView.Adapter<DeliverOrderAdapter.DeliverOrderViewHolder> {

    private Context context;
    private List<DeliverOrderModel> ordersList;

    // Constructor
    public DeliverOrderAdapter(Context context, List<DeliverOrderModel> ordersList) {
        this.context = context;
        this.ordersList = ordersList != null ? ordersList : new ArrayList<>();
    }

    @NonNull
    @Override
    public DeliverOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the deliver order item layout
        View view = LayoutInflater.from(context).inflate(R.layout.deliver_orders_items, parent, false);
        return new DeliverOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliverOrderViewHolder holder, int position) {
        DeliverOrderModel order = ordersList.get(position);

        // Bind data to the views
        holder.txtUserName.setText(order.getUserName());
        holder.contactNum.setText(order.getContactNum());
        holder.location.setText(order.getLocation());
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());

        // Use Glide to load the cake image
        Glide.with(context)
                .load(order.getImageURL())
                .into(holder.cakeImage);

        // Button click listener (e.g., "Deliver" action)
        holder.btnDeliverOrder.setOnClickListener(v -> {
            // Get the order document reference from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String orderId = order.getOrderId();

            // Query Firestore to get the userEmail from the order document
            db.collection("Users")
                    .document(uid)
                    .collection("Orders")
                    .document(orderId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the userEmail from the document
                            String customerEmail = documentSnapshot.getString("userEmail");

                            // Update the order status to "Out for Delivery"
                            db.collection("Users")
                                    .document(uid)
                                    .collection("Orders")
                                    .document(orderId)
                                    .update("status", "Out for Delivery")
                                    .addOnSuccessListener(aVoid -> {
                                        // Optionally, show a toast or update the UI to reflect the change
                                        Toast.makeText(context, "Order is now out for delivery!", Toast.LENGTH_SHORT).show();

                                        // Create and save notification to the user
                                        NotificationViewModel notification = createNotification("Out for Delivery", order);
                                        saveNotificationToUser(customerEmail, notification);

                                        // You can also update the adapter's dataset if needed
                                        notifyItemChanged(position); // Refresh the item view
                                        removeItem(position);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle the failure (e.g., show an error toast)
                                        Toast.makeText(context, "Failed to update order status.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Handle the case where the order document doesn't exist
                            Toast.makeText(context, "Order not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle the failure to fetch the order document
                        Toast.makeText(context, "Failed to fetch order details.", Toast.LENGTH_SHORT).show();
                    });
        });
    }


    private NotificationViewModel createNotification(String newStatus, DeliverOrderModel order) {
        String orderStatus = "";
        String userRateComment = "";

        // Define the notification content based on the new status
        switch (newStatus) {
            case "preparing order":
                orderStatus = "Order Accepted by Seller";
                userRateComment = "Your order has been confirmed by the seller and is currently preparing it for delivery.";
                break;
            case "Out for Delivery":
                orderStatus = "Out for Delivery";
                userRateComment = "Your order is on its way! The seller is delivering your cake.";
                break;
            default:
                orderStatus = "Order Update";
                userRateComment = "Your order status has been updated.";
                break;
        }

        // Optionally, use an image URL from the order or default it
        String imageUrl = order.getImageURL();

        // Return the new notification
        return new NotificationViewModel(orderStatus, userRateComment, imageUrl);
    }

    private void saveNotificationToUser(String email, NotificationViewModel notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 5: Save the notification in the "Notifications" subcollection under the user's email
        db.collection("Users")
                .whereEqualTo("email", email)  // Find the user document by their email
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the user's document ID
                        String userDocId = querySnapshot.getDocuments().get(0).getId();

                        // Save the notification under the user's "Notifications" subcollection
                        db.collection("Users")
                                .document(userDocId)  // User's document ID
                                .collection("Notifications")  // Notifications subcollection
                                .add(new HashMap<String, Object>() {{
                                    put("orderStatus", notification.getOrderStatus());
                                    put("userRateComment", notification.getUserRateComment());
                                    put("imageUrl", notification.getCakeImageUrl());
                                    put("timestamp", FieldValue.serverTimestamp());  // Add a timestamp
                                }})
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("Notification", "Notification saved successfully for email: " + email);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Notification", "Error saving notification for email: " + email, e);
                                });
                    } else {
                        Log.w("Notification", "User with email not found: " + email);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Error fetching user with email: " + email, e);
                });
    }

    // Method to remove item from the list
    private void removeItem(int position) {
        // Ensure the position is within bounds of the list
        if (position >= 0 && position < ordersList.size()) {
            // Remove the item from the list
            ordersList.remove(position);
            // Notify the adapter that an item was removed
            notifyItemRemoved(position);
        } else {
            // Log an error if the position is invalid
            Log.e("PendingOrdersAdapter", "Invalid position for removal: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    // ViewHolder class for binding deliver order data to views
    public static class DeliverOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, contactNum, location, productName, cakeSize, quantity, totalPrice;
        ImageView cakeImage;
        Button btnDeliverOrder;

        public DeliverOrderViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            txtUserName = itemView.findViewById(R.id.txtUserName);
            contactNum = itemView.findViewById(R.id.contactNum);
            location = itemView.findViewById(R.id.location);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            btnDeliverOrder = itemView.findViewById(R.id.btnDeliverOrder);
        }
    }
}
