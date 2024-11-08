package com.ignacio.partykneadsapp;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.ignacio.partykneadsapp.adapters.BalloonColorAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentBalloonClassicDescriptionBinding;
import com.ignacio.partykneadsapp.databinding.FragmentShopBinding;
import com.ignacio.partykneadsapp.model.AddToCartModel;
import com.ignacio.partykneadsapp.model.BalloonColorModel;
import com.ignacio.partykneadsapp.model.CartItemModel;
import com.ignacio.partykneadsapp.model.CategoriesModel;
import com.ignacio.partykneadsapp.model.CupcakeModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;

import java.util.ArrayList;
import java.util.List;


public class BalloonClassicDescription extends Fragment {
    private TextView productName, productPrice, productDescription, ratePercent, numReviews;
    private ImageView productImage, btnBack;
    private TextView quantityTextView;
    private Button btnAddtoCart, btnBuyNow; // Add to Cart and Buy Now buttons
    private ProductShopModel productShopModel;
    private FirebaseFirestore firestore;
    private ListenerRegistration productListener;
    private String color = "";
    private int quantity = 1; // Initial quantity

    FragmentBalloonClassicDescriptionBinding binding;
    private List<CategoriesModel> categoriesModelList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBalloonClassicDescriptionBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        ratePercent = view.findViewById(R.id.ratePercent);
        numReviews = view.findViewById(R.id.numReviews);
        btnBack = view.findViewById(R.id.btnBack);
        // Initialize quantity controls
        btnAddtoCart = view.findViewById(R.id.btnAddtoCart); // Initialize Add to Cart button
        btnBuyNow = view.findViewById(R.id.btnBuyNow); // Initialize Buy Now button

        // Set button click listener for "Add to Cart"
        btnAddtoCart.setOnClickListener(v -> showAddToCartDialog());

        // Set button click listener for "Buy Now"
        btnBuyNow.setOnClickListener(v -> handleBuyNow());

        if (productShopModel != null) {
            loadProductDetails(productShopModel.getId());
        }


        setupBalloonColors();

        btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);

            // Replace the current fragment with the menu page fragment and pass the argument
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_balloonClassicDescription_to_homePageFragment, args1);
        });

    }

    private void setupBalloonColors() {
        RecyclerView balloonColorsRecyclerView = binding.balloonColors;
        List<BalloonColorModel> balloonColorList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        balloonColorList.add(new BalloonColorModel(R.drawable.assorter, "Assorted"));
        balloonColorList.add(new BalloonColorModel(R.drawable.blue_balloon, "Blue"));
        balloonColorList.add(new BalloonColorModel(R.drawable.darkpink_balloon, "Dark Pink"));
        balloonColorList.add(new BalloonColorModel(R.drawable.green_balloon, "Green"));
        balloonColorList.add(new BalloonColorModel(R.drawable.pink_balloon, "Pink"));
        balloonColorList.add(new BalloonColorModel(R.drawable.yellow_balloon, "Yellow"));
        balloonColorList.add(new BalloonColorModel(R.drawable.white_balloon, "White"));
        balloonColorList.add(new BalloonColorModel(R.drawable.purple_balloon, "Purple"));
        balloonColorList.add(new BalloonColorModel(R.drawable.red_balloon, "Red"));

        // Initialize adapter and layout manager for balloon colors
        BalloonColorAdapter balloonColorAdapter = new BalloonColorAdapter(requireActivity(), balloonColorList, colorSelected -> {
            color = colorSelected;
        });

        balloonColorsRecyclerView.setAdapter(balloonColorAdapter);
        balloonColorsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        balloonColorsRecyclerView.setHasFixedSize(true);
        balloonColorsRecyclerView.setNestedScrollingEnabled(false);
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
        // Create a new AddToCartModel with imageUrl and timestamp
        String imageUrl = productShopModel.getimageUrl(); // Ensure this accesses the image URL correctly

        long timestamp = System.currentTimeMillis(); // Get the current timestamp

        AddToCartModel cartItem = new AddToCartModel(
                productShopModel.getId(),
                productShopModel.getName(),
                color,
                quantity,
                productPrice.getText().toString(),
                imageUrl,
                timestamp // Pass the timestamp here
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userId).collection("cartItems").add(cartItem)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added to cart
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(getActivity(), "Failed to add item to cart.", Toast.LENGTH_SHORT).show();
                });
    }


    private void handleBuyNow() {
        // Create a CartItemModel for the selected cake and quantity
        int price = Integer.parseInt(productPrice.getText().toString());
        int totalPrice = price * quantity;

        // Create a CartItemModel for this selection
        CartItemModel cartItem = new CartItemModel(
                productShopModel.getId(),
                productShopModel.getName(),
                color,
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
        navController.navigate(R.id.action_balloonClassicDescription_to_checkoutFragment, bundle);
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
                    String price = documentSnapshot.getString("price");

                    // Populate the views with data
                    Glide.with(BalloonClassicDescription.this).load(imageUrl).into(productImage); // Load image with Glide
                    productName.setText(name);
                    productDescription.setText(description);
                    ratePercent.setText(rate);
                    numReviews.setText(numReviewsStr);
                    productPrice.setText("₱" +  price);
                    productShopModel.setimageUrl(imageUrl); // Save image URL to productShopModel
                }
            }
        });
    }

    private void goToCart() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_balloonClassicDescription_to_cartFragment);
    }

    private void goToShop() {

        // Create a bundle with the flag to load ShopFragment
        Bundle args = new Bundle();
        args.putBoolean("loadShop", true);

        // Replace the current fragment with the menu page fragment and pass the argument
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_balloonClassicDescription_to_homePageFragment, args);
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