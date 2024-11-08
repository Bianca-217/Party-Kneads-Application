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

public class ProfileFragment extends Fragment {

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

        if (currentUser != null) {
            String userId = currentUser.getUid();
            retrieveUserInfo(userId);
        }

        setupButtons();
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