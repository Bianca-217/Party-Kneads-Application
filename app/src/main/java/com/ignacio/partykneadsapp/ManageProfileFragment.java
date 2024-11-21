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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.databinding.FragmentManageProfileBinding;
import com.ignacio.partykneadsapp.databinding.FragmentProfileBinding;


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

        binding.btnChangePassword.setOnClickListener(v -> {
            ChangePasswordDialogFragment dialogFragment = new ChangePasswordDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "ChangePasswordDialog");
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
}