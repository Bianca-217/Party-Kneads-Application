package com.ignacio.partykneadsapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.CancelCustomerAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentCancelCustomerBinding;
import com.ignacio.partykneadsapp.model.CancelCustomerModel;
import com.ignacio.partykneadsapp.databinding.FragmentCancelledOrderBinding;
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class CancelCustomer extends Fragment {
    private FragmentCancelCustomerBinding binding;  // Correct layout binding
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CancelCustomerAdapter adapter;
    private List<CancelCustomerModel> canceledOrdersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCancelCustomerBinding.inflate(inflater, container, false); // Correct layout binding
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize list and adapter
        canceledOrdersList = new ArrayList<>();
        adapter = new CancelCustomerAdapter(canceledOrdersList, getContext());

        // Initialize RecyclerView from binding
        binding.cancelRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.cancelRecyclerview.setAdapter(adapter);

        // Fetch cancelled orders for the current user
        fetchCancelledOrdersFromFirestore();
    }

    private void fetchCancelledOrdersFromFirestore() {
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
                                .whereEqualTo("status", "Cancelled")  // Fetch "Cancelled" orders
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    canceledOrdersList.clear();  // Clear the existing list before adding new orders
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String status = doc.getString("status");
                                        String reason = doc.getString("reason");
                                        String referenceId = doc.getId();
                                        fetchFirstItemFromOrder(doc, referenceId, status, reason);
                                    }
                                    adapter.notifyDataSetChanged();  // Update the adapter after data is loaded
                                })
                                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
    }

    private void fetchFirstItemFromOrder(QueryDocumentSnapshot doc, String referenceId, String status, String reason) {
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

            if (reason == null) {
                reason = "Others";
            }

            // Format the price with currency symbol
            String formattedPrice = String.format("₱%.2f", parsedPrice);

            // Construct the CancelCustomerModel with the item and other order details
            CancelCustomerModel order = new CancelCustomerModel(
                    productName,
                    cakeSize,
                    (int) quantity,
                    formattedPrice,
                    imageUrl,
                    status,
                    reason
            );

            // Add the order to the list
            canceledOrdersList.add(order);
        }
    }
}
