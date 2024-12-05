package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.ignacio.partykneadsapp.databinding.FragmentChangePasswordDialogBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChangePasswordDialogFragment extends DialogFragment {

    private FragmentChangePasswordDialogBinding binding;
    private ConstraintLayout cl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the dialog layout
        binding = FragmentChangePasswordDialogBinding.inflate(inflater, container, false);

        // Fetch and display the passwordChangedDate
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchAndDisplayPasswordChangedDate(user);
        }

        // Handle password validation
        setupPasswordValidation();

        // Handle confirm password matching
        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword());

        // Handle confirm password field text change to show or hide the helperNotMatch
        setupConfirmPasswordValidation();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        binding.helperNotMatch.setVisibility(View.GONE);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupPasswordValidation() {
        binding.NewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupConfirmPasswordValidation() {
        binding.confirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show or hide the helperNotMatch based on the match between new password and confirm password
                validatePasswordsMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validatePassword(String password) {
        // Update helper text colors based on password criteria
        binding.helperMinLength.setTextColor(password.length() >= 8 ? getResources().getColor(R.color.green) : getResources().getColor(R.color.grey));
        binding.helperUpperCase.setTextColor(password.matches(".*[A-Z].*") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.grey));
        binding.helperNumber.setTextColor(password.matches(".*\\d.*") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.grey));
        binding.helperSpecialChar.setTextColor(password.matches(".*[.@#$%^&+=].*") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.grey));
    }

    private void validatePasswordsMatch() {
        String newPassword = binding.NewPass.getText().toString();
        String confirmPassword = binding.confirmPass.getText().toString();

        // Show or hide the mismatch helper depending on whether passwords match
        if (!newPassword.equals(confirmPassword)) {
            binding.helperNotMatch.setVisibility(View.VISIBLE);
        } else {
            binding.helperNotMatch.setVisibility(View.GONE);
        }
    }

    private void handleChangePassword() {
        String currentPassword = binding.currentPass.getText().toString();
        String newPassword = binding.NewPass.getText().toString();
        String confirmPassword = binding.confirmPass.getText().toString();
        if(!confirmPassword.isEmpty()) {
            if (newPassword.equals(confirmPassword)) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Re-authenticate the user
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Password is correct, proceed with password change
                                    updatePassword(newPassword, user);
                                } else {
                                    // Show error if current password is incorrect
                                    Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Passwords do not match
                binding.helperNotMatch.setVisibility(View.VISIBLE);
            }
        }else {
            Toast.makeText(getContext(), "Please enter a password", Toast.LENGTH_SHORT).show();
        }

    }

    private void updatePassword(String newPassword, FirebaseUser user) {
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully updated password
                        Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();

                        // Store the password change timestamp in Firestore
                        updatePasswordChangedDate(user);

                        dismiss();
                    } else {
                        // Error changing password
                        Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePasswordChangedDate(FirebaseUser user) {
        String userId = user.getUid();
        long currentTimestamp = System.currentTimeMillis();

        // Format the current timestamp to a readable date string
        String formattedDate = formatTimestampToDate(currentTimestamp);

        // Reference to the user's document in Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if the document exists and if passwordChangedDate is already set
                        if (task.getResult().contains("passwordChangedDate")) {
                            // If passwordChangedDate exists, update it with the current formatted date
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("passwordChangedDate", formattedDate);

                            firestore.collection("Users").document(userId)
                                    .set(updateData, SetOptions.merge())  // Merge to avoid overwriting other fields
                                    .addOnSuccessListener(aVoid -> Log.d("ChangePasswordDialog", "Password change date updated"))
                                    .addOnFailureListener(e -> Log.e("ChangePasswordDialog", "Error updating password change date", e));
                        } else {
                            // If passwordChangedDate doesn't exist, set it with the current formatted date
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("passwordChangedDate", formattedDate);

                            firestore.collection("Users").document(userId)
                                    .set(updateData, SetOptions.merge())  // Merge to avoid overwriting other fields
                                    .addOnSuccessListener(aVoid -> Log.d("ChangePasswordDialog", "Password change date set"))
                                    .addOnFailureListener(e -> Log.e("ChangePasswordDialog", "Error setting password change date", e));
                        }
                    } else {
                        Log.e("ChangePasswordDialog", "Error fetching user data", task.getException());
                    }
                });
    }



    private String formatTimestampToDate(long timestamp) {
        // Format the timestamp into a readable date
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private void fetchAndDisplayPasswordChangedDate(FirebaseUser user) {
        String userId = user.getUid();

        // Reference to the user's document in Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if 'passwordChangedDate' exists in the document
                        if (task.getResult().contains("passwordChangedDate")) {
                            Object passwordChangedDateObj = task.getResult().get("passwordChangedDate");

                            // Check the type of the field and process it accordingly
                            if (passwordChangedDateObj instanceof String) {
                                String passwordChangedDate = (String) passwordChangedDateObj;
                                if (passwordChangedDate.isEmpty()) {
                                    binding.dateAccountCreated.setText("Password change date not set");
                                } else {
                                    binding.dateAccountCreated.setText(passwordChangedDate);
                                }
                            } else if (passwordChangedDateObj instanceof Long) {
                                // If it's stored as a timestamp (in milliseconds), convert it to a date string
                                long timestamp = (Long) passwordChangedDateObj;
                                String formattedDate = formatTimestampToDate(timestamp);
                                binding.dateAccountCreated.setText(formattedDate);
                            } else {
                                Log.d("ChangePasswordDialog", "Unexpected data type for passwordChangedDate");
                            }
                        } else {
                            binding.dateAccountCreated.setText("Password change date not available");
                        }
                    } else {
                        Log.e("ChangePasswordDialog", "Error fetching user data", task.getException());
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Set dialog width and height
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,  // Set to MATCH_PARENT for full width
                    ViewGroup.LayoutParams.WRAP_CONTENT  // Set to WRAP_CONTENT for dynamic height
            );

            // Remove gray background and make it transparent
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
