package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.adapters.VoucherAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentVoucherBinding;
import com.ignacio.partykneadsapp.model.VoucherModel;

import java.util.ArrayList;
import java.util.List;


public class VoucherFragment extends Fragment {

    private FragmentVoucherBinding binding;
    private VoucherAdapter adapter;
    private List<VoucherModel> voucherList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVoucherBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        binding.discountRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VoucherAdapter(requireContext(), voucherList);
        binding.discountRecyclerview.setAdapter(adapter);


        binding.btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_voucherFragment_to_homePageFragment, args1);
        });
        // Fetch data from Firestore
        fetchVouchers();
    }

    private void fetchVouchers() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("Users")
                .document(uid)
                .collection("Vouchers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    voucherList.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        // Extract fields from Firestore
                        String id = document.getId(); // Use document ID as the unique identifier
                        String discount = document.getString("discount");
                        String status = document.getString("status");

                        // Only add vouchers that are "not claimed"
                        if ("not claimed".equals(status)) {
                            // Add a new VoucherModel to the list
                            voucherList.add(new VoucherModel(id, discount, status));
                        }
                    }
                    // Notify the adapter of data changes
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("VoucherFragment", "Error fetching vouchers", e));
    }


}
