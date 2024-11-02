package com.ignacio.partykneadsapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.databinding.FragmentOTPBinding;

import java.util.HashMap;
import java.util.Random;

public class OTPFragment extends Fragment {
    SharedViewModel sharedViewModel;
    private FragmentOTPBinding binding;
    private FirebaseAuth auth;
    private String email = "";
    private String pass = "";
    private String lname = "";
    private String fname = "";
    private int random;
    private CountDownTimer resendCountDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOTPBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        auth = FirebaseAuth.getInstance();

        fname = sharedViewModel.getFname();
        lname = sharedViewModel.getLname();

        if (getArguments() != null) {
            email = getArguments().getString("email");
            pass = getArguments().getString("password");
        }

        random();

        // Add text watchers for OTP fields
        setupOtpInputFields();
        binding.tvEmail.setText(email);
        binding.btnsubmitOTP.setOnClickListener(v -> verifyOtp());


        binding.resendCount.setVisibility(View.INVISIBLE);

        binding.btnResend.setOnClickListener(v -> {
            // Regenerate OTP, show toast message, and start countdown
            random();
            Toast.makeText(getActivity(), "A new OTP has been sent to your email", Toast.LENGTH_SHORT).show();

            // Show countdown text and start countdown timer
            binding.resendCount.setVisibility(View.VISIBLE);
            startResendCountdown();
        });

    }

    private void startResendCountdown() {
        binding.btnResend.setEnabled(false); // Disable resend button during countdown

        resendCountDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.resendCount.setText("(" + (millisUntilFinished / 1000) + " sec)");
            }

            @Override
            public void onFinish() {
                binding.resendCount.setText(""); // Clear the countdown text
                binding.resendCount.setVisibility(View.INVISIBLE); // Hide countdown text
                binding.btnResend.setEnabled(true); // Re-enable resend button
            }
        }.start();
    }

    private void saveName() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("First Name", fname);  // Store first name
            userMap.put("Last Name", lname);  // Store last name
            userMap.put("email", email);

            // Save user details to Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(uid).set(userMap)
                    .addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            // Navigate to the home page after saving the data
                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.action_OTPFragment_to_homePageFragment);
                        } else {
                            Toast.makeText(getActivity(), "Error saving user details.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setupOtpInputFields() {
        binding.otp1.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.otp2.requestFocus();
                }
            }
        });

        binding.otp2.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.otp3.requestFocus();
                } else {
                    binding.otp1.requestFocus();
                }
            }
        });

        binding.otp3.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.otp4.requestFocus();
                } else {
                    binding.otp2.requestFocus();
                }
            }
        });

        binding.otp4.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.otp5.requestFocus();
                } else {
                    binding.otp3.requestFocus();
                }
            }
        });

        binding.otp5.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.otp6.requestFocus();
                } else {
                    binding.otp4.requestFocus();
                }
            }
        });

        binding.otp6.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    binding.otp5.requestFocus();
                }
            }
        });
    }

    private void verifyOtp() {
        String otp1 = binding.otp1.getText().toString();
        String otp2 = binding.otp2.getText().toString();
        String otp3 = binding.otp3.getText().toString();
        String otp4 = binding.otp4.getText().toString();
        String otp5 = binding.otp5.getText().toString();
        String otp6 = binding.otp6.getText().toString();

        String otp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6;

        if (otp.isEmpty() || otp.length() < 6) {
            Toast.makeText(getActivity(), "Enter OTP", Toast.LENGTH_SHORT).show();
        } else if (!otp.equals(String.valueOf(random))) {
            Toast.makeText(getActivity(), "Incorrect OTP", Toast.LENGTH_SHORT).show();
        } else {
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    saveName();
                } else {
                    // Log the specific error from Firebase
                    Exception e = task.getException();
                    if (e != null) {
                        Log.e("OTPFragment", "Error: " + e.getMessage());
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Unknown error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void random() {
        random = new Random().nextInt(900000) + 100000; // Generate 6-digit OTP

        // Create and send the email
        String subject = "Login Signup app's OTP";
        String message = "Your OTP is -> " + random;

        new JavaMailSender(email, subject, message).execute(); // Send email
    }

    // Don't forget to cancel the timer in onDestroy to avoid leaks
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
        }
    }

    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
