package com.ignacio.partykneadsapp.customermenus;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentProfileBinding;

import java.util.Objects;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnSuccessListener;


public class ProfileFragment extends Fragment {
    private StorageReference storageReference;
    private Uri selectedImageUri;
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            retrieveUserInfo(userId);
        }

        setupButtons();

        binding.btnchangeProfile.setOnClickListener(v -> {
            openGallery();
        });

        binding.btnTerms.setOnClickListener(v ->{

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_termsProfileFragment);
        });

        binding.btnManageProfile.setOnClickListener(v ->{

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_manageProfileFragment);
        });

        binding.btnPolicy.setOnClickListener(v ->{

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_policyFragment);
        });

        binding.btnAbout.setOnClickListener(v ->{

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_aboutFragment);
        });

        binding.btnSupport.setOnClickListener(v ->{

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_supportFragment);
        });

        binding.btnHistory.setOnClickListener(v ->{

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_orderHistoryFragment);
        });


    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }


    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();

                    // Using Glide to load the selected image into ImageView
                    Glide.with(requireContext())
                            .load(selectedImageUri)
                            .placeholder(R.drawable.round_person_24) // Optional: add a placeholder
                            .into(binding.userProfile);

                    uploadProfilePicture(selectedImageUri);
                }
            }
    );

    private void uploadProfilePicture(Uri imageUri) {
        if (imageUri != null && currentUser != null) {
            String userId = currentUser.getUid();
            StorageReference fileRef = storageReference.child("profile_pictures/" + userId + ".jpg");

            // Show progress bar
            binding.progressBar.setVisibility(View.VISIBLE);

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateProfilePictureUrl(userId, downloadUrl);
                        Toast.makeText(getActivity(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to upload picture", Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    });
        }
    }


    private void updateProfilePictureUrl(String userId, String url) {
        firestore.collection("Users").document(userId)
                .update("profilePictureUrl", url)
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Profile picture saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to save profile picture", Toast.LENGTH_SHORT).show());
    }


    private void retrieveUserInfo(String userId) {
        firestore.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve first name, last name, email, and profile picture URL
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String fname = document.getString("First Name");
                            String lname = document.getString("Last Name");
                            String email = document.getString("email");
                            String profilePictureUrl = document.getString("profilePictureUrl");

                            // Set the full name and email in the TextViews
                            if (fname == null && lname == null) {
                                binding.txtUserName.setText(firstName + " " + lastName);
                            } else if ((fname != null && lname != null)){
                                binding.txtUserName.setText(fname + " " + lname);
                            }

                            if (email != null) {
                                binding.txtUserEmail.setText(email);
                            } else {
                                binding.txtUserEmail.setText("No Email Available");
                            }

                            // Load and display the profile picture if available
                            if (profilePictureUrl != null) {
                                Glide.with(getActivity())  // Glide to load the image
                                        .load(profilePictureUrl)
                                        .into(binding.userProfile);  // Set the profile image in imgProfile
                            } else {
                                binding.userProfile.setImageResource(R.drawable.img_placeholder);  // Use default image if no profile picture
                            }
                        } else {
                            Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupButtons() {
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_profileFragment_to_loginFragment);
        });

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            if (Objects.equals(currentUser.getEmail(), "sweetkatrinabiancaignacio@gmail.com")) {
                navController.navigate(R.id.action_profileFragment_to_seller_HomePageFragment);
            } else {
                navController.navigate(R.id.action_profileFragment_to_homePageFragment);
            }
        });
    }
}