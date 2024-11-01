package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.databinding.FragmentEditAddressBinding;

public class EditAddressFragment extends Fragment {

    private ConstraintLayout cl;
    FragmentEditAddressBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditAddressBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String username = getArguments().getString("username");
        String phonenumber = getArguments().getString("phonenumber");

        binding.txtUserName.setText(username);
        binding.contactNum.setText(phonenumber);

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_editAddressFragment_to_addressFragment);
        });

        binding.btndeleteAddress.setOnClickListener(v -> clearInputFields());
        numberMaxDigit();
        binding.btnSave.setOnClickListener(v -> {
            if (validateFields()) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String location = getArguments().getString("location");  // Get location from bundle
                String userName = binding.txtUserName.getText().toString().trim();
                String phoneNumber = binding.contactNum.getText().toString().trim();
                String city = binding.cities.getText().toString().trim();
                String barangay = binding.barangays.getText().toString().trim();
                String postalCode = binding.postalCode.getText().toString().trim();
                String houseNum = binding.houseNum.getText().toString().trim();

                Toast.makeText(getContext(), location, Toast.LENGTH_SHORT).show();
                String loc = houseNum + ", " + barangay + ", " + city + ", Laguna, " + postalCode;
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users").document(userId).collection("Locations")
                        .whereEqualTo("location", location)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                // Assuming there's only one document per location
                                String docId = querySnapshot.getDocuments().get(0).getId();

                                db.collection("Users").document(userId).collection("Locations")
                                        .document(docId)
                                        .update(
                                                "userName", userName,
                                                "phoneNumber", phoneNumber,
                                                "city", city,
                                                "barangay", barangay,
                                                "postalCode", postalCode,
                                                "houseNum", houseNum,
                                                "location", loc
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Address updated successfully!", Toast.LENGTH_SHORT).show();
                                            NavController navController = Navigation.findNavController(requireView());
                                            navController.navigate(R.id.action_editAddressFragment_to_addressFragment);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to update address", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to fetch address", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(),"error", Toast.LENGTH_SHORT).show();
            }
        });

        setupAutoCompleteTextView();
    }

    private void setupAutoCompleteTextView() {
        String[] cities = getResources().getStringArray(R.array.City);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cities);
        binding.cities.setAdapter(adapter);

        binding.cities.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            updateBarangayOptions(selectedCity);
            updatePostalCode(selectedCity);
        });
    }

    private void updateBarangayOptions(String city) {
        int barangayArrayId = getBarangayArrayId(city);
        if (barangayArrayId != -1) {
            String[] barangays = getResources().getStringArray(barangayArrayId);
            ArrayAdapter<String> barangayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, barangays);
            binding.barangays.setAdapter(barangayAdapter);
            binding.barangays.setText("");
        } else {
            binding.barangays.setAdapter(null);
        }
    }

    private int getBarangayArrayId(String city) {
        switch (city) {
            case "Alaminos":
                return R.array.AlaminosBarangay;
            case "Bay":
                return R.array.BayBarangay;
            case "Biñan":
                return R.array.BinanBarangay;
            case "Cabuyao":
                return R.array.CabuyaoBarangay;
            case "Calamba":
                return R.array.CalambaBarangay;
            case "Calauan":
                return R.array.CalauanBarangay;
            case "Cavinti":
                return R.array.CavintiBarangay;
            case "Famy":
                return R.array.FamyBarangay;
            case "Kalayaan":
                return R.array.KalayaanBarangay;
            case "Liliw":
                return R.array.LiliwBarangay;
            case "Los Baños":
                return R.array.LosBanosBarangay;
            case "Luisiana":
                return R.array.LuisianaBarangay;
            case "Lumban":
                return R.array.LumbanBarangay;
            case "Mabitac":
                return R.array.MabitacBarangay;
            case "Magdalena":
                return R.array.MagdalenaBarangay;
            case "Majayjay":
                return R.array.MajayjayBarangay;
            case "Nagcarlan":
                return R.array.NagcarlanBarangay;
            case "Paete":
                return R.array.PaeteBarangay;
            case "Pagsanjan":
                return R.array.PagsanjanBarangay;
            case "Pakil":
                return R.array.PakilBarangay;
            case "Pangil":
                return R.array.PangilBarangay;
            case "Pila":
                return R.array.PilaBarangay;
            case "Rizal":
                return R.array.RizalBarangay;
            case "San Pablo":
                return R.array.SanPabloBarangay;
            case "San Pedro":
                return R.array.SanPedroBarangay;
            case "Santa Cruz":
                return R.array.SantaCruzBarangay;
            case "Santa Maria":
                return R.array.SantaMariaBarangay;
            case "Santa Rosa":
                return R.array.SantaRosaBarangay;
            case "Siniloan":
                return R.array.SiniloanBarangay;
            case "Victoria":
                return R.array.VictoriaBarangay;
            default:
                return -1;
        }
    }

    private void updatePostalCode(String city) {
        String postalCode = getPostalCode(city);
        binding.postalCode.setText(postalCode);
    }

    private String getPostalCode(String city) {
        switch (city) {
            case "Alaminos": return "4034";
            case "Bay": return "4033";
            case "Biñan": return "4024";
            case "Cabuyao": return "4025";
            case "Calamba": return "4027";
            case "Calauan": return "4012";
            case "Cavinti": return "4013";
            case "Famy": return "4014";
            case "Kalayaan": return "4015";
            case "Liliw": return "4038";
            case "Los Baños": return "4030";
            case "Luisiana": return "4032";
            case "Lumban": return "4015";
            case "Mabitac": return "4008";
            case "Magdalena": return "4009";
            case "Majayjay": return "4007";
            case "Nagcarlan": return "4006";
            case "Paete": return "4004";
            case "Pagsanjan": return "4005";
            case "Pakil": return "4003";
            case "Pangil": return "4016";
            case "Pila": return "4038";
            case "Rizal": return "4017";
            case "San Pablo": return "4000";
            case "San Pedro": return "4021";
            case "Santa Cruz": return "4003";
            case "Santa Maria": return "4019";
            case "Santa Rosa": return "4026";
            case "Siniloan": return "4018";
            case "Victoria": return "4038";
            default: return "";
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void clearInputFields() {
        binding.txtUserName.setText("");
        binding.contactNum.setText("");
        binding.cities.setText("");
        binding.barangays.setText("");
        binding.postalCode.setText("");
        binding.houseNum.setText("");
        Toast.makeText(getContext(), "Address fields cleared", Toast.LENGTH_SHORT).show();
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Clear existing error messages
        binding.txtUserName.setError(null);
        binding.contactNum.setError(null);
        binding.cities.setError(null);
        binding.barangays.setError(null);
        binding.postalCode.setError(null);
        binding.houseNum.setError(null);

        if (binding.txtUserName.getText().toString().trim().isEmpty()) {
            binding.txtUserName.setError("Full Name is required");
            isValid = false;
        }
        if (binding.contactNum.getText().toString().trim().isEmpty()) {
            binding.contactNum.setError("Phone Number is required");
            isValid = false;
        } else {
            String phoneNumber = binding.contactNum.getText().toString().trim();
            try {
                Long.parseLong(phoneNumber); // Check if it's numeric

                // Validate that it starts with "09" and has a length of 11
                if (!phoneNumber.startsWith("09") || phoneNumber.length() != 11) {
                    binding.contactNum.setError("Phone Number must start with '09' and be 11 digits long");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.contactNum.setError("Phone Number must be numeric");
                isValid = false;
            }
        }
        if (binding.cities.getText().toString().trim().isEmpty()) {
            binding.cities.setError("City is required");
            isValid = false;
        }
        if (binding.barangays.getText().toString().trim().isEmpty()) {
            binding.barangays.setError("Barangay is required");
            isValid = false;
        }
        if (binding.postalCode.getText().toString().trim().isEmpty()) {
            binding.postalCode.setError("Postal Code is required");
            isValid = false;
        }
        if (binding.houseNum.getText().toString().trim().isEmpty()) {
            binding.houseNum.setError("House Number is required");
            isValid = false;
        }

        return isValid;
    }

    public void numberMaxDigit() {
        binding.contactNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error message when the user starts typing
                binding.contactNum.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Prevent typing more than 11 digits
                if (s.length() > 11) {
                    s.delete(11, s.length()); // Remove excess characters
                }
            }
        });
    }

}
