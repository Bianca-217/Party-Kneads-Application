package com.ignacio.partykneadsapp.Seller_Orders;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentCompleteOrderBinding;
import com.ignacio.partykneadsapp.databinding.FragmentCompletedBinding;


public class CompleteOrderFragment extends Fragment {

   private FragmentCompleteOrderBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompleteOrderBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
}