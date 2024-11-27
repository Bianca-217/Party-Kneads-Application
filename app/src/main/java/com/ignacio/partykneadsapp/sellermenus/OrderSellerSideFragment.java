package com.ignacio.partykneadsapp.sellermenus;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentOrderSellerSideBinding;


public class OrderSellerSideFragment extends Fragment {

   private FragmentOrderSellerSideBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

      binding = FragmentOrderSellerSideBinding.inflate(getLayoutInflater());
      return binding.getRoot();

    }
}