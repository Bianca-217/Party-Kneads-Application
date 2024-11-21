package com.ignacio.partykneadsapp.ProfileMenus;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentAboutBinding;
import com.ignacio.partykneadsapp.databinding.FragmentTermsProfileBinding;


public class AboutFragment extends Fragment {

   private FragmentAboutBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAboutBinding.inflate(inflater, container, false);
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

            }
        }, 3000); // Simulated delay (replace with actual content loading time)


        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_aboutFragment_to_profileFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}