package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.databinding.FragmentTandCBinding; // Ensure this matches your XML layout name

public class TandCFragment extends Fragment {

    private FragmentTandCBinding binding; // Use the generated binding class name

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using the binding class
        binding = FragmentTandCBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide the Terms and Conditions content initially
        binding.scrollview.setVisibility(View.GONE);
        binding.termsLabel.setVisibility(View.GONE);
        binding.back.setVisibility(View.GONE);// This hides the ScrollView (content area)

        // Show the progress bar while loading content
        binding.progressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Once the content is ready, hide the progress bar
                binding.progressBar.setVisibility(View.GONE);

                // Now display the Terms and Conditions content
                binding.scrollview.setVisibility(View.VISIBLE);
                binding.termsLabel.setVisibility(View.VISIBLE);// Make the content visible
                binding.back.setVisibility(View.VISIBLE);

                // Set the terms text with HTML and enable the link
                binding.tvTerms5.setMovementMethod(LinkMovementMethod.getInstance());
                binding.tvTerms5.setLinkTextColor(getResources().getColor(android.R.color.holo_blue_dark)); // Make links blue

                binding.tvTerms10.setMovementMethod(LinkMovementMethod.getInstance());
                binding.tvTerms10.setLinkTextColor(getResources().getColor(android.R.color.holo_blue_dark)); // Make links blue
            }
        }, 3000); // Simulated delay (replace with actual content loading time)

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_tandCFragment_to_termsFragment2);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
