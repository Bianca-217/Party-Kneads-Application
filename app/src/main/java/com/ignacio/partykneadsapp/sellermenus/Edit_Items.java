package com.ignacio.partykneadsapp.sellermenus;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
    private ConstraintLayout cl;
    private CardView progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditItemsBinding.inflate(inflater, container, false);

        progressDialog = (CardView) inflater.inflate(R.layout.progress_bar_saving, container, false);
        ((ViewGroup) binding.getRoot()).addView(progressDialog);  // Add it to the root view
        // Set the background of the progress dialog view to the drawable "dialog_pink_bg"
        progressDialog.setBackgroundResource(R.drawable.dialog_pink_bg);  // Ensure dialog_pink_bg is applied

// Set the layout parameters to center the progress bar
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        // Set horizontal margins for the progress dialog
        params.setMargins(60, 0, 60, 0); // 40dp horizontal margin (left and right)

        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

// Apply the parameters to the progress dialog
        progressDialog.setLayoutParams(params);

// Initially hide the progress dialog and dim the background
        progressDialog.setVisibility(View.GONE);

// Add the progress dialog to the root view and dim the background
        ViewGroup rootView = (ViewGroup) binding.getRoot();
        rootView.setAlpha(1f);  // Make sure the background is not dim initially


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

        // Set up the Clear All button
        binding.btnClearAll.setOnClickListener(v -> clearAllFields());

        // Set up the Save Changes button
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());

        // Set up the back button
        binding.btnBack.setOnClickListener(v -> showBackDialog());
    }

    private void showBackDialog() {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.close_dialog, null);

        // Create and customize the dialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set the background of the dialog to be transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set up the Cancel button
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        // Set up the Discard button
        dialogView.findViewById(R.id.btnDiscard).setOnClickListener(v -> {
            dialog.dismiss();
            // Navigate back to the homepage
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_edit_Items_to_seller_HomePageFragment);
        });

        // Show the dialog
        dialog.show();
    }


    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void clearAllFields() {
        // Clear all text fields
        binding.productName.setText("");
        binding.description.setText("");
        binding.productPrice.setText("");
        binding.categgories.setText("");

        // Reset the ImageView to a placeholder or remove the image
        binding.itemImg.setImageResource(R.drawable.placeholder); // Replace with your placeholder image resource

        // Clear the selected image URI
        selectedImageUri = null;

        // Show a toast message for user feedback
        Toast.makeText(getContext(), "All fields cleared!", Toast.LENGTH_SHORT).show();
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
        boolean isValid = true;

        if (updatedName.isEmpty()) {
            binding.productName.setError("Product name is required!");
            isValid = false;
        } else {
            binding.productName.setError(null); // Clear error if valid
        }

        if (updatedDescription.isEmpty()) {
            binding.description.setError("Description is required!");
            isValid = false;
        } else {
            binding.description.setError(null); // Clear error if valid
        }

        if (updatedPrice.isEmpty()) {
            binding.productPrice.setError("Price is required!");
            isValid = false;
        } else {
            binding.productPrice.setError(null); // Clear error if valid
        }

        if (updatedCategory.isEmpty()) {
            binding.categgories.setError("Category is required!");
            isValid = false;
        } else {
            binding.categgories.setError(null); // Clear error if valid
        }

        if (!isValid) {
            // If any field is invalid, stop further processing
            Toast.makeText(getContext(), "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog before saving and dim the background
        progressDialog.setVisibility(View.VISIBLE);
        getView().setAlpha(0.5f); // Dim the background when saving

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
        ).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
            progressDialog.setVisibility(View.GONE);  // Hide progress dialog on failure
        });
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
                        // Hide progress dialog and remove background dim
                        progressDialog.setVisibility(View.GONE);
                        getView().setAlpha(1f);  // Remove the dim effect

                        // Update UI to reflect the changes
                        binding.productName.setText(name);
                        binding.description.setText(description);
                        binding.productPrice.setText(price);
                        binding.categgories.setText(category);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(getContext()).load(imageUrl).into(binding.itemImg);
                        }

                        // Navigate back
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_edit_Items_to_myProductFragment);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update product", Toast.LENGTH_SHORT).show();
                        // Hide progress dialog and remove background dim
                        progressDialog.setVisibility(View.GONE);
                        getView().setAlpha(1f);  // Remove the dim effect
                    });
        }
    }
}

