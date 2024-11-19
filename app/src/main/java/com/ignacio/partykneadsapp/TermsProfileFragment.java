package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.databinding.FragmentTandCBinding;
import com.ignacio.partykneadsapp.databinding.FragmentTermsProfileBinding;


public class TermsProfileFragment extends Fragment {


    private FragmentTermsProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using the binding class
        binding = FragmentTermsProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the terms text with HTML and enable the link
        binding.tvTerms5.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvTerms5.setLinkTextColor(getResources().getColor(android.R.color.holo_blue_dark)); // Make links blue

        binding.tvTerms10.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvTerms10.setLinkTextColor(getResources().getColor(android.R.color.holo_blue_dark)); // Make links blue

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_termsProfileFragment_to_profileFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}