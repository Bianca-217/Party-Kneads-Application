package com.ignacio.partykneadsapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.ToCompleteAdapter;
import com.ignacio.partykneadsapp.adapters.ToReceiveAdapter;
import com.ignacio.partykneadsapp.adapters.ToShipAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentOrderBinding;
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderFragment extends Fragment {

    FragmentOrderBinding binding;
    ToShipAdapter toShipAdapter;
    ToReceiveAdapter toReceiveAdapter;
    ToCompleteAdapter toCompleteAdapter; // Add adapter for completed orders
    List<ToShipModel> orderList = new ArrayList<>();
    FirebaseFirestore db;
    String adminEmail = "sweetkatrinabiancaignacio@gmail.com";
    String currentUserEmail;

    // Track which tab is currently active
    private static final String TO_SHIP = "To Ship";
    private static final String TO_RECEIVE = "To Receive";
    private static final String TO_COMPLETE = "Completed";
    private String currentTab = TO_SHIP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        setupBackButton();
        initializeAdapters();
        setupTabButtons();
        fetchOrdersBasedOnTab();
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_orderFragment_to_homePageFragment, args1);
        });
    }

    private void initializeAdapters() {
        // Initialize adapters
        toShipAdapter = new ToShipAdapter(orderList, getContext());
        toReceiveAdapter = new ToReceiveAdapter(orderList, getContext());  // Same adapter for both tabs
        toCompleteAdapter = new ToCompleteAdapter(orderList, getContext()); // Adapter for completed orders

        // Set the initial adapter
        binding.toShipRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.toShipRecycler.setAdapter(toShipAdapter);  // Default adapter
    }

    private void setupTabButtons() {
        // Default and selected colors
        int defaultBackgroundColor = getResources().getColor(R.color.semiwhite);
        int defaultTextColor = getResources().getColor(R.color.pink);
        int selectedBackgroundColor = getResources().getColor(R.color.pink);
        int selectedTextColor = getResources().getColor(R.color.semiwhite);

        // Helper method to reset all buttons
        Runnable resetButtonStyles = () -> {
            binding.btnToShip.setBackgroundTintList(ColorStateList.valueOf(defaultBackgroundColor));
            binding.btnToShip.setTextColor(defaultTextColor);

            binding.btnToReceive.setBackgroundTintList(ColorStateList.valueOf(defaultBackgroundColor));
            binding.btnToReceive.setTextColor(defaultTextColor);

            binding.btnCompleted.setBackgroundTintList(ColorStateList.valueOf(defaultBackgroundColor));
            binding.btnCompleted.setTextColor(defaultTextColor);
        };

        // Set default selected button to "To Ship"
        resetButtonStyles.run(); // Ensure all buttons start with the default state
        binding.btnToShip.setBackgroundTintList(ColorStateList.valueOf(selectedBackgroundColor));
        binding.btnToShip.setTextColor(selectedTextColor);
        binding.toShipRecycler.setAdapter(toShipAdapter);
        currentTab = TO_SHIP; // Set the initial tab value
        fetchOrdersBasedOnTab(); // Fetch data for the default tab

        // To Ship button
        binding.btnToShip.setOnClickListener(v -> {
            currentTab = TO_SHIP;
            resetButtonStyles.run();
            binding.btnToShip.setBackgroundTintList(ColorStateList.valueOf(selectedBackgroundColor));
            binding.btnToShip.setTextColor(selectedTextColor);
            binding.toShipRecycler.setAdapter(toShipAdapter);
            fetchOrdersBasedOnTab();
        });

        // To Receive button
        binding.btnToReceive.setOnClickListener(v -> {
            currentTab = TO_RECEIVE;
            resetButtonStyles.run();
            binding.btnToReceive.setBackgroundTintList(ColorStateList.valueOf(selectedBackgroundColor));
            binding.btnToReceive.setTextColor(selectedTextColor);
            binding.toShipRecycler.setAdapter(toReceiveAdapter);
            fetchOrdersBasedOnTab();
        });

        // Completed button
        binding.btnCompleted.setOnClickListener(v -> {
            currentTab = TO_COMPLETE;
            resetButtonStyles.run();
            binding.btnCompleted.setBackgroundTintList(ColorStateList.valueOf(selectedBackgroundColor));
            binding.btnCompleted.setTextColor(selectedTextColor);
            binding.toShipRecycler.setAdapter(toCompleteAdapter);
            fetchOrdersBasedOnTab();
        });
    }



    private void fetchOrdersBasedOnTab() {
        if (TO_SHIP.equals(currentTab)) {
            fetchToShipOrders();
        } else if (TO_RECEIVE.equals(currentTab)) {
            fetchToReceiveOrders();
        } else if (TO_COMPLETE.equals(currentTab)) {
            fetchToCompleteOrders(); // Fetch completed orders
        }
    }

    private void fetchToShipOrders() {
        db.collection("Users")
                .whereEqualTo("email", adminEmail)
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
                                    toShipAdapter.notifyDataSetChanged();  // Notify adapter for changes
                                })
                                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
    }

    private void fetchToReceiveOrders() {
        db.collection("Users")
                .whereEqualTo("email", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String adminUid = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("Users").document(adminUid)
                                .collection("Orders")
                                .whereEqualTo("userEmail", currentUserEmail)
                                .whereEqualTo("status", "Out for Delivery")
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    orderList.clear();
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String status = doc.getString("status");
                                        String referenceId = doc.getId();
                                        fetchFirstItemFromOrder(doc, referenceId, status);
                                    }
                                    toReceiveAdapter.notifyDataSetChanged();  // Notify adapter for changes
                                })
                                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
    }

    private void fetchToCompleteOrders() {
        db.collection("Users")
                .whereEqualTo("email", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String adminUid = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("Users").document(adminUid)
                                .collection("Orders")
                                .whereEqualTo("userEmail", currentUserEmail)
                                .whereEqualTo("status", "Complete Order")
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    orderList.clear();
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String status = doc.getString("status");
                                        String referenceId = doc.getId();
                                        fetchFirstItemFromOrder(doc, referenceId, status);
                                    }
                                    toCompleteAdapter.notifyDataSetChanged();  // Notify adapter for changes
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

            // Assuming the productId and referenceID are not available here, use placeholders
            String productId = "defaultProductId"; // You can set a default or fetch it from the Firestore document
            String orderReferenceId = referenceId; // Use the provided referenceId as the referenceID

            // Create the OrderItemModel with the correct constructor
            OrderItemModel item = new OrderItemModel(productId, productName, cakeSize, imageUrl, (int) quantity, totalPrice, orderReferenceId);

            List<OrderItemModel> itemList = new ArrayList<>();
            itemList.add(item);

            // Create the ToShipModel and add it to the list
            ToShipModel order = new ToShipModel(referenceId, displayStatus, totalPrice, productName, cakeSize, imageUrl, (int) quantity, itemList);
            orderList.add(order);
        }
    }

}


