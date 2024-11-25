package com.ignacio.partykneadsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.BalloonColorAdapter;
import com.ignacio.partykneadsapp.adapters.PartyHatsAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentPartyHatsDescriptionBinding;
import com.ignacio.partykneadsapp.model.AddToCartModel;
import com.ignacio.partykneadsapp.model.BalloonColorModel;
import com.ignacio.partykneadsapp.model.CartItemModel;
import com.ignacio.partykneadsapp.model.CategoriesModel;
import com.ignacio.partykneadsapp.model.PartyHatsModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;

import java.util.ArrayList;
import java.util.List;


public class PartyHatsDescription extends Fragment {
    private TextView productName, productPrice, productDescription, ratePercent, numReviews;
    private ImageView productImage, btnBack;
    private TextView quantityTextView;
    private Button btnAddtoCart, btnBuyNow; // Add to Cart and Buy Now buttons
    private ProductShopModel productShopModel;
    private FirebaseFirestore firestore;
    private ListenerRegistration productListener;
    private String color;
    private int quantity = 1; // Initial quantity

    FragmentPartyHatsDescriptionBinding binding;
    private List<CategoriesModel> categoriesModelList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPartyHatsDescriptionBinding.inflate(getLayoutInflater());
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
        btnAddtoCart.setOnClickListener(v -> showAddToCartDialog("", 1));

        // Set button click listener for "Buy Now"
        btnBuyNow.setOnClickListener(v -> handleBuyNow("", 1));


        if (productShopModel != null) {
            loadProductDetails(productShopModel.getId());
        }

        if (productShopModel.getCategory().equals("Party Hats - Cartoon")) {
            color = "Barbie";
            setupCartoonTheme();
        } else if (productShopModel.getCategory().equals("Party Hats - Pompoms")) {
            color = "Princess";
            setupPompomsTheme();
        } else if (productShopModel.getCategory().equals("Party Hats - Balloon Headress")) {
            color = "Kuromi";
            setupHeaddressTheme();
        }

        btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);

            // Replace the current fragment with the menu page fragment and pass the argument
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_partyHatsDescription_to_homePageFragment, args1);
        });
    }

    // Define the method for adding to the cart with selected letter and quantity
    private void addToCartFunctionality(String selectedLetter, int quantityLetter) {
        // Example logic for adding the item to the cart
        showAddToCartDialog(selectedLetter, quantityLetter);
        // Implement your actual add-to-cart logic here
    }

    private void setupCartoonTheme() {
        RecyclerView partyHatsTheme = binding.partyHatsTheme;
        List<PartyHatsModel> partyHatsModelList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        partyHatsModelList.add(new PartyHatsModel(R.drawable.barbie, "Barbie"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.minions, "Minions"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.avengers, "Avengers"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.bossbaby, "Boss Baby"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.cocomelon, "Cocomelon"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.pepa, "Pepa Pig"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.princess, "Princess"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.shark, "Baby Shark"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.superman, "Superman"));

        // Initialize adapter and layout manager for balloon colors
        PartyHatsAdapter partyHatsAdapter = new PartyHatsAdapter(requireActivity(), partyHatsModelList, colorSelected -> {
            color = colorSelected;
        });

        partyHatsTheme.setAdapter(partyHatsAdapter);
        partyHatsTheme.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        partyHatsTheme.setHasFixedSize(true);
        partyHatsTheme.setNestedScrollingEnabled(false);
    }

    private void setupPompomsTheme() {
        RecyclerView partyHatsTheme = binding.partyHatsTheme;
        List<PartyHatsModel> partyHatsModelList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        partyHatsModelList.add(new PartyHatsModel(R.drawable.princess_hat, "Princess"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.prince, "Prince"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.bday_girl, "Birthday Girl"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.bday_boy, "Birthday Boy"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.blue_gold, "Blue Gold"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.pink_gold, "Pink Gold"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.colorful_blue, "Colorful Blue"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.colorful_pink, "Colorful Pink"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.heart_hat, "Heart"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.black_hat, "Black Polka"));

        // Initialize adapter and layout manager for balloon colors
        PartyHatsAdapter partyHatsAdapter = new PartyHatsAdapter(requireActivity(), partyHatsModelList, colorSelected -> {
            color = colorSelected;
        });

        partyHatsTheme.setAdapter(partyHatsAdapter);
        partyHatsTheme.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        partyHatsTheme.setHasFixedSize(true);
        partyHatsTheme.setNestedScrollingEnabled(false);
    }

    private void setupHeaddressTheme() {
        RecyclerView partyHatsTheme = binding.partyHatsTheme;
        List<PartyHatsModel> partyHatsModelList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        partyHatsModelList.add(new PartyHatsModel(R.drawable.kuromi, "Kuromi"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.kuromi_pink, "Kuromi Pink"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.christmashat, "Christmas"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.snowman, "Snow Man"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.mickey, "Mickey"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.minnie, "Minnie"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.princess_balloon, "Princess"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.cat, "Cat"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.dog, "Dog"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.cow, "Cow"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.elephant, "Elephant"));
        partyHatsModelList.add(new PartyHatsModel(R.drawable.monkey, "Monkey"));

        // Initialize adapter and layout manager for balloon colors
        PartyHatsAdapter partyHatsAdapter = new PartyHatsAdapter(requireActivity(), partyHatsModelList, colorSelected -> {
            color = colorSelected;
        });

        partyHatsTheme.setAdapter(partyHatsAdapter);
        partyHatsTheme.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        partyHatsTheme.setHasFixedSize(true);
        partyHatsTheme.setNestedScrollingEnabled(false);
    }


    private void showAddToCartDialog(String selectedLetter, int quantityLetter) {
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
            saveCartItem(userId, selectedLetter, quantityLetter); // Method to add to cart
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

    private void saveCartItem(String userId, String selectedLetter, int quantityLetter) {
        // Extract the product details
        String priceText = productPrice.getText().toString();
        int unitPrice = Integer.parseInt(priceText.replaceAll("[^\\d]", ""));
        int totalPrice = unitPrice * quantityLetter;
        String imageUrl = productShopModel.getimageUrl();
        long timestamp = System.currentTimeMillis();
        String productName = productShopModel.getName();
        String uniqueKey = color + selectedLetter; // Create a unique key for color and letter combination

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if the product with the same color and letter already exists in the cart
        db.collection("Users")
                .document(userId)
                .collection("cartItems")
                .whereEqualTo("productName", productName)
                .whereEqualTo("cakeSize", uniqueKey)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Item exists, update the quantity and totalPrice
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String docId = document.getId();
                            int existingQuantity = document.getLong("quantity").intValue();
                            String existingTotalPriceStr = document.getString("totalPrice");

                            // Calculate the new quantity and total price
                            int newQuantity = existingQuantity + quantityLetter;
                            int existingTotalPrice = Integer.parseInt(existingTotalPriceStr.replaceAll("[^\\d]", ""));
                            int newTotalPrice = existingTotalPrice + (unitPrice * quantityLetter);

                            // Update the existing cart item
                            db.collection("Users")
                                    .document(userId)
                                    .collection("cartItems")
                                    .document(docId)
                                    .update("quantity", newQuantity, "totalPrice", "₱" + newTotalPrice)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully updated the cart item
                                        Toast.makeText(getActivity(), "Cart item updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Failed to update cart item.", Toast.LENGTH_SHORT).show();
                                        Log.e("CartItem", "Error updating cart item: ", e);
                                    });
                        }
                    } else {
                        // Item doesn't exist, create a new cart item
                        AddToCartModel cartItem = new AddToCartModel(
                                productShopModel.getId(),
                                productName,
                                uniqueKey,
                                quantityLetter,
                                "₱" + totalPrice,
                                imageUrl,
                                timestamp
                        );

                        // Add the new item to the cart
                        db.collection("Users")
                                .document(userId)
                                .collection("cartItems")
                                .add(cartItem)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getActivity(), "Item added to cart", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Failed to add item to cart.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartItem", "Error fetching cart items: ", e);
                });
    }



    private void handleBuyNow(String selectedNumLetter, int quantityNumLetter) {
        // Create a CartItemModel for the selected cake and quantity
        int price = Integer.parseInt(productPrice.getText().toString().replace("₱", ""));
        int totalPrice = price * quantityNumLetter;

        // Create a CartItemModel for this selection
        CartItemModel cartItem = new CartItemModel(
                productShopModel.getId(),
                productShopModel.getName(),
                color + selectedNumLetter,
                quantityNumLetter,
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
        navController.navigate(R.id.action_partyHatsDescription_to_checkoutFragment, bundle);
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
                    Glide.with(PartyHatsDescription.this).load(imageUrl).into(productImage); // Load image with Glide
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
        navController.navigate(R.id.action_partyHatsDescription_to_cartFragment);
    }

    private void goToShop() {

        // Create a bundle with the flag to load ShopFragment
        Bundle args = new Bundle();
        args.putBoolean("loadShop", true);

        // Replace the current fragment with the menu page fragment and pass the argument
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_partyHatsDescription_to_homePageFragment, args);
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