package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.facebook.CallbackManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPass;
    private Button btnContinue, btnFacebook, btnGoogle;
    private FirebaseAuth mAuth;
    private TextView btnSignup;
    private ConstraintLayout cl;
    private CallbackManager callbackManager;
    private TextView btnForgotpass;

    private static final int RC_SIGN_IN = 100; // Request code for Google Sign-In

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            if (Objects.equals(currentUser.getEmail(), "sweetkatrinabiancaignacio@gmail.com")) {
                navigateToSellerHomePage();
            } else {
                Log.d("LoginFragment", "User is already signed in: " + currentUser.getEmail());
                navigateToUserHomePage();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();

        etEmail = view.findViewById(R.id.etEmail);
        etPass = view.findViewById(R.id.etPassword);
        btnContinue = view.findViewById(R.id.btnContinue);
        btnSignup = view.findViewById(R.id.btnSignUp);
        btnForgotpass = view.findViewById(R.id.btnForgotPass);


        btnForgotpass.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_loginFragment_to_forgotPassword);
        });


        btnSignup.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_loginFragment_to_termsFragment2);
        });

        // Login with email
        btnContinue.setOnClickListener(v -> loginWithEmail());

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        // Set up VideoView to play and loop the video
        VideoView loginVideoView = view.findViewById(R.id.loginVideoView);
        Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.login_new);
        loginVideoView.setVideoURI(videoUri);
        loginVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);  // Set looping to true
            loginVideoView.start();  // Start the video
        });
    }


    private void loginWithEmail() {
        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email"); // Set error on email field
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPass.setError("Enter password"); // Set error on password field
            etPass.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login Successfully", Toast.LENGTH_SHORT).show();
                        if (Objects.equals(email, "sweetkatrinabiancaignacio@gmail.com")) {
                            navigateToSellerHomePage();
                        } else {
                            navigateToUserHomePage();
                        }
                    } else {
                        Log.e("LoginFragment", "Email login failed", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void navigateToUserHomePage() {
        // Navigate to the User's home page after successful login
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_loginFragment_to_homePageFragment);
    }

    private void navigateToSellerHomePage() {
        // Navigate to Seller's home page after successful login
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_loginFragment_to_seller_HomePageFragment);
    }
}