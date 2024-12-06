package com.ignacio.partykneadsapp.adapters;

import android.app.Dialog;
import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.NotificationViewModel;
import com.ignacio.partykneadsapp.model.ToShipModel;
import com.ignacio.partykneadsapp.model.OrderItemModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToReceiveAdapter extends RecyclerView.Adapter<ToReceiveAdapter.OrderViewHolder> {
    private List<ToShipModel> orderList;
    private Context context;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();


    // Constructor to initialize the order list and context
    public ToReceiveAdapter(List<ToShipModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.toreceiveitems, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        ToShipModel order = orderList.get(position);
        holder.productName.setText(order.getProductName());
        holder.cakeSize.setText(order.getCakeSize());
        holder.quantity.setText(String.valueOf(order.getQuantity()));
        holder.totalPrice.setText(order.getTotalPrice());

        // Load image using Glide
        Glide.with(context)
                .load(order.getImageUrl())
                .into(holder.cakeImage);

        // Set the status text
        holder.orderStatus.setText(order.getStatus());

        // Handle button click if needed
        holder.receiveButton.setOnClickListener(v -> {
            Toast.makeText(context, "Order received!", Toast.LENGTH_SHORT).show();
            // Additional logic to mark order as received
        });

        holder.receiveButton.setOnClickListener(v -> {
            // Open the confirmation dialog
            showConfirmReceivedDialog(order.getReferenceId(), holder.getAdapterPosition());
        });

        // Handle item click to open dialog
        holder.itemView.setOnClickListener(v -> showOrderDetailsDialog(order.getReferenceId()));

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }


    // ViewHolder class
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, cakeSize, quantity, totalPrice, orderStatus;
        ImageView cakeImage;
        Button receiveButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            receiveButton = itemView.findViewById(R.id.receiveButton);
        }
    }


    private void showConfirmReceivedDialog(String referenceId, int position) {
        // Create a dialog
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.confirmreceived_dialog);

        // Set the background of the dialog to transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set up the confirmation button (btnReceive)
        Button btnReceive = dialog.findViewById(R.id.btnReceive);
        btnReceive.setOnClickListener(v -> {
            // Get the productId from the order
            String productId = getProductIdFromOrder(referenceId, position); // This function fetches the productId from the order's REF field

            if (productId != null) {
                // Update the order status in Firestore
                updateOrderStatus(referenceId);
                String quantity = String.valueOf(orderList.get(position).getQuantity());
                // Create notification for in-app and Firestore update
                createNotification(referenceId, position, new ToShipModel());

                // Update the order status in the RecyclerView list
                orderList.get(position).setStatus("Complete Order");
                notifyItemChanged(position);  // Refresh the item to show the updated status
                updateOrderStatusLocally(position, "Complete Order");

                // Show confirmation message
                Toast.makeText(context, "Order marked as complete!", Toast.LENGTH_SHORT).show();
                // Update the product's sold field
                updateProductSoldField(productId, quantity);

                // Dismiss the dialog
                dialog.dismiss();
            } else {
                Log.e("FirestoreError", "Product ID is null, cannot update sold count");
                Toast.makeText(context, "Error: Product ID is null", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the "No" button (btnNo) to dismiss the dialog
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            // Dismiss the dialog when "No" is clicked
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    // In ToReceiveAdapter
    public String getProductIdFromOrder(String referenceId, int position) {
        ToShipModel order = orderList.get(position);  // Get the order at the given position
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            OrderItemModel item = order.getItems().get(0);  // Get the first item (or any item you need)
            return item.getProductId();  // Return the productId
        }
        return null;
    }


    private void updateProductSoldField(String productId, String quant) {
        if (productId != null) {
            // Create a map to store the updated 'sold' field value
            Map<String, Object> updatedProduct = new HashMap<>();
            long quantNum = Long.parseLong(quant);

            // Fetch the current value of the 'sold' field and increment it
            firestore.collection("products").document(productId)
                    .get()
                    .addOnSuccessListener(productSnapshot -> {
                        int currentSoldCount = 0;
                        if (productSnapshot.exists() && productSnapshot.contains("sold")) {
                            // If the 'sold' field exists, get the current count
                            currentSoldCount = productSnapshot.getLong("sold").intValue();
                        }

                        // Increment the 'sold' count by 1
                        updatedProduct.put("sold", currentSoldCount + quantNum);

                        // Update the 'sold' field in the product document
                        firestore.collection("products").document(productId)
                                .update(updatedProduct)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Product sold count updated", Toast.LENGTH_SHORT).show(); // Use context here
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to update sold count", Toast.LENGTH_SHORT).show(); // Use context here
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error fetching product: " + e.getMessage());
                    });
        }
    }

    private void createNotification(String referenceId, int position, ToShipModel shipModel) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = currentUser != null ? currentUser.getEmail() : null;

        // The target emails
        List<String> targetEmails = new ArrayList<>();
        targetEmails.add("sweetkatrinabiancaignacio@gmail.com"); // Add the first email
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            targetEmails.add(currentUserEmail); // Add the current user email
        }

        // Create the notification details
        String orderStatus;
        String userRateComment;
        String cakeImageUrl = shipModel.getImageUrl(); // Use the image URL from the order

        if (cakeImageUrl == null || cakeImageUrl.isEmpty()) {
            cakeImageUrl = "default_image_url"; // Use a default image if URL is missing
        }

        // Check if the user is admin or customer
        if (isAdmin(currentUserEmail)) {
            orderStatus = "Order Received by Customer";
            userRateComment = "The customer has received their order successfully. Great work on completing the delivery process!";
        } else {
            orderStatus = "Order Complete";
            userRateComment = "Your order has been successfully completed! We appreciate your trust. Thank you for choosing Party Kneads.";
        }

        // Create the notification data
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("orderStatus", orderStatus);
        notificationData.put("userRateComment", userRateComment);
        notificationData.put("imageUrl", cakeImageUrl);
        notificationData.put("createdAt", FieldValue.serverTimestamp());

        // Iterate over the target emails to send notifications
        for (String email : targetEmails) {
            db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            String uid = userDoc.getId();

                            // Add the notification to the user's Notifications subcollection
                            db.collection("Users")
                                    .document(uid)
                                    .collection("Notifications")
                                    .add(notificationData)
                                    .addOnSuccessListener(documentReference -> {
                                        // Notification successfully added
                                        Log.d("Notification", "Notification added successfully for " + email);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Notification", "Error adding notification for " + email, e);
                                    });
                        } else {
                            Log.e("Notification", "User not found for email: " + email);
                        }
                    });
        }
    }


    private boolean isAdmin(String email) {
        // Implement logic to check if the email belongs to an admin user
        return email.equals("sweetkatrinabiancaignacio@gmail.com");  // Replace with actual admin check logic
    }

    public void updateOrderStatusLocally(int position, String status) {
        // Update the status in the orderList
        ToShipModel order = orderList.get(position);
        order.setStatus(status);  // Update the order status

        // Notify the adapter that the item at this position has been updated
        notifyItemChanged(position);
    }

    private void updateOrderStatus(String referenceId) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Users collection where email field equals "sweetkatrinabiancaignacio@gmail.com"
        db.collection("Users")
                .whereEqualTo("email", "sweetkatrinabiancaignacio@gmail.com")  // Query by email
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Assuming only one document will be returned
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String uid = document.getId();  // Get the UID of the user (document ID)

                        // Now use the UID to access the Orders collection and update the order status
                        DocumentReference orderRef = db.collection("Users")
                                .document(uid)  // Use the UID to access the user's document
                                .collection("Orders")
                                .document(referenceId);  // Order document ID using referenceId

                        // Update the status to "Complete Order"
                        orderRef.update("status", "Complete Order")
                                .addOnSuccessListener(aVoid -> {
                                    // Order status successfully updated
                                    Log.d("OrderAdapter", "Order status updated to Complete Order.");
                                })
                                .addOnFailureListener(e -> {
                                    // Handle error in updating the status
                                    Log.e("OrderAdapter", "Error updating order status", e);
                                });
                    } else {
                        // Handle the case where the email is not found or query fails
                        Log.e("OrderAdapter", "User with email not found or query failed.");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors with the query
                    Log.e("OrderAdapter", "Error querying Users collection", e);
                });
    }


    private void showOrderDetailsDialog(String referenceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the 'Users' collection for the document with the specified email
        db.collection("Users")
                .whereEqualTo("email", "sweetkatrinabiancaignacio@gmail.com")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the user document
                        DocumentSnapshot userDocument = querySnapshot.getDocuments().get(0);

                        // Access the Orders collection and the specific order by referenceId
                        db.collection("Users")
                                .document(userDocument.getId())
                                .collection("Orders")
                                .document(referenceId)
                                .get()
                                .addOnSuccessListener(orderSnapshot -> {
                                    if (orderSnapshot.exists()) {
                                        // Retrieve the 'items' field from the order document
                                        List<Map<String, Object>> items = (List<Map<String, Object>>) orderSnapshot.get("items");

                                        // Initialize the dialog
                                        Dialog dialog = new Dialog(context);
                                        dialog.setContentView(R.layout.view_order_details);

                                        // Set the dialog background to transparent
                                        if (dialog.getWindow() != null) {
                                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        }

                                        // Set margin of 20dp horizontally and center the dialog
                                        Window window = dialog.getWindow();
                                        if (window != null) {
                                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                                            layoutParams.copyFrom(window.getAttributes());

                                            // Convert dp to pixels
                                            int margin = (int) (20 * context.getResources().getDisplayMetrics().density);

                                            // Set width to match parent with 20dp margin on each side
                                            layoutParams.width = (int) (context.getResources().getDisplayMetrics().widthPixels - margin * 2); // 20dp margin on each side
                                            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Height to wrap content

                                            // Set the gravity to center the dialog
                                            layoutParams.gravity = Gravity.CENTER;

                                            // Apply the layout parameters
                                            window.setAttributes(layoutParams);
                                        }

                                        // Get references to the TextViews in the dialog
                                        TextView orderIdTextView = dialog.findViewById(R.id.OrderID);
                                        TextView itemTotalTextView = dialog.findViewById(R.id.itemTotal);
                                        TextView totalCosttTextView = dialog.findViewById(R.id.totalCost);
                                        RecyclerView productsRecyclerView = dialog.findViewById(R.id.recyclerViewCart);

                                        productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                                        // Adapter for the RecyclerView
                                        List<OrderItemModel> productList = new ArrayList<>();
                                        OrderItemsAdapter adapter = new OrderItemsAdapter(productList, context);
                                        productsRecyclerView.setAdapter(adapter);

                                        // Set the OrderID TextView to the referenceId
                                        orderIdTextView.setText(referenceId);

                                        // Variable to hold the total price sum
                                        double totalPrice = 0;

                                        // Check if 'items' is null or empty
                                        if (items == null || items.isEmpty()) {
                                            Log.d("OrderDetailsDialog", "No items found or empty items list.");
                                            Toast.makeText(context, "No items found in the order.", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss(); // Close dialog if no items
                                        } else {
                                            // Iterate through items and calculate the total price
                                            for (Map<String, Object> item : items) {
                                                String productName = (String) item.get("productName");
                                                String cakeSize = (String) item.get("cakeSize");
                                                long quantity = item.containsKey("quantity") ? (long) item.get("quantity") : 0;
                                                String imageUrl = (String) item.get("imageUrl");
                                                String price = (String) item.get("totalPrice");

                                                // Add item to the list if the required fields are not null
                                                if (productName != null && price != null) {
                                                    String productId = "someProductId";  // Make sure you have the productId here (either pass it from elsewhere or fetch it)

                                                    // Create new OrderItemModel with productId
                                                    productList.add(new OrderItemModel(productId, productName, cakeSize, imageUrl, (int) quantity, price, referenceId));


                                                    // Sum the totalPrice (assuming the price is stored with the "₱" symbol)
                                                    try {
                                                        String priceWithoutSymbol = price.replace("₱", "").trim();  // Remove "₱" symbol and spaces
                                                        double itemPrice = Double.parseDouble(priceWithoutSymbol);  // Convert to double
                                                        totalPrice += itemPrice * quantity;  // Multiply by quantity and add to totalPrice
                                                    } catch (NumberFormatException e) {
                                                        Log.e("ParseError", "Invalid price format: " + price);
                                                    }
                                                }
                                            }

                                            // Notify adapter of the new data
                                            adapter.notifyDataSetChanged();

                                            // Set the total price to the itemTotal TextView
                                            itemTotalTextView.setText("₱" + String.format("%.2f", totalPrice)); // Format to 2 decimal places
                                            totalCosttTextView.setText(itemTotalTextView.getText());
                                        }

                                        // Show the dialog after all data is set
                                        dialog.show();
                                    } else {
                                        Log.d("OrderDetailsDialog", "Order document not found.");
                                        Toast.makeText(context, "Order not found.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore Error", e.getMessage());
                                    Toast.makeText(context, "Failed to fetch order details.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.d("OrderDetailsDialog", "User document not found.");
                        Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", e.getMessage());
                    Toast.makeText(context, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                });
    }
}