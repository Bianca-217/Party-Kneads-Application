package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.databinding.FragmentVoucherBinding;


public class VoucherFragment extends Fragment{

   private FragmentVoucherBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentVoucherBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }
}