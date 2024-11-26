package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.ToShipAdapter;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToShipFragment extends Fragment {
    private RecyclerView toShipRecycler;
    private ToShipAdapter toShipAdapter;
    private List<ToShipModel> orderList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_to_ship, container, false);

        // Initialize RecyclerView and Adapter
        toShipRecycler = view.findViewById(R.id.toShipRecycler);
        toShipAdapter = new ToShipAdapter(orderList, getContext());
        toShipRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        toShipRecycler.setAdapter(toShipAdapter);

        // Fetch orders
        fetchToShipOrders();

        return view;
    }

    private void fetchToShipOrders() {
        db = FirebaseFirestore.getInstance();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("Users")
                .whereEqualTo("email", "sweetkatrinabiancaignacio@gmail.com")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String adminUid = queryDocumentSnapshots.getDocuments().get(0).getId();

                        db.collection("Users").document(adminUid)
                                .collection("Orders")
                                .whereEqualTo("userEmail", currentUserEmail)
                                .whereIn("status", List.of("placed", "preparing order"))
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    orderList.clear();
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String status = doc.getString("status");
                                        String referenceId = doc.getId();
                                        fetchFirstItemFromOrder(doc, referenceId, status);
                                    }
                                    toShipAdapter.notifyDataSetChanged(); // Update the adapter with new data
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

            // Extract details from the first item
            String productName = (String) firstItem.get("productName");
            String cakeSize = (String) firstItem.get("cakeSize");
            long quantity = firstItem.get("quantity") != null ? (long) firstItem.get("quantity") : 0;
            String totalPrice = (String) firstItem.get("totalPrice");
            String imageUrl = (String) firstItem.get("imageUrl");

            // Display-friendly status mapping
            String displayStatus;
            switch (status.toLowerCase()) {
                case "preparing order":
                    displayStatus = "Seller is preparing your order";
                    break;
                case "placed":
                    displayStatus = "Order has been placed";
                    break;
                default:
                    displayStatus = "Unknown status";
                    break;
            }

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
