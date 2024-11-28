package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.databinding.FragmentCustomizeOrderBinding;

public class CustomizeOrderFragment extends Fragment {

   private FragmentCustomizeOrderBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCustomizeOrderBinding.inflate(getLayoutInflater());
        return binding.getRoot();

    }
}