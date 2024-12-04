package com.ignacio.partykneadsapp.adapters;

import android.app.Dialog;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.ToShipModel;
import com.ignacio.partykneadsapp.model.OrderItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToShipAdapter extends RecyclerView.Adapter<ToShipAdapter.OrderViewHolder> {
    private List<ToShipModel> orderList;
    private Context context;

    // Constructor to initialize the order list and context
    public ToShipAdapter(List<ToShipModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.toshipitems, parent, false);
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

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            cakeImage = itemView.findViewById(R.id.cakeImage);
            orderStatus = itemView.findViewById(R.id.txtStatus);
        }
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
