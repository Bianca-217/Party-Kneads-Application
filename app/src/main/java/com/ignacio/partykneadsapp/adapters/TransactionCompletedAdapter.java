package com.ignacio.partykneadsapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.OrderItemWithReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionCompletedAdapter extends RecyclerView.Adapter<TransactionCompletedAdapter.ItemViewHolder> {

    private List<OrderItemWithReference> orderListWithRefID;
    private Context context;

    public TransactionCompletedAdapter(List<OrderItemWithReference> orderListWithRefID, Context context) {
        this.orderListWithRefID = orderListWithRefID;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cartcheckout_items, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Get the OrderItemWithReference object
        OrderItemWithReference itemWithRef = orderListWithRefID.get(position);
        OrderItemModel orderItem = itemWithRef.getOrderItem();
        String referenceID = itemWithRef.getReferenceID();

        // Bind data to UI elements
        holder.productName.setText(orderItem.getProductName());
        holder.cakeSize.setText(orderItem.getCakeSize());
        holder.quantity.setText(String.valueOf(orderItem.getQuantity()));
        holder.price.setText(orderItem.getPrice());

        // Load image using Glide
        Glide.with(context).load(orderItem.getImageUrl()).into(holder.productImage);

        // Set click listener to show order details dialog using the referenceID
        holder.itemView.setOnClickListener(v -> showOrderDetailsDialog(referenceID));
    }

    @Override
    public int getItemCount() {
        return orderListWithRefID.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView productName, cakeSize, quantity, price;
        ImageView productImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            cakeSize = itemView.findViewById(R.id.cakeSize);
            quantity = itemView.findViewById(R.id.quantity);
            price = itemView.findViewById(R.id.totalPrice);
            productImage = itemView.findViewById(R.id.cakeImage);
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

                        // Access the Orders collection and the specific order by referenceID
                        db.collection("Users")
                                .document(userDocument.getId())
                                .collection("Orders")
                                .document(referenceId) // Use referenceID for the specific order
                                .get()
                                .addOnSuccessListener(orderDoc -> {
                                    if (orderDoc.exists()) {
                                        // Fetch the 'items' field from the order document
                                        List<Map<String, Object>> items = (List<Map<String, Object>>) orderDoc.get("items");

                                        // Initialize the dialog
                                        Dialog dialog = new Dialog(context);
                                        dialog.setContentView(R.layout.view_order_details);

                                        // Get references to the TextViews in the dialog
                                        TextView orderIdTextView = dialog.findViewById(R.id.OrderID);
                                        TextView itemTotalTextView = dialog.findViewById(R.id.itemTotal);
                                        TextView totalCostTextView = dialog.findViewById(R.id.totalCost);
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
                                                    productList.add(new OrderItemModel(productName, cakeSize, imageUrl, (int) quantity, price));

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
                                            totalCostTextView.setText(itemTotalTextView.getText());
                                        }

                                        // Show the dialog after all data is set
                                        dialog.show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("OrderDetailsDialog", "Error fetching order: ", e);
                                    Toast.makeText(context, "Failed to fetch order details.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderDetailsDialog", "Error fetching user: ", e);
                    Toast.makeText(context, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                });
    }
}
