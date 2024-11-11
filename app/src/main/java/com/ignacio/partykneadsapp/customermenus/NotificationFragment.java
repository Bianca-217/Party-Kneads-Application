package com.ignacio.partykneadsapp.customermenus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.adapters.NotificationAdapter;
import com.ignacio.partykneadsapp.model.CartItemModel;
import com.ignacio.partykneadsapp.model.NotificationViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<CartItemModel> orderItems;
    private NotificationViewModel notificationViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Initialize RecyclerView
        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize ViewModel
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);

        // Observe the LiveData for order status changes (toship or toreceive)
        notificationViewModel.getOrderReferenceId().observe(getViewLifecycleOwner(), orderRefId -> {
            // Fetch the order details based on the orderRefId
            fetchOrderDetails(orderRefId);
        });

        return view;
    }

    private void fetchOrderDetails(String orderRefId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = "QqqccLchjigd0C7zf8ewPXY0KZc2"; // This should be dynamically fetched from the current user (customer)

        // Fetch the order details from the database using the provided orderRefId
        db.collection("Users").document(userId) // Current user's document ID
                .collection("Orders")
                .document(orderRefId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<HashMap<String, Object>> items = (List<HashMap<String, Object>>) documentSnapshot.get("items");
                        String orderStatus = documentSnapshot.getString("status"); // Get the order status

                        orderItems = new ArrayList<>();
                        for (HashMap<String, Object> itemData : items) {
                            // Create CartItemModel objects using the correct constructor
                            String productId = (String) itemData.get("productId");
                            String productName = (String) itemData.get("productName");
                            String cakeSize = (String) itemData.get("cakeSize");
                            int quantity = ((Long) itemData.get("quantity")).intValue();
                            String totalPrice = (String) itemData.get("totalPrice"); // Assuming price is stored as a string
                            String imageUrl = (String) itemData.get("imageUrl");

                            CartItemModel item = new CartItemModel(productId, productName, cakeSize, quantity, totalPrice, imageUrl);
                            orderItems.add(item);
                        }

                        // Update the UI if necessary
                        if (notificationAdapter == null) {
                            notificationAdapter = new NotificationAdapter(orderItems);
                            notificationRecyclerView.setAdapter(notificationAdapter);
                        } else {
                            notificationAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("NotificationFragment", "Error fetching order details", e));
    }

    // Method to handle new notification updates (toship, toreceive, etc.)
    private void updateNotification(String orderStatus, String orderRefId) {
        String message = "";

        // Determine the message based on the order status
        if ("to ship".equals(orderStatus)) {
            message = "Your order is now ready to ship!";
        } else if ("out for delivery".equals(orderStatus)) {
            message = "Your order is out for delivery!";
        }

        // Add this notification message to the list
        CartItemModel notificationItem = new CartItemModel("Notification", message, "", 1, "", "");
        orderItems.add(notificationItem); // Add notification to the list
        notificationAdapter.notifyDataSetChanged(); // Notify the adapter to update the view
    }

    // Public method to update the notification list manually (if needed)
    public void updateNotificationList(String orderRefId) {
        fetchOrderDetails(orderRefId); // Re-fetch the order details when needed
    }
}