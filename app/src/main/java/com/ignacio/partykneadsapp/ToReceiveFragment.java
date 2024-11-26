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
import com.ignacio.partykneadsapp.adapters.ToReceiveAdapter;
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToReceiveFragment extends Fragment {
    private RecyclerView toReceiveRecycler;
    private ToReceiveAdapter toReceiveAdapter;
    private List<ToShipModel> orderList = new ArrayList<>();  // Change to ToShipModel
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_to_receive, container, false);

        // Initialize RecyclerView and Adapter
        toReceiveRecycler = view.findViewById(R.id.toReceiveRecyclerview);
        toReceiveAdapter = new ToReceiveAdapter(orderList, getContext());  // Using ToShipModel here
        toReceiveRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        toReceiveRecycler.setAdapter(toReceiveAdapter);

        // Fetch orders
        fetchToReceiveOrders();

        return view;
    }

    private void fetchToReceiveOrders() {
        db = FirebaseFirestore.getInstance();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("Users")
                .whereEqualTo("email", "sweetkatrinabiancaignacio@gmail.com")  // You may want to replace this hardcoded email
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String adminUid = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Fetch orders where the user email matches and the status is "Out for Delivery"
                        db.collection("Users").document(adminUid)
                                .collection("Orders")
                                .whereEqualTo("userEmail", currentUserEmail)
                                .whereEqualTo("status", "Out for Delivery")
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    orderList.clear();  // Clear the existing list before adding new orders
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String status = doc.getString("status");
                                        String referenceId = doc.getId();
                                        fetchFirstItemFromOrder(doc, referenceId, status);
                                    }
                                    toReceiveAdapter.notifyDataSetChanged();  // Update the adapter after data is loaded
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
            String totalPrice = (String) firstItem.get("totalPrice");
            String imageUrl = (String) firstItem.get("imageUrl");

            // Determine the display status based on the order status
            String displayStatus;
            switch (status.toLowerCase()) {
                case "preparing order":
                    displayStatus = "Seller is preparing your order";
                    break;
                case "out for delivery":
                    displayStatus = "Your order is Out for Delivery";
                    break;
                case "complete order":
                    displayStatus = "Your order has been delivered.";
                    break;
                default:
                    displayStatus = "Placed Order";
                    break;
            }

            // Create the OrderItemModel for the item
            OrderItemModel item = new OrderItemModel(productName, cakeSize, imageUrl, (int) quantity, totalPrice);

            // Create a ToShipModel with the item and other order details
            List<OrderItemModel> itemList = new ArrayList<>();
            itemList.add(item);

            // Construct the ToShipModel object with the full order details
            ToShipModel order = new ToShipModel(referenceId, displayStatus, totalPrice, productName, cakeSize, imageUrl, (int) quantity, itemList);

            // Add the order to the list
            orderList.add(order);
        }
    }
}
