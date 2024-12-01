package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.PendingItemAdapter;
import com.ignacio.partykneadsapp.adapters.ToShipAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentPendingBinding;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingFragment extends Fragment {
    private RecyclerView toShipRecycler;
    private PendingItemAdapter toShipAdapter;
    private List<ToShipModel> orderList = new ArrayList<>();
    private FirebaseFirestore db;
    FragmentPendingBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPendingBinding.inflate(getLayoutInflater());

        toShipRecycler = binding.pendingRecyclerview;
        toShipAdapter = new PendingItemAdapter(orderList, getContext());
        toShipRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        toShipRecycler.setAdapter(toShipAdapter);


        fetchPlacedOrders();
        return binding.getRoot();
    }


    private void fetchPlacedOrders() {
        db = FirebaseFirestore.getInstance();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("Users")
                .whereEqualTo("email", "sweetkatrinabiancaignacio@gmail.com")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get admin UID
                        String adminUid = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Query placed orders, sorted by timestamp in descending order
                        db.collection("Users").document(adminUid)
                                .collection("Orders")
                                .whereEqualTo("userEmail", currentUserEmail)
                                .whereEqualTo("status", "placed")
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    orderList.clear(); // Clear the list before adding new data
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String status = doc.getString("status");
                                        String referenceId = doc.getId();
                                        fetchFirstItemFromOrder(doc, referenceId, status);
                                    }
                                    toShipAdapter.notifyDataSetChanged(); // Notify the adapter about data changes
                                })
                                .addOnFailureListener(e -> Log.e("Firestore Error", "Failed to fetch orders: " + e.getMessage()));
                    } else {
                        Log.w("Firestore Warning", "No admin UID found for the provided email.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", "Failed to fetch admin UID: " + e.getMessage()));
    }





    private void fetchFirstItemFromOrder(QueryDocumentSnapshot doc, String referenceId, String status) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");

        if (items != null && !items.isEmpty()) {
            Map<String, Object> firstItem = items.get(0);

            // Extract details from the first item
            String productName = (String) firstItem.get("productName");
            String cakeSize = (String) firstItem.get("cakeSize");
            long quantity = firstItem.get("quantity") != null ? (long) firstItem.get("quantity") : 0;
            String totalPrice = (String) firstItem.get("totalPrice");
            String imageUrl = (String) firstItem.get("imageUrl");

            // Display-friendly status mapping
            String displayStatus = "Order has been placed";

            // Create the order model and add to the list
            ToShipModel order = new ToShipModel(
                    referenceId,
                    displayStatus,
                    totalPrice,
                    productName,
                    cakeSize,
                    imageUrl,
                    (int) quantity,
                    new ArrayList<>() // Assuming item list is optional for simplicity
            );

            orderList.add(order);
        }
    }
}