package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountFragment extends Fragment {

    private TextInputEditText etPassCA, etEmailCA;
    private Button btnCont;
    private FirebaseAuth mAuth;
    private ImageView btnBack;
    private ConstraintLayout cl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmailCA = view.findViewById(R.id.etEmailCA);
        etPassCA = view.findViewById(R.id.etPassCA);
        btnCont = view.findViewById(R.id.btnCont);
        btnBack = view.findViewById(R.id.btnBack);
        cl = view.findViewById(R.id.clayout);

        // Helper Text Views for Password Criteria
        TextView helperMinLength = view.findViewById(R.id.helperMinLength);
        TextView helperUpperCase = view.findViewById(R.id.helperUpperCase);
        TextView helperNumber = view.findViewById(R.id.helperNumber);
        TextView helperSpecialChar = view.findViewById(R.id.helperSpecialChar);

        etPassCA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String password = editable.toString();

                // Check each criterion and update helper text color accordingly
                helperMinLength.setTextColor(password.length() >= 8 ? Color.GREEN : Color.GRAY);
                helperUpperCase.setTextColor(Pattern.compile("[A-Z]").matcher(password).find() ? Color.GREEN : Color.GRAY);
                helperNumber.setTextColor(Pattern.compile("[0-9]").matcher(password).find() ? Color.GREEN : Color.GRAY);
                helperSpecialChar.setTextColor(Pattern.compile("[^a-zA-Z0-9 ]").matcher(password).find() ? Color.GREEN : Color.GRAY);
            }
        });

        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_createAccountFragment4_to_personaldetailsFragment);
        });

        btnCont.setOnClickListener(v -> {
            if (validateFields()) {
                String email = etEmailCA.getText().toString().trim();
                String password = etPassCA.getText().toString();

                // Proceed to OTP Fragment
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                bundle.putString("password", password);

                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_createAccountFragment4_to_OTPFragment, bundle);
            }
        });

        cl.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });
    }

    private boolean validateFields() {
        String email = etEmailCA.getText().toString().trim();
        String password = etPassCA.getText().toString();

        // Check if email field is empty
        if (TextUtils.isEmpty(email)) {
            etEmailCA.setError("Email is required");
            Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isValidEmail(email)) {  // Check if email is valid
            etEmailCA.setError("Please enter a valid Gmail address (e.g., user@gmail.com)");
            Toast.makeText(requireContext(), "Please enter a valid Gmail address (e.g., user@gmail.com)", Toast.LENGTH_LONG).show();
            return false;
        } else {
            etEmailCA.setError(null); // Clear error if valid
        }

        // Check if password field is empty
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password is strong enough
        if (!isStrongPassword(password)) {
            Toast.makeText(requireContext(), "Password should contain a minimum of 8 characters, at least one uppercase letter, one number, and one special character.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    // Helper method to validate email format, specifically for Gmail
    private boolean isValidEmail(String email) {
        // Regular expression pattern for a Gmail address
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        Pattern upperCasePattern = Pattern.compile("[A-Z]");
        Pattern numberPattern = Pattern.compile("[0-9]");
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9 ]");

        Matcher hasUpperCase = upperCasePattern.matcher(password);
        Matcher hasNumber = numberPattern.matcher(password);
        Matcher hasSpecialChar = specialCharPattern.matcher(password);

        return hasUpperCase.find() && hasNumber.find() && hasSpecialChar.find();
    }

}
