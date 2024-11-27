package com.ignacio.partykneadsapp.Seller_Orders;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentDeliverOrderBinding;


public class DeliverOrderFragment extends Fragment {

    private FragmentDeliverOrderBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDeliverOrderBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
}