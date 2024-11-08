package com.ignacio.partykneadsapp;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPass;
    private Button btnContinue, btnFacebook, btnGoogle;
    private FirebaseAuth mAuth;
    private TextView btnSignup;
    private ConstraintLayout cl;
    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;

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
        btnFacebook = view.findViewById(R.id.btnFacebook);
        btnGoogle = view.findViewById(R.id.btnGoogle);

        btnSignup.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_loginFragment_to_termsFragment2);
        });

        // Login with email
        btnContinue.setOnClickListener(v -> loginWithEmail());

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        setupFacebookLogin();
        setupGoogleSignIn();
    }

    private void loginWithEmail() {
        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Enter password", Toast.LENGTH_SHORT).show();
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

    private void setupFacebookLogin() {
        btnFacebook.setOnClickListener(v -> signInWithFacebook());
        initializeFacebookLogin();
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("email", "public_profile"));
    }

    private void initializeFacebookLogin() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("LoginFragment", "Facebook login successful");
                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    user.linkWithCredential(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("FacebookLogin", "Linking successful");
                                    navigateToUserHomePage();
                                } else {
                                    Log.e("FacebookLogin", "Linking failed", task.getException());
                                    Toast.makeText(getActivity(), "Linking Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    navigateToUserHomePage();
                                } else {
                                    Log.e("FacebookLogin", "Firebase Auth failed", task.getException());
                                    Toast.makeText(getActivity(), "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancel() {
                Log.d("FacebookLogin", "Facebook login canceled");
                Toast.makeText(getActivity(), "Facebook login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookLogin", "Error: " + error.getMessage(), error);
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGoogleSignIn() {
        // Update to use Web Client ID from Firebase for Google Sign-In
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Web Client ID (from Firebase)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), options);

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Get the signed-in account from the result
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Extract the first name, last name, email, and profile picture URL from the account
            String firstName = account.getGivenName();  // First name
            String lastName = account.getFamilyName();  // Last name
            String email = account.getEmail();          // Email address
            String profilePictureUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;  // Profile picture URL

            // Log the information (or save it as needed)
            Log.d("GoogleSignIn", "First Name: " + firstName);
            Log.d("GoogleSignIn", "Last Name: " + lastName);
            Log.d("GoogleSignIn", "Email: " + email);
            Log.d("GoogleSignIn", "Profile Picture URL: " + profilePictureUrl);

            // Continue to Firebase authentication
            firebaseAuthWithGoogle(account, firstName, lastName, email, profilePictureUrl);

        } catch (ApiException e) {
            Log.e("GoogleSignIn", "Sign-in failed: " + e.getStatusCode(), e);
            Toast.makeText(getActivity(), "Google Sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account, String firstName, String lastName, String email, String profilePictureUrl) {
        // Use the Google account's ID token to authenticate with Firebase
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Successfully authenticated with Firebase
                        Log.d("GoogleSignIn", "Authentication successful");

                        // You can also save the user data (first name, last name, profile picture, etc.) to Firestore
                        saveUserData(firstName, lastName, email, profilePictureUrl);

                        // Navigate to the user home page
                        navigateToUserHomePage();
                    } else {
                        // Authentication failed
                        Log.e("GoogleSignIn", "Authentication failed", task.getException());
                        Toast.makeText(getActivity(), "Google Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String firstName, String lastName, String email, String profilePictureUrl) {
        // Save user data (First Name, Last Name, Email, Profile Picture URL) to Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Create a map to hold the user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("email", email);
            userData.put("profilePictureUrl", profilePictureUrl);  // Add the profile picture URL
            userData.put("lastLogin", FieldValue.serverTimestamp());

            // Save the data to Firestore (you can store it in a collection like "users")
            firestore.collection("Users")
                    .document(currentUser.getUid())
                    .set(userData, SetOptions.merge())  // Use merge to avoid overwriting existing fields
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("GoogleSignIn", "User data saved to Firestore");
                        } else {
                            Log.e("GoogleSignIn", "Error saving user data: ", task.getException());
                        }
                    });
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