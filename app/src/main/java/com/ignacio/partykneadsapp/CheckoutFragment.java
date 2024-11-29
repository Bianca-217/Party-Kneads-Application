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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cUser = mAuth.getCurrentUser();

        // Initialize UI elements
        btnBack = view.findViewById(R.id.btnBack);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        subTotalTextView = view.findViewById(R.id.subTotal);
        itemTotalTextView = view.findViewById(R.id.itemTotal);
        totalCostTextView = view.findViewById(R.id.totalPayment);
        txtUserName = view.findViewById(R.id.txtUserName);
        showVouchers = view.findViewById(R.id.showVouchers);
        discount = view.findViewById(R.id.discount);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Retrieve selected items from arguments if provided
        selectedItems = getArguments() != null ? getArguments().getParcelableArrayList("selectedItems") : new ArrayList<>();

        // Log received selected items for debugging
        if (selectedItems != null && !selectedItems.isEmpty()) {
            for (CartItemModel item : selectedItems) {
                Log.d("CheckoutFragment", "Received Item: " + item.getProductName() + ", Quantity: " + item.getQuantity());
            }
        } else {
            Log.d("CheckoutFragment", "No selected items received from CartFragment.");
        }

        // Set up the adapter with selected items
        coutAdapter = new CheckoutAdapter(selectedItems);
        recyclerView.setAdapter(coutAdapter);

        // Initialize RecyclerView for locations
        locationRecyclerView = view.findViewById(R.id.locationRecycler);
        activeLocations = new ArrayList<>();
        locationAdapter = new CheckoutLocationAdapter(activeLocations);
        locationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationRecyclerView.setAdapter(locationAdapter);

        // Fetch user name and locations
        fetchUserNameAndLocations();

        // Back button listener
        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_checkoutFragment_to_cartFragment);
        });

        showVouchers.setOnClickListener(v -> {
            ChooseVoucherFragment dialogFragment = new ChooseVoucherFragment();
            dialogFragment.setVoucherSelectedListener(selectedVoucher -> {
                // Handle the selected voucher
                discount.setText(selectedVoucher);
                updateTotals();
            });
            dialogFragment.show(requireActivity().getSupportFragmentManager(), "ChooseVoucherDialog");
        });

        // Checkout button listener
        Button btnCheckout = view.findViewById(R.id.btncheckout);
        btnCheckout.setOnClickListener(v -> {
            saveOrderToDatabase();
        });

        // Update totals based on selectedItems
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

                // Fetch active locations after getting the user's name
                fetchActiveLocations(userName);
            }).addOnFailureListener(e -> {
                Log.w("CheckoutFragment", "Error fetching user name", e);
            });
        }
    }

    private void fetchActiveLocations(String userName) {
        if (cUser != null) {
            String userId = cUser.getUid();

            // Use Firestore real-time listener (addSnapshotListener)
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
                            activeLocations.clear(); // Clear the list before adding new items

                            // Loop through each document in the snapshot
                            String userNameInLocation = "";
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String location = document.getString("location");
                                String phoneNumber = document.getString("phoneNumber");
                                userNameInLocation = document.getString("userName"); // Fetch the userName

                                // Check if location and phone number exist
                                if (location != null && phoneNumber != null) {
                                    // Combine location and phone number as a single string
                                    activeLocations.add(location + " - " + phoneNumber);
                                }
                            }

                            // Update the username in the adapter, if needed
                            locationAdapter.setUserName(userNameInLocation);

                            // Notify the adapter that data has been updated
                            locationAdapter.notifyDataSetChanged();
                        } else {
                            Log.w("CheckoutFragment", "No active locations found.");
                        }
                    });
        }
    }


    private void saveOrderToDatabase() {
        // Check for user's address and phone number first
        if (cUser != null) {
            String userId = cUser.getUid();

            // Access the Locations sub-collection within the Users collection
            db.collection("Users").document(userId)
                    .collection("Locations")
                    .limit(1) // Fetch the first document in the Locations collection
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
                            showAddressDialog(); // Show dialog if no document is found
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
        // Generate a random reference ID
        String orderRefId = "REF-" + System.currentTimeMillis();

        // Create a map to store order details
        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("referenceId", orderRefId);
        orderData.put("status", "placed");

        // Get the current user's email
        if (cUser != null) {
            orderData.put("userEmail", cUser.getEmail());
        } else {
            Log.w("CheckoutFragment", "Current user is null. Unable to retrieve email.");
        }

        // List to hold item details
        List<HashMap<String, Object>> itemsList = new ArrayList<>();
        final String[] cakeImageUrl = {""};  // Use an array to allow assignment within loop

        for (CartItemModel item : selectedItems) {
            HashMap<String, Object> itemData = new HashMap<>();
            itemData.put("productId", item.getProductId());
            itemData.put("productName", item.getProductName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("totalPrice", itemTotalTextView.getText().toString());
            itemData.put("imageUrl", item.getImageUrl());  // Add imageUrl to the item data
            itemData.put("cakeSize", item.getCakeSize());
            itemsList.add(itemData);

            // Get the cake image URL (assuming imageUrl is stored in CartItemModel)
            if (!item.getImageUrl().isEmpty()) {
                cakeImageUrl[0] = item.getImageUrl();  // Store the image URL for the notification
            }
        }
        orderData.put("items", itemsList);

        // Fetch location, phone number, and user name from the active location
        db.collection("Users").document(cUser.getUid()).collection("Locations")
                .whereEqualTo("status", "Active")
                .limit(1)  // Get only one active location
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

                        // Save order under the admin document's ID
                        String adminEmail = "sweetkatrinabiancaignacio@gmail.com";
                        db.collection("Users")
                                .whereEqualTo("email", adminEmail)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        String userDocId = task.getResult().getDocuments().get(0).getId();

                                        // Use document(orderRefId) to set the order data
                                        db.collection("Users").document(userDocId).collection("Orders")
                                                .document(orderRefId)  // Use the reference ID as the document ID
                                                .set(orderData, SetOptions.merge())  // Use merge to avoid overwriting
                                                .addOnSuccessListener(documentReference -> {
                                                    Log.d("CheckoutFragment", "Order placed successfully: " + orderRefId);
                                                    clearCart();
                                                    showSuccessDialog();  // Show success dialog after order placement

                                                    // Send notification after order is successfully placed
                                                    sendOrderNotification(cakeImageUrl[0]);  // Pass the cake image URL
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("CheckoutFragment", "Error placing order", e);
                                                });
                                    } else {
                                        Log.w("CheckoutFragment", "Admin user not found or no documents returned.");
                                    }
                                });
                    } else {
                        Log.w("CheckoutFragment", "No active locations found.");
                        showAddressDialog();  // Show dialog if no active location is found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("CheckoutFragment", "Error fetching active location details", e);
                });
    }

    private void sendOrderNotification(String cakeImageUrl) {
        // Admin's email address
        String adminEmail = "sweetkatrinabiancaignacio@gmail.com";

        // Create NotificationViewModel for the order
        NotificationViewModel newOrderNotification = new NotificationViewModel(
                "New Order Received",
                "A customer has placed a new order with you! Check the order items and get them ready to go!",
                cakeImageUrl
        );

        // Save the notification to the admin's Firestore Notifications subcollection
        saveNotificationToAdmin(adminEmail, newOrderNotification);

        // Optionally send it to the NotificationFragment (if necessary)
        NotificationFragment notificationFragment = (NotificationFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(NotificationFragment.class.getSimpleName());

        if (notificationFragment != null) {
            notificationFragment.getNotificationList().add(newOrderNotification);
            notificationFragment.getNotificationAdapter().notifyDataSetChanged();
        }
    }

    // Helper method to save the notification in Firestore for the admin
    private void saveNotificationToAdmin(String adminEmail, NotificationViewModel notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Save the notification in the "Notifications" subcollection under the admin's document
        db.collection("Users")
                .whereEqualTo("email", adminEmail)  // Find the admin document by email
                .limit(1)  // Limit to 1 result
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the admin's document ID
                        String adminDocId = querySnapshot.getDocuments().get(0).getId();

                        // Save the notification under the admin's "Notifications" subcollection
                        db.collection("Users")
                                .document(adminDocId)  // Admin's document ID
                                .collection("Notifications")  // Notifications subcollection
                                .add(new HashMap<String, Object>() {{
                                    put("orderStatus", notification.getOrderStatus());
                                    put("userRateComment", notification.getUserRateComment());
                                    put("imageUrl", notification.getCakeImageUrl());
                                    put("timestamp", FieldValue.serverTimestamp());  // Add a timestamp
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

        // Make sure the dialog is not dismissible by tapping outside
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

            // Replace the current fragment with the menu page fragment and pass the argument
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_checkoutFragment_to_homePageFragment, args);
        });

        alertDialog.show();
    }


    private void showAddressDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.addressdialog, null);
        Button btnFinishSetup = dialogView.findViewById(R.id.btnFinishsetup);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel); // Make sure this ID matches your layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        // Set the dialog background to transparent
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnFinishSetup.setOnClickListener(v -> {
            alertDialog.dismiss();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_checkoutFragment_to_addressFragment);
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss(); // Just dismiss the dialog
        });

        alertDialog.show();
    }

    private void updateTotals() {
        double subTotal = 0;   // This will hold the total price of items before discount
        String discountText = discount.getText().toString();  // Get discount as text from TextView

        double discountValue = 0;  // Variable to hold the discount value in numeric form

        // Check if the discount string is not null or empty
        if (discountText != null && !discountText.isEmpty()) {
            // If discount is in the format "20%" (percentage)
            if (discountText.endsWith("%")) {
                try {
                    // Extract the numeric part and convert to a percentage (e.g., "20%" -> 20)
                    double discountPercentage = Double.parseDouble(discountText.replace("%", ""));
                    discountValue = discountPercentage / 100; // Convert percentage to a decimal (e.g., 20% -> 0.20)
                } catch (NumberFormatException e) {
                    // Handle cases where the discount format is incorrect
                    Log.e("updateTotals", "Invalid discount format (percentage)");
                }
            }
            // If the discount is a fixed amount (e.g., "₱100")
            else if (discountText.startsWith("₱")) {
                try {
                    // Extract the numeric part and convert to a fixed value (e.g., "₱100" -> 100)
                    discountValue = Double.parseDouble(discountText.replace("₱", ""));
                } catch (NumberFormatException e) {
                    Log.e("updateTotals", "Invalid discount format (fixed amount)");
                }
            }
        }

        // Sum the total price of all selected items to get subTotal
        for (CartItemModel item : selectedItems) {
            subTotal += item.getTotalPriceAsDouble();  // Assuming getTotalPriceAsDouble() works correctly
        }

        // If the discount is a percentage, apply it to the subtotal
        if (discountText.endsWith("%") && discountValue > 0) {
            discountValue *= subTotal;  // Apply percentage discount to subtotal
        }

        // itemTotal is the total after applying the discount
        double itemTotal = subTotal - discountValue;

        // Ensure itemTotal is not negative, set it to 0 if negative
        if (itemTotal < 0) {
            itemTotal = 0;
        }

        // totalCost will be used for the total cost after discounts
        double totalCost = itemTotal;

        // Update the TextViews with the calculated totals
        if (subTotalTextView != null) {
            subTotalTextView.setText("₱" + String.format("%.2f", subTotal));  // Display subtotal
        }

        if (itemTotalTextView != null) {
            itemTotalTextView.setText("₱" + String.format("%.2f", itemTotal));  // Display item total after discount
        }

        if (totalCostTextView != null) {
            totalCostTextView.setText("₱" + String.format("%.2f", totalCost));  // Display total cost after discount
        }

        // Toggle TextView visibility depending on whether there are items in the cart
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
        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the current user's document ID
            String currentUserId = currentUser.getUid();

            // Reference to the cart items collection for the current user
            db.collection("Users")
                    .document(currentUserId)
                    .collection("cartItems")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Loop through the cart items and delete the ones that match the checked-out product IDs, cakeSize, productName, and totalPrice
                            for (CartItemModel item : selectedItems) {
                                String productIdToDelete = item.getProductId();
                                String cakeSizeToDelete = item.getCakeSize();  // Get the cakeSize from selected item
                                String productNameToDelete = item.getProductName();  // Get the productName from selected item
                                String totalPriceToDelete = item.getTotalPrice();  // Get the totalPrice from selected item

                                // Check if the productId, cakeSize, productName, and totalPrice match any in the cart
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    CartItemModel cartItem = document.toObject(CartItemModel.class);

                                    // Match the productId, cakeSize, productName, and totalPrice
                                    if (cartItem.getProductId().equals(productIdToDelete) &&
                                            cartItem.getCakeSize().equals(cakeSizeToDelete) &&
                                            cartItem.getProductName().equals(productNameToDelete) &&
                                            cartItem.getTotalPrice().equals(totalPriceToDelete)) {

                                        // Delete the matching cart item
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