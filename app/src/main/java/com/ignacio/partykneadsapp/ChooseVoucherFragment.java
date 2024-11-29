package com.ignacio.partykneadsapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.adapters.ChooseVoucherAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentChooseVoucherBinding;
import com.ignacio.partykneadsapp.model.VoucherModel;

import java.util.ArrayList;
import java.util.List;

public class ChooseVoucherFragment extends DialogFragment {

    private FragmentChooseVoucherBinding binding;
    private ChooseVoucherAdapter adapter;
    private final List<VoucherModel> voucherList = new ArrayList<>();
    private OnVoucherSelectedListener voucherSelectedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChooseVoucherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the adapter with the listener to handle voucher selection
        adapter = new ChooseVoucherAdapter(getContext(), voucherList, selectedVoucher -> {
            if (voucherSelectedListener != null) {
                voucherSelectedListener.onVoucherSelected(selectedVoucher);
            }
            dismiss(); // Close dialog after voucher selection
        });

        // Configure RecyclerView
        binding.voucherListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.voucherListRecyclerView.setAdapter(adapter);

        // Fetch claimed vouchers from Firestore
        fetchClaimedVouchers();

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    private void fetchClaimedVouchers() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("Users")
                .document(uid)
                .collection("Vouchers")
                .whereEqualTo("status", "claimed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    voucherList.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String id = document.getId();
                        String discount = document.getString("discount");
                        String status = document.getString("status");

                        if (discount != null && status != null) {
                            voucherList.add(new VoucherModel(id, discount, status));
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter about data changes
                })
                .addOnFailureListener(e -> Log.e("ChooseVoucherDialog", "Error fetching claimed vouchers", e));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Adjust dialog size to match parent
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;  // Match parent width
                params.height = WindowManager.LayoutParams.MATCH_PARENT; // Match parent height
                window.setAttributes(params);
                window.setGravity(Gravity.CENTER);  // Ensure dialog is centered

                // Set the background to transparent
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }


    public void setVoucherSelectedListener(OnVoucherSelectedListener listener) {
        this.voucherSelectedListener = listener;
    }

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(String selectedVoucher);
    }
}
