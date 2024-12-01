package com.ignacio.partykneadsapp.sellermenus;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentAddItemsBinding;

import java.util.HashMap;
import java.util.Map;

public class Add_Items extends Fragment {

    FragmentAddItemsBinding binding;
    ConstraintLayout cl;
    final int GALLERY_REQ_CODE = 1000;
    Uri selectedImageUri;
    View progressDialogView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddItemsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        binding.btnUpload.setOnClickListener(v -> uploadImage());

        // Set up the Clear All button
        binding.btnClearAll.setOnClickListener(v -> clearAllFields());

        // Set up the AutoCompleteTextView for categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Categories, android.R.layout.simple_dropdown_item_1line);
        binding.categgories.setAdapter(adapter);

        binding.btnAddItem.setOnClickListener(v -> {
            String productName = binding.productName.getText().toString().trim();
            String productPrice = binding.productPrice.getText().toString().trim();
            String productDescription = binding.description.getText().toString().trim();
            String productCategory = binding.categgories.getText().toString().trim();

            boolean hasError = false;

            // Check if Product Name is empty
            if (productName.isEmpty()) {
                binding.productName.setError("Product name is required!");
                hasError = true;
            } else {
                binding.productName.setError(null); // Clear any previous error
            }

            // Check if Product Price is empty
            if (productPrice.isEmpty()) {
                binding.productPrice.setError("Product price is required!");
                hasError = true;
            } else {
                binding.productPrice.setError(null); // Clear any previous error
            }

            // Check if Product Description is empty
            if (productDescription.isEmpty()) {
                binding.description.setError("Product description is required!");
                hasError = true;
            } else {
                binding.description.setError(null); // Clear any previous error
            }

            // Check if Product Category is empty
            if (productCategory.isEmpty()) {
                binding.categgories.setError("Product category is required!");
                hasError = true;
            } else {
                binding.categgories.setError(null); // Clear any previous error
            }

            // Check if an image is selected
            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "An image must be uploaded!", Toast.LENGTH_SHORT).show();
                hasError = true;
            }

            // Proceed only if there are no errors
            if (!hasError) {
                String productId = String.valueOf(System.currentTimeMillis()); // Use current time as unique ID
                showProgressDialog(); // Show progress dialog when uploading
                uploadImageToFirebase(selectedImageUri, productId);
            }
        });


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

        // Set up the Cancel buttonadd

        // Set up the Discard button
        dialogView.findViewById(R.id.btnDiscard).setOnClickListener(v -> {
            dialog.dismiss();
            // Navigate back to the homepage
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_add_Items_to_seller_HomePageFragment);
        });

        // Show the dialog
        dialog.show();
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

    private void showProgressDialog() {
        // Inflate the progress dialog layout and show it
        LayoutInflater inflater = LayoutInflater.from(getContext());
        progressDialogView = inflater.inflate(R.layout.progress_bar, null);

        // Set the background of the progress dialog view to the drawable "dialog_pink_bg"
        progressDialogView.setBackgroundResource(R.drawable.dialog_pink_bg);  // Ensure dialog_pink_bg is applied

        // Create a dimming overlay (a transparent view with a semi-transparent black background)
        View dimOverlay = new View(getContext());
        dimOverlay.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dimmer_color)); // Set a dim color (e.g., semi-transparent black)
        dimOverlay.setTag("dimOverlay");  // Set a tag to identify the dim overlay

        // Assuming the parent layout is a ConstraintLayout or another type that can hold this dialog
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  // Match parent width
                ConstraintLayout.LayoutParams.MATCH_PARENT // Match parent height (covers the entire screen)
        );

        // Apply the layout parameters for the dimming overlay (cover the whole screen)
        dimOverlay.setLayoutParams(layoutParams);

        // Add the dimming overlay view to the parent layout (ConstraintLayout)
        cl.addView(dimOverlay);

        // Set up the progress dialog layout parameters
        ConstraintLayout.LayoutParams progressLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  // Match parent width
                ConstraintLayout.LayoutParams.WRAP_CONTENT // Wrap content height
        );

        // Set horizontal margins for the progress dialog
        progressLayoutParams.setMargins(60, 0, 60, 0); // 40dp horizontal margin (left and right)

        // Center the progress dialog in the parent layout
        progressLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        progressLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        progressLayoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        progressLayoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;

        // Apply layout params to the progress dialog view
        progressDialogView.setLayoutParams(progressLayoutParams);

        // Add the progress dialog view to the parent layout (ConstraintLayout)
        cl.addView(progressDialogView);
    }




    private void dismissProgressDialog() {
        if (progressDialogView != null) {
            cl.removeView(progressDialogView);  // Remove the progress dialog view
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void uploadImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            // Display the selected image in an ImageView
            binding.itemImg.setImageURI(selectedImageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri, String productId) {
        // Create a reference to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance("gs://party-kneads-firebase.appspot.com").getReference()
                .child("product_images").child(System.currentTimeMillis() + ".jpg");

        // Upload the file
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Get the download URL of the image
                String imageUrl = uri.toString();

                // Save product info and image URL to Firestore
                saveProductDataToFirebase(imageUrl);
            });
        }).addOnFailureListener(e -> {
            // Handle any errors
            dismissProgressDialog(); // Dismiss the dialog in case of failure
            Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProductDataToFirebase(@Nullable String imageUrl) {
        // Retrieve the entered text from input fields
        String productName = binding.productName.getText().toString();
        String productPrice = binding.productPrice.getText().toString();
        String productDescription = binding.description.getText().toString();
        String productCategory = binding.categgories.getText().toString();

        // Prepare the data to be saved
        Map<String, Object> product = new HashMap<>();
        product.put("name", productName);
        product.put("price", productPrice);
        product.put("description", productDescription);
        product.put("categories", productCategory);
        if (imageUrl != null) {
            product.put("imageUrl", imageUrl);
        }

        // Save to Firestore with a new document ID
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // No arguments needed
        db.collection("products").add(product) // Using add() instead of document(productId)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added the product
                    dismissProgressDialog(); // Dismiss the dialog on success
                    dismissDimOverlay(); // Remove the dimming overlay

                    Toast.makeText(getActivity(), "Successfully added item", Toast.LENGTH_SHORT).show();
                    // Reset the form fields
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    dismissProgressDialog(); // Dismiss the dialog on failure
                    dismissDimOverlay(); // Remove the dimming overlay in case of failure
                    Log.e("Firestore Error", "Error adding item", e);
                    Toast.makeText(getContext(), "Error adding item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void dismissDimOverlay() {
        if (cl != null) {
            // Remove the dimming overlay from the parent layout (ConstraintLayout)
            View dimOverlay = cl.findViewWithTag("dimOverlay");
            if (dimOverlay != null) {
                cl.removeView(dimOverlay);  // Remove the dimming overlay view
            }
        }
    }


    private void resetForm() {
        binding.productName.setText("");
        binding.productPrice.setText("");
        binding.description.setText("");
        binding.categgories.setText("");
        binding.itemImg.setImageURI(null);
        selectedImageUri = null;
    }
}
