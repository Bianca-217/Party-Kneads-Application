package com.ignacio.partykneadsapp.ProfileMenus;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.ChangePasswordDialogFragment;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentManageProfileBinding;


public class ManageProfileFragment extends Fragment {

    private FragmentManageProfileBinding binding;
    private FirebaseFirestore firestore;
    private ConstraintLayout cl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize FirebaseFirestore instance
        firestore = FirebaseFirestore.getInstance();

        // Inflate the layout using ViewBinding
        binding = FragmentManageProfileBinding.inflate(inflater, container, false);

        // Assuming you have a method to get the user ID (this can be passed via the Bundle or from FirebaseAuth)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch user data
        retrieveUserInfo(userId);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        // Back button listener
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_manageProfileFragment_to_profileFragment);
        });

        // Change Password button listener
        binding.btnChangePassword.setOnClickListener(v -> {
            ChangePasswordDialogFragment dialogFragment = new ChangePasswordDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "ChangePasswordDialog");
        });

// Save changes button listener
        binding.btnSaveChanges.setOnClickListener(v -> {
            String updatedFirstName = binding.userFName.getText().toString().trim();
            String updatedLastName = binding.userLname.getText().toString().trim();

            boolean isValid = true;

            // Regular expression to allow letters, spaces, and periods
            String nameRegex = "^[a-zA-Z .]+$";

            // Validation for empty or invalid first name
            if (updatedFirstName.isEmpty()) {
                binding.userFName.setError("First name cannot be empty");
                isValid = false;
            } else if (!updatedFirstName.matches(nameRegex)) {
                binding.userFName.setError("First name contains invalid characters");
                isValid = false;
            } else {
                binding.userFName.setError(null); // Clear error
            }

            // Validation for empty or invalid last name
            if (updatedLastName.isEmpty()) {
                binding.userLname.setError("Last name cannot be empty");
                isValid = false;
            } else if (!updatedLastName.matches(nameRegex)) {
                binding.userLname.setError("Last name contains invalid characters");
                isValid = false;
            } else {
                binding.userLname.setError(null); // Clear error
            }

            // Proceed only if all inputs are valid
            if (isValid) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                updateUserInfo(userId, updatedFirstName, updatedLastName);
            }
        });

        // Delete Account button listener
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showDeleteAccountDialog() {
        // Inflate the dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.delete_account_dialog, null);

        // Create the dialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set the background of the dialog to transparent using Window's attributes
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // Find and set up the buttons
        dialogView.findViewById(R.id.btnYes).setOnClickListener(v -> {
            dialog.dismiss(); // Dismiss the dialog
            deleteUserAccount(); // Proceed to delete the account
        });

        dialogView.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteUserAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Delete the user document from Firestore
        firestore.collection("Users").document(userId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Delete the user from Firebase Authentication
                        auth.getCurrentUser().delete().addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                Toast.makeText(getActivity(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                // Redirect to the login or home screen
                                NavController navController = Navigation.findNavController(requireView());
                                navController.navigate(R.id.action_manageProfileFragment_to_loginFragment);
                            } else {
                                Toast.makeText(getActivity(), "Failed to delete account from authentication", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Failed to delete account from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

        private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void retrieveUserInfo(String userId) {
        firestore.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve first name and last name
                            String firstName = document.getString("First Name");
                            String lastName = document.getString("Last Name");
                            if (firstName != null) {
                                binding.userFName.setText(firstName);
                                binding.userFName.requestLayout();  // Force UI to refresh
                            }
                            if (lastName != null) {
                                binding.userLname.setText(lastName);
                                binding.userLname.requestLayout();  // Force UI to refresh
                            }

                        } else {
                            Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to update user info in Firestore
    private void updateUserInfo(String userId, String firstName, String lastName) {
        // Update Firestore document with the new first name and last name
        firestore.collection("Users").document(userId)
                .update("First Name", firstName, "Last Name", lastName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        // Optionally, you could refresh the UI with the updated data here
                    } else {
                        Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
