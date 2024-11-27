package com.ignacio.partykneadsapp.Seller_Orders;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.databinding.FragmentPendingSellerSideBinding;


public class PendingSellerSideFragment extends Fragment {

    private FragmentPendingSellerSideBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPendingSellerSideBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
}