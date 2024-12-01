package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ignacio.partykneadsapp.databinding.FragmentCustomizeOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.ignacio.partykneadsapp.model.CartItemModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomizeOrderFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ConstraintLayout cl;
    private FragmentCustomizeOrderBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Price mapping for Butter Cream/Whipped Cream and Fondant
    private final Map<String, Integer> butterCreamPrices = new HashMap<String, Integer>() {{
        put("Bento Cake", 350);
        put("6-inch", 800);
        put("8-inch", 1200);
        put("9-inch", 1500);
        put("10-inch", 1800);
        put("2-Tier (6\" x 8\")", 3000);
        put("2-Tier (8\" x 10\")", 3500);
        put("3-Tier (6\" x 8\" x 10\")", 5000);
    }};

    private final Map<String, Integer> fondantPrices = new HashMap<String, Integer>() {{
        put("Bento Cake", 450);
        put("6-inch", 1000);
        put("8-inch", 1500);
        put("9-inch", 1800);
        put("10-inch", 2000);
        put("2-Tier (6\" x 8\")", 4000);
        put("2-Tier (8\" x 10\")", 4500);
        put("3-Tier (6\" x 8\" x 10\")", 6500);
    }};

    private Uri selectedImageUri;  // To store the selected image URI

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCustomizeOrderBinding.inflate(getLayoutInflater());
        db = FirebaseFirestore.getInstance();  // Initialize Firestore
        storage = FirebaseStorage.getInstance();  // Initialize Firebase Storage
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_customizeOrderFragment_to_homePageFragment, args1);
        });

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(this::hideKeyboard);

        binding.btnUpload.setOnClickListener(v -> openGallery());

        binding.btnAddToCart.setOnClickListener(v -> addToCart());
        binding.btnAddItem.setOnClickListener(v -> proceedToCheckout());

        setupAutoCompleteTextView();
        setupPriceUpdater();
    }

    private void setupAutoCompleteTextView() {
        String[] cakeTypes = getResources().getStringArray(R.array.Cake_Types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cakeTypes);
        binding.cakeType.setAdapter(adapter);

        String[] cakeSizes = getResources().getStringArray(R.array.Cake_Sizes);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cakeSizes);
        binding.cakeSizes.setAdapter(adapter2);
    }

    private void setupPriceUpdater() {
        binding.cakeType.setOnItemClickListener((parent, view, position, id) -> updatePrice());
        binding.cakeSizes.setOnItemClickListener((parent, view, position, id) -> updatePrice());
    }

    private void updatePrice() {
        String selectedCakeType = binding.cakeType.getText().toString();
        String selectedCakeSize = binding.cakeSizes.getText().toString();

        if (selectedCakeType.isEmpty() || selectedCakeSize.isEmpty()) {
            binding.itemPrice.setText("₱0.00");
            return;
        }

        int price = 0;
        if ("Butter Cream".equals(selectedCakeType) || "Whipped Cream".equals(selectedCakeType)) {
            price = butterCreamPrices.getOrDefault(selectedCakeSize, 0);
        } else if ("Fondant".equals(selectedCakeType)) {
            price = fondantPrices.getOrDefault(selectedCakeSize, 0);
        }

        binding.itemPrice.setText("₱" + price + ".00");
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                binding.itemImg.setImageURI(selectedImageUri);
            }
        }
    }

    // Inside your addToCart method
    private void addToCart() {
        String selectedCakeType = binding.cakeType.getText().toString();
        String selectedCakeSize = binding.cakeSizes.getText().toString();
        String theme = binding.theme.getText().toString();
        String note = binding.notes.getText().toString();
        int quantity = 1; // Default quantity
        String productSize = selectedCakeType + " " + selectedCakeSize;

        int totalPrice = Integer.parseInt(binding.itemPrice.getText().toString().replace("₱", "").replace(".00", ""));
        long timestamp = System.currentTimeMillis();

        // Get the current user's UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Upload image to Firebase Storage
        if (selectedImageUri != null) {
            StorageReference storageRef = storage.getReference().child("product_images/" + timestamp + ".jpg");
            storageRef.putFile(selectedImageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnCompleteListener(urlTask -> {
                        if (urlTask.isSuccessful()) {
                            String imageUrl = urlTask.getResult().toString();

                            // Prepare data to save in Firestore
                            Map<String, Object> cartItem = new HashMap<>();
                            cartItem.put("cakeSize", productSize);
                            cartItem.put("imageUrl", imageUrl);
                            cartItem.put("productId", "customized"); // Use the timestamp as the productId
                            cartItem.put("productName", theme);
                            cartItem.put("quantity", quantity);
                            cartItem.put("theme", theme);
                            cartItem.put("notes", note);
                            cartItem.put("timestamp", timestamp);
                            cartItem.put("totalPrice", "₱" + totalPrice);

                            // Save to Firestore under the user's UID
                            db.collection("Users")
                                    .document(uid)
                                    .collection("cartItems")
                                    .add(cartItem)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Item added to cart!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            clearFields();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void proceedToCheckout() {
        // Ensure required fields are filled
        String theme = binding.theme.getText().toString();
        String cakeSize = binding.cakeSizes.getText().toString();
        String imageUrl = ""; // Ensure `imageUrl` is correctly set after upload
        if (selectedImageUri != null) {
            imageUrl = selectedImageUri.toString();
        }

        if (theme.isEmpty() || cakeSize.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all details before proceeding to checkout.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total price
        int totalPrice = Integer.parseInt(binding.itemPrice.getText().toString().replace("₱", "").replace(".00", ""));
        int quantity = 1; // Default quantity

        // Create the CartItemModel for customized cake
        CartItemModel cartItem = new CartItemModel(
                "customized",      // Product ID
                theme,             // Product Name
                cakeSize,          // Cake Size
                quantity,          // Quantity
                "₱" + totalPrice,  // Total Price
                imageUrl           // Image URL
        );

        // Bundle the cart item to pass to CheckoutFragment
        ArrayList<CartItemModel> selectedItems = new ArrayList<>();
        selectedItems.add(cartItem);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("selectedItems", selectedItems);

        // Navigate to CheckoutFragment
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_customizeOrderFragment_to_checkoutFragment, bundle);
    }




    private void clearFields() {
        binding.cakeType.setText("");       // Clear cake type
        binding.cakeSizes.setText("");      // Clear cake size
        binding.itemPrice.setText("P0.00"); // Reset item price
        binding.notes.setText("");
        binding.theme.setText("");
        binding.itemImg.setImageResource(R.drawable.placeholder); // Clear selected image
        selectedImageUri = null;            // Reset selected image URI
    }
}
