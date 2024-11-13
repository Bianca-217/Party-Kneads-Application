package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ignacio.partykneadsapp.databinding.FragmentPersonaldetailsBinding;

public class personaldetailsFragment extends Fragment {

    private FragmentPersonaldetailsBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using binding
        binding = FragmentPersonaldetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up input filters to allow only letters (no numbers or special characters)
        binding.etfName.setFilters(new InputFilter[]{getTextOnlyFilter()});
        binding.etlName.setFilters(new InputFilter[]{getTextOnlyFilter()});

        // Set up listeners using binding
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_personaldetailsFragment_to_termsFragment2);
        });

        binding.btnCont.setOnClickListener(v -> {
            if (checkFieldIfEmpty()) {
                String fname = binding.etfName.getText().toString().trim();
                String lname = binding.etlName.getText().toString().trim();

                sharedViewModel.setNames(fname, lname);

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_personaldetailsFragment_to_createAccountFragment4);
            }
        });

        // Hide keyboard on ConstraintLayout click
        binding.clayout.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });
    }


    // InputFilter to restrict input to only letters (no numbers or special characters)
    private InputFilter getTextOnlyFilter() {
        return (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isWhitespace(source.charAt(i))) {
                    return ""; // Reject non-letter characters
                }
            }
            return null;
        };
    }


    private boolean checkFieldIfEmpty() {
        boolean isValid = true;

        // Check if the first name is empty or contains only whitespace
        if (TextUtils.isEmpty(binding.etfName.getText().toString().trim())) {
            binding.etfName.setError("First Name is required");
            isValid = false;
        } else {
            binding.etfName.setError(null);  // Clear error if not empty
        }

        // Check if the last name is empty or contains only whitespace
        if (TextUtils.isEmpty(binding.etlName.getText().toString().trim())) {
            binding.etlName.setError("Last Name is required");
            isValid = false;
        } else {
            binding.etlName.setError(null);  // Clear error if not empty
        }

        // Show toast if any field is empty
        if (!isValid) {
            Toast.makeText(requireContext(), "Please fill up all fields.", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // Clear the binding reference
    }
}
