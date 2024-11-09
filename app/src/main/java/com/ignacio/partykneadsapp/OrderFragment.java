package com.ignacio.partykneadsapp;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.ToShipAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentOrderBinding;
import com.ignacio.partykneadsapp.model.OrderItemModel;
import com.ignacio.partykneadsapp.model.ToShipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderFragment extends Fragment {

    FragmentOrderBinding binding;
    ToShipAdapter orderAdapter;
    List<ToShipModel> orderList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrderBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupBackButton();
        initializeAdapter();
        fetchOrders();

        binding.btnToShip.setOnClickListener(v -> { fetchOrders(); });

    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_orderFragment_to_homePageFragment, args1);
        });
    }

    private void initializeAdapter() {
        orderAdapter = new ToShipAdapter(orderList, getContext());
        binding.toShipRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.toShipRecycler.setAdapter(orderAdapter);
    }

    private void fetchOrders() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String adminEmail = "sweetkatrinabiancaignacio@gmail.com";
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Fetch the admin UID and user's orders in a single method
        db.collection("Users")
                .whereEqualTo("email", adminEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String adminUid = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("Users").document(adminUid)
                                .collection("Orders")
                                .whereEqualTo("userEmail", currentUserEmail)
                                .get()
                                .addOnSuccessListener(orderSnapshots -> {
                                    orderList.clear();
                                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                                        String referenceId = doc.getId(); // Get the document ID
                                        fetchFirstItemFromOrder(doc, referenceId);
                                    }
                                    orderAdapter.notifyDataSetChanged(); // Refresh RecyclerView
                                })
                                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
                    } else {
                        Log.e("Firestore Error", "Admin user not found.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", e.getMessage()));
    }

    private void fetchFirstItemFromOrder(QueryDocumentSnapshot doc, String referenceId) {
        // Get the items list as a List<Map<String, Object>> from Firestore
        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");

        if (items != null && !items.isEmpty()) {
            // Fetch only the first item from the list
            Map<String, Object> firstItem = items.get(0);

            // Extract values from the first item
            String productName = (String) firstItem.get("productName");
            String cakeSize = (String) firstItem.get("cakeSize");
            long quantity = firstItem.get("quantity") != null ? (long) firstItem.get("quantity") : 0; // Assuming quantity is stored as long
            String totalPrice = (String) firstItem.get("totalPrice");
            String imageUrl = (String) firstItem.get("imageUrl");
            String status = doc.getString("status").equals("preparing order") ?
                    "Seller preparing your order" : "Placed Order";

            // Create a new OrderItemModel instance
            OrderItemModel item = new OrderItemModel(productName, cakeSize, imageUrl, (int) quantity, totalPrice);

            // Create a new list to hold the OrderItemModel instance(s)
            List<OrderItemModel> itemList = new ArrayList<>();
            itemList.add(item);  // Add the first item to the list

            // Create a new ToShipModel instance using the referenceId and the first item
            ToShipModel order = new ToShipModel(referenceId, status, totalPrice, productName, cakeSize, imageUrl, (int) quantity, itemList);

            // Add the order to the orderList
            orderList.add(order);
        }
    }

}
