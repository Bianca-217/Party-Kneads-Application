package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.CakeSizeAdapter;
import com.ignacio.partykneadsapp.model.CakeSizeModel;
import com.ignacio.partykneadsapp.model.CartItemModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;
import com.ignacio.partykneadsapp.model.AddToCartModel;

import java.util.ArrayList;
import java.util.List;

public class Cake_Description extends Fragment {

    private TextView productName, productPrice, productDescription, ratePercent, numReviews;
    private ImageView productImage, btnBack; // Added btnLike
    private TextView minusButton, quantityTextView, plusButton; // Quantity controls
    private Button btnAddtoCart, btnBuyNow, btnLike; // Add to Cart and Buy Now buttons
    private ProductShopModel productShopModel;
    private FirebaseFirestore firestore;
    private ListenerRegistration productListener;
    private ConstraintLayout cl;

    private int quantity = 1; // Initial quantity
    private CakeSizeModel selectedCakeSize; // Currently selected cake size


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cake__description, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get the passed arguments
        Bundle args = getArguments();
        if (args != null) {
            productShopModel = (ProductShopModel) args.getSerializable("detailed");
        }

        // Initialize views
        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        productDescription = view.findViewById(R.id.productDescription);
        btnBack = view.findViewById(R.id.btnBack);
        btnLike = view.findViewById(R.id.btnLike); // Initialize btnLike
        // Initialize quantity controls
        minusButton = view.findViewById(R.id.minus);
        quantityTextView = view.findViewById(R.id.quantity);
        plusButton = view.findViewById(R.id.plus);
        btnAddtoCart = view.findViewById(R.id.btnAddtoCart); // Initialize Add to Cart button
        btnBuyNow = view.findViewById(R.id.btnBuyNow); // Initialize Buy Now button

        // Set button click listener for "Add to Cart"
        btnAddtoCart.setOnClickListener(v -> showAddToCartDialog());

