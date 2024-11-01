package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.ignacio.partykneadsapp.databinding.FragmentEditAddressBinding;
import com.ignacio.partykneadsapp.databinding.FragmentNewAddressBinding;


public class EditAddressFragment extends Fragment {

    private ConstraintLayout cl;
    FragmentEditAddressBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditAddressBinding.inflate(getLayoutInflater());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        binding.btnBack.setOnClickListener(v -> {

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_editAddressFragment_to_addressFragment);

        });

        }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}