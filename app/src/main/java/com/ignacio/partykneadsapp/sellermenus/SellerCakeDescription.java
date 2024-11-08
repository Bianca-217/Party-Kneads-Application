package com.ignacio.partykneadsapp.sellermenus;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.ignacio.partykneadsapp.Cake_Description;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.CakeSizeModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;
import com.ignacio.partykneadsapp.model.SellerProductModel;

public class SellerCakeDescription extends Fragment {

    private TextView productName, productPrice, productDescription, ratePercent, numReviews;
    private ImageView productImage, btnBack;
    private TextView minusButton, quantityTextView, plusButton; // Quantity controls
    private Button btnDelete, btnEdit; // Add to Cart and Buy Now buttons
    private SellerProductModel productShopModel;
    private FirebaseFirestore firestore;
    private ListenerRegistration productListener;

    private int quantity = 1; // Initial quantity
    private CakeSizeModel selectedCakeSize; // Currently selected cake size

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_cake_description, container, false);

        firestore = FirebaseFirestore.getInstance();

        // Get the passed arguments
        Bundle args = getArguments();
        if (args != null) {
            productShopModel = (SellerProductModel) args.getSerializable("detailed");
        }

        // Initialize views
        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        productDescription = view.findViewById(R.id.productDescription);
        ratePercent = view.findViewById(R.id.ratePercent);
        numReviews = view.findViewById(R.id.numReviews);
        btnBack = view.findViewById(R.id.btnBack);
        btnDelete = view.findViewById(R.id.btnDelete); // Initialize Add to Cart button
        btnEdit = view.findViewById(R.id.btnEdit); // Initialize Buy Now button
        if (productShopModel != null) {
            loadProductDetails(productShopModel.getId());
        }
        // Set button click listener for "Add to Cart"
        btnDelete.setOnClickListener(v -> deleteItem());

        // Set button click listener for "Buy Now"
        btnEdit.setOnClickListener(v -> editItem());

        btnBack.setOnClickListener(v -> {
            // Replace the current fragment with the menu page fragment and pass the argument
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_sellerCakeDescription_to_myProductFragment);
        });
        return view;
    }

    public void deleteItem() {
        if (productShopModel != null && productShopModel.getId() != null) {
            // Get the product ID from the model
            String productId = productShopModel.getId();

            // Show a confirmation dialog before deleting with custom style
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
            builder.setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Reference to the Firestore document
                        firestore.collection("products").document(productId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();

                                    // Navigate back to the product list after deletion
                                    NavController navController = Navigation.findNavController(requireView());
                                    navController.navigate(R.id.action_sellerCakeDescription_to_myProductFragment);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to delete product", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Toast.makeText(getContext(), "Invalid product details", Toast.LENGTH_SHORT).show();
        }
    }


    public void editItem() {}


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
                    Glide.with(getContext()).load(imageUrl).into(productImage); // Load image with Glide
                    productName.setText(name);
                    productDescription.setText(description);
                    ratePercent.setText(rate);
                    numReviews.setText(numReviewsStr);
                    productShopModel.setimageUrl(imageUrl); // Save image URL to productShopModel
                    productPrice.setText("₱" + (price));
                }
            }
        });
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