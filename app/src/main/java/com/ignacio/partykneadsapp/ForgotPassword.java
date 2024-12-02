package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ignacio.partykneadsapp.databinding.FragmentForgotPasswordBinding;
public class ForgotPassword extends Fragment {

    FragmentForgotPasswordBinding binding;
    private ConstraintLayout cl;

    // Firebase Auth instance
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Set up the click listener for the back button
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_forgotPassword_to_loginFragment);
        });

        // Set up the reset password functionality
        binding.btnSendLink.setOnClickListener(v -> {
            String email = binding.etEmailCA.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordResetEmail(email);
            }
        });

        return binding.getRoot(); // Return the view here
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl = view.findViewById(R.id.clayout);

        cl.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });
    }

    // Method to send password reset email using Firebase Auth
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show a success message
                        Toast.makeText(getActivity(), "Password reset email sent!", Toast.LENGTH_SHORT).show();

                        // Navigate back to the login screen after successfully sending the email
                        if (isAdded() && getView() != null) {
                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.action_forgotPassword_to_loginFragment);
                        }
                    } else {
                        // Show error message if something goes wrong
                        Toast.makeText(getActivity(), "Error sending password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}