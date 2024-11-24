package com.ignacio.partykneadsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.adapters.LocationAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentChooseAddressBinding;

import java.util.ArrayList;


public class ChooseAddressFragment extends DialogFragment {

    FragmentChooseAddressBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment;
        binding = FragmentChooseAddressBinding.inflate(getLayoutInflater());
        return binding.getRoot();



    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Set dialog width and height
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,  // Set to MATCH_PARENT for full width
                    ViewGroup.LayoutParams.WRAP_CONTENT  // Set to WRAP_CONTENT for dynamic height
            );

            // Remove gray background and make it transparent
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }



    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnClose.setOnClickListener(v -> {
            // Inflate the custom dialog layout
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.close_dialog, null);

            // Create and configure the AlertDialog
            AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            // Make the background of the dialog transparent
            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // Set up the Cancel button
            dialogView.findViewById(R.id.btnCancel).setOnClickListener(cancelView -> {
                alertDialog.dismiss(); // Close the custom dialog
            });

            // Set up the Discard button
            dialogView.findViewById(R.id.btnDiscard).setOnClickListener(discardView -> {
                alertDialog.dismiss(); // Close the custom dialog
                dismiss(); // Close the DialogFragment
            });

            // Show the dialog
            alertDialog.show();
        });
    }

}