        // Set button click listener for "Buy Now"
        btnBuyNow.setOnClickListener(v -> handleBuyNow());

        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityAndPrice();
            }
        });

        plusButton.setOnClickListener(v -> {
            quantity++;
            updateQuantityAndPrice();
        });

        if (productShopModel != null) {
            loadProductDetails(productShopModel.getId());
        }

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.cakeSizes);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Prepare cake sizes data
        List<CakeSizeModel> cakeSizes = new ArrayList<>();
        cakeSizes.add(new CakeSizeModel("Bento Cake", "Serves 1 - 2", "₱280"));
        cakeSizes.add(new CakeSizeModel("6'' Cake", "Serves 6 - 8", "₱420"));
        cakeSizes.add(new CakeSizeModel("8'' Cake", "Serves 10 - 12", "₱600"));
        cakeSizes.add(new CakeSizeModel("9'' Cake", "Serves 12 - 16", "₱850"));
        cakeSizes.add(new CakeSizeModel("10'' Cake", "Serves 16 - 20", "₱1000"));

        // Set the adapter with the listener
        CakeSizeAdapter adapter = new CakeSizeAdapter(cakeSizes, cakeSize -> {
            selectedCakeSize = cakeSize; // Update selected cake size
            updateQuantityAndPrice(); // Update price based on the new selection
        });
        recyclerView.setAdapter(adapter);

        // Set default selection to Bento Cake
        selectedCakeSize = cakeSizes.get(0); // Bento Cake
        updateQuantityAndPrice(); // Initialize price display

        btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);

            // Replace the current fragment with the menu page fragment and pass the argument
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_cake_Description_to_homePageFragment, args1);
        });

        // Set the like button listener
        btnLike.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

            if (userId != null) {
                // Save liked product to Firestore under the user's Likes sub-collection
                saveProductToLikes(userId);
            } else {
                Toast.makeText(getContext(), "Please log in to like products.", Toast.LENGTH_SHORT).show();
            }
        });

        return view; // Return the inflated view
    }

    private void saveProductToLikes(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Save the liked product inside the "Likes" collection of the user
        db.collection("Users")
                .document(userId)
                .collection("Likes") // Likes sub-collection inside the user's document
                .document(productShopModel.getId()) // Use the product ID as the document ID
                .set(productShopModel) // Save the product details
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Product liked!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to like the product.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddToCartDialog() {
        // Create a dialog
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.addtocartdialog); // Inflate your dialog layout
        dialog.setCancelable(true);

        // Make the dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Reference to FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Automatically add item to the cart when the dialog is shown
        if (userId != null) {
            saveCartItem(userId); // Method to add to cart
        } else {
            Toast.makeText(getActivity(), "Please log in to add items to the cart.", Toast.LENGTH_SHORT).show();
        }

        Button btnCart = dialog.findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
            goToCart(); // Navigate to the cart fragment
        });

        // Use findViewById to get the TextView and set an OnClickListener
        TextView btnContinue = dialog.findViewById(R.id.btnContinue); // Keep it as TextView
        btnContinue.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
            goToShop(); // Navigate back to the shop fragment
        });

        // Show the dialog
        dialog.show();
    }

    private void saveCartItem(String userId) {
        String imageUrl = productShopModel.getimageUrl(); // Ensure this accesses the image URL correctly
        long timestamp = System.currentTimeMillis(); // Get the current timestamp
        String productName = productShopModel.getName();
        String cakeSize = selectedCakeSize.getSize();

        // Check if the product and cake size already exist in the user's cart
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(userId)
                .collection("cartItems")
                .whereEqualTo("productName", productName)
                .whereEqualTo("cakeSize", cakeSize)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Item exists, update the quantity and price
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve the existing cart item document
                            String docId = document.getId();
                            int existingQuantity = document.getLong("quantity").intValue();
                            String existingPrice = document.getString("totalPrice");

                            // Calculate new quantity and price
                            int newQuantity = existingQuantity + quantity;
                            String newPrice = calculateNewPrice(existingPrice, productPrice.getText().toString(), existingQuantity, newQuantity);

                            // Debugging: Log the new quantity and total price
                            Log.d("CartItem", "New Quantity: " + newQuantity);
                            Log.d("CartItem", "New Price: " + newPrice);

                            // Update the cart item with new quantity and price
                            db.collection("Users")
                                    .document(userId)
                                    .collection("cartItems")
                                    .document(docId)
                                    .update("quantity", newQuantity, "totalPrice", newPrice)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully updated the cart item
                                        Toast.makeText(getActivity(), "Cart item updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Toast.makeText(getActivity(), "Failed to update cart item.", Toast.LENGTH_SHORT).show();
                                        Log.e("CartItem", "Error updating cart item: ", e);
                                    });
                        }
                    } else {
                        // Item doesn't exist, create a new cart item
                        AddToCartModel cartItem = new AddToCartModel(
                                productShopModel.getId(),
                                productName,
                                cakeSize,
                                quantity,
                                productPrice.getText().toString(),
                                imageUrl,
                                timestamp // Pass the timestamp here
                        );

                        // Save the new cart item to Firestore
                        db.collection("Users")
                                .document(userId)
                                .collection("cartItems")
                                .add(cartItem)
                                .addOnSuccessListener(documentReference -> {
                                    // Successfully added to cart
                                    Toast.makeText(getActivity(), "Item added to cart", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    Toast.makeText(getActivity(), "Failed to add item to cart.", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // Helper function to calculate the new price based on quantity
    private String calculateNewPrice(String existingTotalPrice, String newPrice, int existingQuantity, int newQuantity) {
        // Check if existingTotalPrice is null or empty, and handle the case appropriately
        if (existingTotalPrice == null || existingTotalPrice.isEmpty()) {
            existingTotalPrice = "₱0"; // Set to a default price, or handle it differently based on your needs
        }

        // Remove the "₱" symbol and convert the price to a float
        float existingTotalPriceValue = Float.parseFloat(existingTotalPrice.replace("₱", "").trim());
        float newPriceValue = Float.parseFloat(newPrice.replace("₱", "").trim());

        // Calculate the total price by adding the price of the new quantity to the existing total
        float newTotalPrice = existingTotalPriceValue + (newPriceValue * quantity);

        // Debugging: Log the total price calculation
        Log.d("CartItem", "Existing Total Price: " + existingTotalPriceValue);
        Log.d("CartItem", "New Price Value: " + newPriceValue);
        Log.d("CartItem", "New Total Price: " + newTotalPrice);

        // Return the formatted price with "₱" symbol
        return "₱" + String.format("%.2f", newTotalPrice);
    }

    private void handleBuyNow() {
        // Create a CartItemModel for the selected cake and quantity
        String size = selectedCakeSize.getSize();
        int price = Integer.parseInt(selectedCakeSize.getPrice().replace("₱", ""));
        int totalPrice = price * quantity;

        // Create a CartItemModel for this selection
        CartItemModel cartItem = new CartItemModel(
                productShopModel.getId(),
                productShopModel.getName(),
                size,
                quantity,
                "₱" + totalPrice, // Include price with quantity
                productShopModel.getimageUrl() // Product Image URL
        );

        // Bundle the cart item to pass to the CheckoutFragment
        List<CartItemModel> selectedItems = new ArrayList<>();
        selectedItems.add(cartItem);

        // Log the details of the cart item
        Log.d("CartItem", "Item: " + cartItem.getProductName() + ", Quantity: " + cartItem.getQuantity() + ", Total Price: " + cartItem.getTotalPrice());

        // Create a Bundle to pass the selected items to CheckoutFragment
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("selectedItems", (ArrayList<? extends Parcelable>) selectedItems);

        // Navigate to CheckoutFragment and pass the selected items
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_cake_Description_to_checkoutFragment, bundle);
    }

    private void updateQuantityAndPrice() {
        quantityTextView.setText(String.valueOf(quantity));
        if (selectedCakeSize != null) {
            int price = Integer.parseInt(selectedCakeSize.getPrice().replace("₱", ""));
            int totalPrice = price * quantity;
            productPrice.setText("₱" + totalPrice); // Update displayed price
        }
        // Enable or disable the minus button based on quantity
        minusButton.setEnabled(quantity > 1);
    }

    private void loadProductDetails(String productId) {
        DocumentReference productRef = firestore.collection("products").document(productId);

        productListener = productRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle the error
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Fetch and display product details
                    String imageUrl = documentSnapshot.getString("imageUrl"); // Fetch the imageUrl
                    String name = documentSnapshot.getString("name");
                    String description = documentSnapshot.getString("description");
                    String rate = documentSnapshot.getString("rate");
                    String numReviewsStr = documentSnapshot.getString("numreviews");

                    // Populate the views with data
                    Glide.with(Cake_Description.this).load(imageUrl).into(productImage); // Load image with Glide
                    productName.setText(name);
                    productDescription.setText(description);
                    productShopModel.setimageUrl(imageUrl); // Save image URL to productShopModel
                }
            }
        });
    }



    private void goToCart() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_cake_Description_to_cartFragment);
    }

    private void goToShop() {

        // Create a bundle with the flag to load ShopFragment
        Bundle args = new Bundle();
        args.putBoolean("loadShop", true);

        // Replace the current fragment with the menu page fragment and pass the argument
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_cake_Description_to_homePageFragment, args);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the listener to prevent memory leaks
        if (productListener != null) {
            productListener.remove();
        }
    }
}