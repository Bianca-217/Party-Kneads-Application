package com.ignacio.partykneadsapp.sellermenus;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentEditItemsBinding;
import java.util.HashMap;
import java.util.Map;

public class Edit_Items extends Fragment {

    private FragmentEditItemsBinding binding;
    private FirebaseFirestore firestore;
    private String productId, imageUrl, name, description, price, category;
    private final int GALLERY_REQ_CODE = 1000;
    private Uri selectedImageUri;
    ConstraintLayout cl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditItemsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        firestore = FirebaseFirestore.getInstance();

        // Set up the AutoCompleteTextView for categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Categories, android.R.layout.simple_dropdown_item_1line);
        binding.categgories.setAdapter(adapter);

        // Get data from bundle
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            imageUrl = getArguments().getString("imageUrl");
            name = getArguments().getString("name");
            description = getArguments().getString("description");
            price = getArguments().getString("price");
            category = getArguments().getString("category");

            // Display the details
            binding.productName.setText(name);
            binding.description.setText(description);
            binding.productPrice.setText(price);
            binding.categgories.setText(category);

            // Load the image using Glide
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(getContext()).load(imageUrl).into(binding.itemImg);
            }
        }

        // Set up the button for selecting a new image
        binding.btnUpload.setOnClickListener(v -> selectNewImage());

        // Set up the Save Changes button
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());

        // Set up the back button
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_edit_Items_to_seller_HomePageFragment);
        });
    }
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void selectNewImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            binding.itemImg.setImageURI(selectedImageUri);
        }
    }

    private void saveChanges() {
        // Get updated details from the fields
        String updatedName = binding.productName.getText().toString().trim();
        String updatedDescription = binding.description.getText().toString().trim();
        String updatedPrice = binding.productPrice.getText().toString().trim();
        String updatedCategory = binding.categgories.getText().toString().trim();

        // Validate inputs
        if (updatedName.isEmpty() || updatedDescription.isEmpty() || updatedPrice.isEmpty() || updatedCategory.isEmpty()) {
            Toast.makeText(getContext(), "All fields must be filled up!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri, updatedName, updatedDescription, updatedPrice, updatedCategory);
        } else {
            updateProductDetails(imageUrl, updatedName, updatedDescription, updatedPrice, updatedCategory);
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String name, String description, String price, String category) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("product_images").child(System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String newImageUrl = uri.toString();
                    updateProductDetails(newImageUrl, name, description, price, category);
                })
        ).addOnFailureListener(e -> Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show());
    }

    private void updateProductDetails(String imageUrl, String name, String description, String price, String category) {
        if (productId != null) {
            Map<String, Object> updatedProduct = new HashMap<>();
            updatedProduct.put("name", name);
            updatedProduct.put("description", description);
            updatedProduct.put("price", price);
            updatedProduct.put("categories", category);
            if (imageUrl != null) {
                updatedProduct.put("imageUrl", imageUrl);
            }

            firestore.collection("products").document(productId)
                    .update(updatedProduct)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_edit_Items_to_seller_HomePageFragment);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update product", Toast.LENGTH_SHORT).show());
        }
    }
}
