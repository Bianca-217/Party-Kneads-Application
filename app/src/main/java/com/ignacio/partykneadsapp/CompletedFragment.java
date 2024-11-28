package com.ignacio.partykneadsapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.ToCompleteAdapter;
import com.ignacio.partykneadsapp.adapters.ToReceiveAdapter;  // Use ToReceiveAdapter (modify if necessary)
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompletedFragment extends Fragment {
    private RecyclerView completedRecycler;
    private ToCompleteAdapter toCompleteAdapter;  // Adapter for completed orders
    private List<ToShipModel> orderList = new ArrayList<>();  // List of completed orders (ToShipModel)
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_completed, container, false);

        // Initialize RecyclerView and Adapter
        completedRecycler = view.findViewById(R.id.completedRecyclerview);
        toCompleteAdapter = new ToCompleteAdapter(orderList, getContext());  // Use the adapter for ToShipModel
        completedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        completedRecycler.setAdapter(toCompleteAdapter);

        // Fetch completed orders
        fetchToCompleteOrders();

        return view;
    }

    private void fetchToCompleteOrders() {
        db = FirebaseFirestore.getInstance();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String adminEmail = "sweetkatrinabiancaignacio@gmail.com";  // Replace with actual admin email if necessary

        db.collection("Users")
                .whereEqualTo("email", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String adminUid = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Fetch orders where the user email matches and the status is "Complete Order"
                        db.collection("Users").document(adminUid)
                                .collection("Orders")
                                .whereEqualTo("userEmail", currentUserEmail)
                                .whereEqualTo("status", "Complete Order")
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    orderList.clear();  // Clear the existing list before adding new orders
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String status = doc.getString("status");
                                        String referenceId = doc.getId();
                                        fetchFirstItemFromOrder(doc, referenceId, status);
                                    }
                                    toCompleteAdapter.notifyDataSetChanged();  // Update the adapter after data is loaded
                                })
                                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
    }

    private void fetchFirstItemFromOrder(QueryDocumentSnapshot doc, String referenceId, String status) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");

        if (items != null && !items.isEmpty()) {
            Map<String, Object> firstItem = items.get(0);
            String productName = (String) firstItem.get("productName");
            String cakeSize = (String) firstItem.get("cakeSize");
            long quantity = firstItem.get("quantity") != null ? (long) firstItem.get("quantity") : 0;
            String totalPriceString = (String) firstItem.get("totalPrice");
            double parsedPrice = 0.0; // Default value in case parsing fails
            String imageUrl = (String) firstItem.get("imageUrl");

            if (totalPriceString != null) {
                try {
                    // Remove "₱" and parse the price
                    totalPriceString = totalPriceString.replace("₱", "").trim();
                    parsedPrice = Double.parseDouble(totalPriceString);
                } catch (NumberFormatException e) {
                    Log.e("Parsing Error", "Invalid price format: " + totalPriceString, e);
                }
            }

            // Determine the display status based on the order status
            String displayStatus = "Your order has been delivered.";  // For "Complete Order"

            // Use the parsed price (converted back to a string for consistency, if needed)
            String formattedPrice = String.format("₱%.2f", parsedPrice); // Display with currency symbol

            // Create the OrderItemModel for the item
            OrderItemModel item = new OrderItemModel(productName, cakeSize, imageUrl, (int) quantity, formattedPrice);

            // Create a ToShipModel with the item and other order details
            List<OrderItemModel> itemList = new ArrayList<>();
            itemList.add(item);

            // Construct the ToShipModel object with the full order details
            ToShipModel order = new ToShipModel(referenceId, displayStatus, formattedPrice, productName, cakeSize, imageUrl, (int) quantity, itemList);

            // Add the order to the list
            orderList.add(order);
        }
    }
}
