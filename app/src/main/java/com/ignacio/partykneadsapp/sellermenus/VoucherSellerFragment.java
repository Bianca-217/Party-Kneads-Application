package com.ignacio.partykneadsapp.sellermenus;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentVoucherSellerBinding;

import java.util.HashMap;
import java.util.Map;

public class VoucherSellerFragment extends Fragment {
    FragmentVoucherSellerBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentVoucherSellerBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddVoucher1.setOnClickListener(v -> {
            // Show the confirmation dialog
            showConfirmVoucherDialog("10% Discount");
        });
        binding.btnAddVoucher2.setOnClickListener(v -> {
            // Show the confirmation dialog
            showConfirmVoucherDialog("20% Discount");
        });
        binding.btnAddVoucher3.setOnClickListener(v -> {
            // Show the confirmation dialog
            showConfirmVoucherDialog("15% Discount");
        });
        binding.btnAddVoucher4.setOnClickListener(v -> {
            // Show the confirmation dialog
            showConfirmVoucherDialog("₱100 Off");
        });
        binding.btnAddVoucher5.setOnClickListener(v -> {
            // Show the confirmation dialog
            showConfirmVoucherDialog("₱200 Off");
        });



    }

    // Function to display the confirmation dialog
    private void showConfirmVoucherDialog(String discount) {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.confirm_voucher_dialog, null);

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Get references to dialog buttons
        Button btnNo = dialogView.findViewById(R.id.btnNo);
        Button btnYes = dialogView.findViewById(R.id.btnYes);

        // Set click listeners for buttons
        btnNo.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog if "No" is clicked

        btnYes.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
            addVoucherToAllUsers(discount); // Proceed with adding vouchers
        });

        // Show the dialog
        dialog.show();
    }

    // Function to add voucher to all users (from previous implementation)
    private void addVoucherToAllUsers(String discount) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String userId = document.getId();
                        String email = document.getString("email");

                        // Skip admin account
                        if ("sweetkatrinabiancaignacio@gmail.com".equals(email)) {
                            continue;
                        }

                        // Voucher details
                        Map<String, Object> voucherData = new HashMap<>();
                        voucherData.put("discount", discount);
                        voucherData.put("status", "not claimed");

                        // Add to user's Vouchers collection
                        firestore.collection("Users")
                                .document(userId)
                                .collection("Vouchers")
                                .add(voucherData)
                                .addOnSuccessListener(docRef -> Log.d("Voucher", "Voucher added for user: " + userId))
                                .addOnFailureListener(e -> Log.e("Voucher", "Failed to add voucher for user: " + userId, e));
                    }

                    Toast.makeText(getContext(), "You have successfully added " + discount, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to retrieve users", e));
    }
}