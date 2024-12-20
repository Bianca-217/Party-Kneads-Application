package com.ignacio.partykneadsapp;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.ignacio.partykneadsapp.adapters.CheckoutAdapter;
import com.ignacio.partykneadsapp.adapters.CheckoutLocationAdapter;
import com.ignacio.partykneadsapp.adapters.NotificationAdapter;
import com.ignacio.partykneadsapp.customermenus.NotificationFragment;
import com.ignacio.partykneadsapp.model.CartItemModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.model.NotificationViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class CheckoutFragment extends Fragment {
    private RecyclerView recyclerView;
    private CheckoutAdapter coutAdapter;
    private List<CartItemModel> selectedItems;
    private TextView subTotalTextView;
    private TextView totalCostTextView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser cUser;
    private ImageView btnBack;
    private RecyclerView locationRecyclerView;
    private CheckoutLocationAdapter locationAdapter;
    private List<String> activeLocations;
    private TextView txtUserName;
    private TextView itemTotalTextView;
    private TextView discount;
    private Button showVouchers;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cUser = mAuth.getCurrentUser();


        btnBack = view.findViewById(R.id.btnBack);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        subTotalTextView = view.findViewById(R.id.subTotal);
        itemTotalTextView = view.findViewById(R.id.itemTotal);
        totalCostTextView = view.findViewById(R.id.totalPayment);
        txtUserName = view.findViewById(R.id.txtUserName);
        showVouchers = view.findViewById(R.id.showVouchers);
        discount = view.findViewById(R.id.discount);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        selectedItems = getArguments() != null ? getArguments().getParcelableArrayList("selectedItems") : new ArrayList<>();


        if (selectedItems != null && !selectedItems.isEmpty()) {
            for (CartItemModel item : selectedItems) {
                Log.d("CheckoutFragment", "Received Item: " + item.getProductName() + ", Quantity: " + item.getQuantity());
            }
        } else {
            Log.d("CheckoutFragment", "No selected items received from CartFragment.");
        }


        coutAdapter = new CheckoutAdapter(selectedItems);
        recyclerView.setAdapter(coutAdapter);


        locationRecyclerView = view.findViewById(R.id.locationRecycler);
        activeLocations = new ArrayList<>();
        locationAdapter = new CheckoutLocationAdapter(activeLocations);
        locationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationRecyclerView.setAdapter(locationAdapter);


        fetchUserNameAndLocations();


        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_checkoutFragment_to_cartFragment);
        });

        showVouchers.setOnClickListener(v -> {
            ChooseVoucherFragment dialogFragment = new ChooseVoucherFragment();
            dialogFragment.setVoucherSelectedListener(selectedVoucher -> {

                discount.setText(selectedVoucher);
                updateTotals();
            });
            dialogFragment.show(requireActivity().getSupportFragmentManager(), "ChooseVoucherDialog");
        });


        Button btnCheckout = view.findViewById(R.id.btncheckout);
        btnCheckout.setOnClickListener(v -> {
            saveOrderToDatabase();
        });


        updateTotals();
        return view;
    }



    private void fetchUserNameAndLocations() {
        if (cUser != null) {
            String userId = cUser.getUid();
            db.collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                String firstName = documentSnapshot.getString("First Name");
                String lastName = documentSnapshot.getString("Last Name");
                String userName = firstName + " " + lastName;


                fetchActiveLocations(userName);
            }).addOnFailureListener(e -> {
                Log.w("CheckoutFragment", "Error fetching user name", e);
            });
        }
    }

    private void fetchActiveLocations(String userName) {
        if (cUser != null) {
            String userId = cUser.getUid();


            db.collection("Users")
                    .document(userId)
                    .collection("Locations")
                    .whereEqualTo("status", "Active")
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            Log.w("CheckoutFragment", "Error fetching active locations", e);
                            return;
                        }

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            activeLocations.clear();


                            String userNameInLocation = "";
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String location = document.getString("location");
                                String phoneNumber = document.getString("phoneNumber");
                                userNameInLocation = document.getString("userName");


                                if (location != null && phoneNumber != null) {

                                    activeLocations.add(location + " - " + phoneNumber);
                                }
                            }


                            locationAdapter.setUserName(userNameInLocation);


                            locationAdapter.notifyDataSetChanged();
                        } else {
                            Log.w("CheckoutFragment", "No active locations found.");
                        }
                    });
        }
    }


    private void saveOrderToDatabase() {

        if (cUser != null) {
            String userId = cUser.getUid();


            db.collection("Users").document(userId)
                    .collection("Locations")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String userLocation = documentSnapshot.getString("location");
                            String phoneNumber = documentSnapshot.getString("phoneNumber");

                            if (userLocation == null || userLocation.isEmpty() ||
                                    phoneNumber == null || phoneNumber.isEmpty()) {
                                showAddressDialog();
                            } else {
                                proceedToSaveOrder();
                            }
                        } else {
                            Log.w("CheckoutFragment", "No documents found in Locations collection.");
                            showAddressDialog();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("CheckoutFragment", "Error fetching user location or phone number", e);
                    });
        } else {
            Log.w("CheckoutFragment", "Current user is null. Unable to retrieve location and phone number.");
        }
    }


    private void proceedToSaveOrder() {

        String dateFormatted = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()); // Example: 20241207
        String timeFormatted = new SimpleDateFormat("hhmm", Locale.getDefault()).format(new Date()); // Example: 1128 (12-hour format)
        String randomNum = String.format("%05d", new Random().nextInt(100000)); // 5 random digits, e.g., 00512


        String orderRefId = "REF-" + dateFormatted + timeFormatted + randomNum;


        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("referenceId", orderRefId);
        orderData.put("status", "placed");
        orderData.put("discount", discount.getText().toString());
        orderData.put("subtotal", subTotalTextView.getText().toString());
        orderData.put("totalPrice", totalCostTextView.getText().toString());


        String timestampFormatted = new SimpleDateFormat("MMMM dd, yyyy, h:mm a", Locale.getDefault()).format(new Date()); // Example: December 07, 2024, 11:28 AM
        orderData.put("timestamp", timestampFormatted);


        if (cUser != null) {
            orderData.put("userEmail", cUser.getEmail());
        } else {
            Log.w("CheckoutFragment", "Current user is null. Unable to retrieve email.");
        }


        List<HashMap<String, Object>> itemsList = new ArrayList<>();
        final String[] cakeImageUrl = {""};

        for (CartItemModel item : selectedItems) {
            HashMap<String, Object> itemData = new HashMap<>();
            itemData.put("productId", item.getProductId());
            itemData.put("productName", item.getProductName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("totalPrice", item.getTotalPrice());
            itemData.put("imageUrl", item.getImageUrl());
            itemData.put("cakeSize", item.getCakeSize());
            itemsList.add(itemData);


            if (!item.getImageUrl().isEmpty()) {
                cakeImageUrl[0] = item.getImageUrl();
            }
        }
        orderData.put("items", itemsList);


        db.collection("Users").document(cUser.getUid()).collection("Locations")
                .whereEqualTo("status", "Active")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String location = documentSnapshot.getString("location");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String userName = documentSnapshot.getString("userName");

                        if (location != null) orderData.put("location", location);
                        if (phoneNumber != null) orderData.put("phoneNumber", phoneNumber);
                        if (userName != null) orderData.put("userName", userName);


                        db.collection("Users").document("QqqccLchjigd0C7zf8ewPXY0KZc2").collection("Orders")
                                .document(orderRefId)
                                .set(orderData, SetOptions.merge())
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("CheckoutFragment", "Order placed successfully: " + orderRefId);
                                    clearCart();
                                    showSuccessDialog();


                                    sendOrderNotification(cakeImageUrl[0]);
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("CheckoutFragment", "Error placing order", e);
                                });
                    } else {
                        Log.w("CheckoutFragment", "No active locations found.");
                        showAddressDialog();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("CheckoutFragment", "Error fetching active location details", e);
                });
    }

    private void sendOrderNotification(String cakeImageUrl) {

        String adminEmail = "sweetkatrinabiancaignacio@gmail.com";


        NotificationViewModel newOrderNotification = new NotificationViewModel(
                "New Order Received",
                "A customer has placed a new order with you! Check the order items and get them ready to go!",
                cakeImageUrl
        );


        saveNotificationToAdmin(adminEmail, newOrderNotification);


        NotificationFragment notificationFragment = (NotificationFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(NotificationFragment.class.getSimpleName());

        if (notificationFragment != null) {
            notificationFragment.getNotificationList().add(newOrderNotification);
            notificationFragment.getNotificationAdapter().notifyDataSetChanged();
        }
    }


    private void saveNotificationToAdmin(String adminEmail, NotificationViewModel notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("Users")
                .whereEqualTo("email", adminEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {

                        String adminDocId = querySnapshot.getDocuments().get(0).getId();


                        db.collection("Users")
                                .document(adminDocId)
                                .collection("Notifications")
                                .add(new HashMap<String, Object>() {{
                                    put("orderStatus", notification.getOrderStatus());
                                    put("userRateComment", notification.getUserRateComment());
                                    put("imageUrl", notification.getCakeImageUrl());
                                    put("timestamp", FieldValue.serverTimestamp());
                                }})
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("Notification", "Notification saved successfully for admin: " + adminEmail);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Notification", "Error saving notification for admin", e);
                                });
                    } else {
                        Log.w("Notification", "Admin user not found with email: " + adminEmail);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Error fetching admin user", e);
                });
    }


    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.success_dialog, null);
        TextView btnContinue = dialogView.findViewById(R.id.btnContinue);
        TextView btnShopMore = dialogView.findViewById(R.id.btnShopMore);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        // Set the dialog background to transparent
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }


        alertDialog.setCancelable(false);

        btnContinue.setOnClickListener(v -> {
            alertDialog.dismiss();
            Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_checkoutFragment_to_homePageFragment);
        });

        btnShopMore.setOnClickListener(v -> {
            alertDialog.dismiss();
            Bundle args = new Bundle();
            args.putBoolean("loadShop", true);


            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_checkoutFragment_to_homePageFragment, args);
        });

        alertDialog.show();
    }


    private void showAddressDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.addressdialog, null);
        Button btnFinishSetup = dialogView.findViewById(R.id.btnFinishsetup);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();


        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnFinishSetup.setOnClickListener(v -> {
            alertDialog.dismiss();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_checkoutFragment_to_addressFragment);
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void updateTotals() {
        double subTotal = 0;
        String discountText = discount.getText().toString();

        double discountValue = 0;


        if (discountText != null && !discountText.isEmpty()) {

            if (discountText.endsWith("%")) {
                try {

                    double discountPercentage = Double.parseDouble(discountText.replace("%", ""));
                    discountValue = discountPercentage / 100;
                } catch (NumberFormatException e) {

                    Log.e("updateTotals", "Invalid discount format (percentage)");
                }
            }

            else if (discountText.startsWith("₱")) {
                try {

                    discountValue = Double.parseDouble(discountText.replace("₱", ""));
                } catch (NumberFormatException e) {
                    Log.e("updateTotals", "Invalid discount format (fixed amount)");
                }
            }
        }


        for (CartItemModel item : selectedItems) {
            subTotal += item.getTotalPriceAsDouble();
        }


        if (discountText.endsWith("%") && discountValue > 0) {
            discountValue *= subTotal;
        }


        double itemTotal = subTotal - discountValue;


        if (itemTotal < 0) {
            itemTotal = 0;
        }


        double totalCost = itemTotal;


        if (subTotalTextView != null) {
            subTotalTextView.setText("₱" + String.format("%.2f", subTotal));  // Display subtotal
        }

        if (itemTotalTextView != null) {
            itemTotalTextView.setText("₱" + String.format("%.2f", itemTotal));  // Display item total after discount
        }

        if (totalCostTextView != null) {
            totalCostTextView.setText("₱" + String.format("%.2f", totalCost));  // Display total cost after discount
        }


        toggleTextViewVisibility(!selectedItems.isEmpty());
    }



    private void toggleTextViewVisibility(boolean hasItems) {
        if (subTotalTextView != null) {
            subTotalTextView.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        }

        if (itemTotalTextView != null) {
            itemTotalTextView.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        }

        if (totalCostTextView != null) {
            totalCostTextView.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        }
    }

    private void clearCart() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            String currentUserId = currentUser.getUid();


            db.collection("Users")
                    .document(currentUserId)
                    .collection("cartItems")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {

                            for (CartItemModel item : selectedItems) {
                                String productIdToDelete = item.getProductId();
                                String cakeSizeToDelete = item.getCakeSize();
                                String productNameToDelete = item.getProductName();
                                String totalPriceToDelete = item.getTotalPrice();


                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    CartItemModel cartItem = document.toObject(CartItemModel.class);


                                    if (cartItem.getProductId().equals(productIdToDelete) &&
                                            cartItem.getCakeSize().equals(cakeSizeToDelete) &&
                                            cartItem.getProductName().equals(productNameToDelete) &&
                                            cartItem.getTotalPrice().equals(totalPriceToDelete)) {


                                        document.getReference().delete()
                                                .addOnSuccessListener(aVoid -> Log.d("CheckoutFragment", "Cart item deleted successfully"))
                                                .addOnFailureListener(e -> Log.w("CheckoutFragment", "Error deleting cart item", e));
                                    }
                                }
                            }
                        } else {
                            Log.w("CheckoutFragment", "No cart items found for the current user.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("CheckoutFragment", "Error fetching cart items", e);
                    });
        } else {
            Log.w("CheckoutFragment", "No current user is logged in.");
        }
    }
}