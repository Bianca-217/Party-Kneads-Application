package com.ignacio.partykneadsapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ignacio.partykneadsapp.adapters.BalloonColorAdapter;
import com.ignacio.partykneadsapp.databinding.FragmentBalloonClassicDescriptionBinding;
import com.ignacio.partykneadsapp.model.AddToCartModel;
import com.ignacio.partykneadsapp.model.BalloonColorModel;
import com.ignacio.partykneadsapp.model.CartItemModel;
import com.ignacio.partykneadsapp.model.CategoriesModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;

import java.util.ArrayList;
import java.util.List;


public class BalloonClassicDescription extends Fragment {
    private TextView productName, productPrice, productDescription, ratePercent, numReviews;
    private ImageView productImage, btnBack;
    private TextView quantityTextView;
    private Button btnAddtoCart, btnBuyNow; // Add to Cart and Buy Now buttons
    private ProductShopModel productShopModel;
    private FirebaseFirestore firestore;
    private ListenerRegistration productListener;
    private String color;
    private int quantity = 1; // Initial quantity

    FragmentBalloonClassicDescriptionBinding binding;
    private List<CategoriesModel> categoriesModelList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBalloonClassicDescriptionBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        // Get the passed arguments
        Bundle args = getArguments();
        if (args != null) {
            productShopModel = (ProductShopModel) args.getSerializable("detailed");
        }

        // Initialize views
        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        productDescription = view.findViewById(R.id.productDescription);
        ratePercent = view.findViewById(R.id.ratePercent);
        numReviews = view.findViewById(R.id.numReviews);
        btnBack = view.findViewById(R.id.btnBack);
        // Initialize quantity controls
        btnAddtoCart = view.findViewById(R.id.btnAddtoCart); // Initialize Add to Cart button
        btnBuyNow = view.findViewById(R.id.btnBuyNow); // Initialize Buy Now button

        // Set button click listener for "Add to Cart"
        btnAddtoCart.setOnClickListener(v -> showAddToCartDialog(null, 1));

        // Set button click listener for "Buy Now"
        btnBuyNow.setOnClickListener(v -> {

                    String productCategory1 = productShopModel.getCategory();

                    // Check if the category is "Balloons - Classic" or "Balloons - Latex"
                    if (productCategory1.equals("Balloons - Classic") || productCategory1.equals("Balloons - Latex")) {
                        // Directly show the second dialog without quantity and dropdown
                        handleBuyNow("", 1);
                    } else if (productShopModel.getCategory().equals("Balloons - Letter")) {
                        // Show the original dialog with quantity and dropdown
                        showLetterBuyDialog();
                    } else if (productShopModel.getCategory().equals("Balloons - Number")) {
                        showNumberBuyDialog();
                    } else if (productShopModel.getCategory().equals("Balloons - LED")) {
                        showLEDBuyDialog();
                    }
                }
        );


        if (productShopModel != null) {
            loadProductDetails(productShopModel.getId());
        }

        if (productShopModel.getCategory().equals("Balloons - Classic")) {
            color = "Assorted";
            setupBalloonColors();
        } else if(productShopModel.getCategory().equals("Balloons - Latex")) {
            color = "Assorted";
            setupBalloonLatexColors();
        } else if(productShopModel.getCategory().equals("Balloons - LED")) {
            color = "LED Blue";
            setupBalloonLEDColors();
        } else if(productShopModel.getCategory().equals("Balloons - Number")) {
            color = "Gold";
            setupBalloonNumberColors();
        } else if(productShopModel.getCategory().equals("Balloons - Letter")) {
            color = "Gold";
            setupBalloonNumberColors();
        }

        btnBack.setOnClickListener(v -> {
            Bundle args1 = new Bundle();
            args1.putBoolean("loadShop", true);

            // Replace the current fragment with the menu page fragment and pass the argument
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_balloonClassicDescription_to_homePageFragment, args1);
        });

        Button addToCartBtn = getView().findViewById(R.id.btnAddtoCart); // Your main "Add to Cart" button
        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the product category
                String productCategory = productShopModel.getCategory();

                // Check if the category is "Balloons - Classic" or "Balloons - Latex"
                if (productCategory.equals("Balloons - Classic") || productCategory.equals("Balloons - Latex")) {
                    // Directly show the second dialog without quantity and dropdown
                    showAddToCartDialog("Assorted", 1);
                } else if (productShopModel.getCategory().equals("Balloons - Letter")){
                    // Show the original dialog with quantity and dropdown
                    showLetterAddDialog();
                } else if (productShopModel.getCategory().equals("Balloons - Number")) {
                    showNumberAddDialog();
                } else if (productShopModel.getCategory().equals("Balloons - LED")) {
                    showLEDAddDialog();
                }
            }
        });

    }

    private void showLetterBuyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.numletterbuy_dialog, null);
        builder.setView(dialogView);

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set up the AutoCompleteTextView
        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.colorstxtview);

        // Create an ArrayAdapter using the letters from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Letters, android.R.layout.simple_dropdown_item_1line);

        // Set the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Initialize the quantity-related views
        TextView quantityTextView = dialogView.findViewById(R.id.quantity);
        TextView minusButton = dialogView.findViewById(R.id.minus);
        TextView plusButton = dialogView.findViewById(R.id.plus);

        // Set up initial quantity
        int[] quantity = {1}; // Use an array to modify the value within inner classes
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Handle the minus button click
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity[0]++;
                quantityTextView.setText(String.valueOf(quantity[0]));
            }
        });

        // Inside this dialog, find the confirmation button (addToCartBtn)
        Button confirmbuyBtnToCartBtn = dialogView.findViewById(R.id.buyBtn);
        confirmbuyBtnToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected letter from the AutoCompleteTextView
                String selectedLetter = autoCompleteTextView.getText().toString();

                // Check if the selected letter is empty
                if (selectedLetter.isEmpty()) {
                    // Show a warning message if no letter is selected
                    autoCompleteTextView.setError("Please select a letter");
                    autoCompleteTextView.requestFocus();
                    return; // Stop further execution until a letter is selected
                }

                // Your logic for confirming adding to cart with the selected letter and quantity
                handleBuyNow(selectedLetter, quantity[0]);

                // Dismiss the dialog after confirmation
                dialog.dismiss();
            }
        });

        dialog.show(); // Display the dialog
    }

    private void showNumberBuyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.numletterbuy_dialog, null);
        builder.setView(dialogView);

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set up the AutoCompleteTextView
        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.colorstxtview);

        // Create an ArrayAdapter using the letters from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Numbers, android.R.layout.simple_dropdown_item_1line);

        // Set the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Initialize the quantity-related views
        TextView quantityTextView = dialogView.findViewById(R.id.quantity);
        TextView minusButton = dialogView.findViewById(R.id.minus);
        TextView plusButton = dialogView.findViewById(R.id.plus);

        // Set up initial quantity
        int[] quantity = {1}; // Use an array to modify the value within inner classes
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Handle the minus button click
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity[0]++;
                quantityTextView.setText(String.valueOf(quantity[0]));
            }
        });

        // Inside this dialog, find the confirmation button (addToCartBtn)
        Button confirmbuyBtnToCartBtn = dialogView.findViewById(R.id.buyBtn);
        confirmbuyBtnToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected letter from the AutoCompleteTextView
                String selectedLetter = autoCompleteTextView.getText().toString();

                // Check if the selected letter is empty
                if (selectedLetter.isEmpty()) {
                    // Show a warning message if no letter is selected
                    autoCompleteTextView.setError("Please select a number");
                    autoCompleteTextView.requestFocus();
                    return; // Stop further execution until a letter is selected
                }

                // Your logic for confirming adding to cart with the selected letter and quantity
                handleBuyNow(selectedLetter, quantity[0]);

                // Dismiss the dialog after confirmation
                dialog.dismiss();
            }
        });

        dialog.show(); // Display the dialog
    }

    private void showLEDBuyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.quantobuy, null);
        builder.setView(dialogView);
        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Initialize the quantity-related views
        TextView quantityTextView = dialogView.findViewById(R.id.quantity);
        TextView minusButton = dialogView.findViewById(R.id.minus);
        TextView plusButton = dialogView.findViewById(R.id.plus);

        // Set up initial quantity
        int[] quantity = {1}; // Use an array to modify the value within inner classes
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Handle the minus button click
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity[0]++;
                quantityTextView.setText(String.valueOf(quantity[0]));
            }
        });

        // Inside this dialog, find the confirmation button (addToCartBtn)
        Button confirmAddToCartBtn = dialogView.findViewById(R.id.buyBtn);
        confirmAddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Your logic for confirming adding to cart with the selected letter and quantity
                handleBuyNow("", quantity[0]);

                // Dismiss the dialog after confirmation
                dialog.dismiss();
            }
        });

        dialog.show(); // Display the dialog
    }

    // Method to show the first dialog with quantity and dropdown
    private void showLetterAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.numletteradd_dialog, null);
        builder.setView(dialogView);

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set up the AutoCompleteTextView
        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.colorstxtview);

        // Create an ArrayAdapter using the letters from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Letters, android.R.layout.simple_dropdown_item_1line);

        // Set the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Initialize the quantity-related views
        TextView quantityTextView = dialogView.findViewById(R.id.quantity);
        TextView minusButton = dialogView.findViewById(R.id.minus);
        TextView plusButton = dialogView.findViewById(R.id.plus);

        // Set up initial quantity
        int[] quantity = {1}; // Use an array to modify the value within inner classes
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Handle the minus button click
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity[0]++;
                quantityTextView.setText(String.valueOf(quantity[0]));
            }
        });

        // Inside this dialog, find the confirmation button (addToCartBtn)
        Button confirmAddToCartBtn = dialogView.findViewById(R.id.addToCartBtn);
        confirmAddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected letter from the AutoCompleteTextView
                String selectedLetter = autoCompleteTextView.getText().toString();

                // Check if the selected letter is empty
                if (selectedLetter.isEmpty()) {
                    // Show a warning message if no letter is selected
                    autoCompleteTextView.setError("Please select a letter");
                    autoCompleteTextView.requestFocus();
                    return; // Stop further execution until a letter is selected
                }
                // Your logic for confirming adding to cart with the selected letter and quantity
                addToCartFunctionality(selectedLetter, quantity[0]);

                // Dismiss the dialog after confirmation
                dialog.dismiss();
            }
        });

        dialog.show(); // Display the dialog
    }

    // Method to show the first dialog with quantity and dropdown
    private void showNumberAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.numletteradd_dialog, null);
        builder.setView(dialogView);

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set up the AutoCompleteTextView
        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.colorstxtview);

        // Create an ArrayAdapter using the letters from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Numbers, android.R.layout.simple_dropdown_item_1line);

        // Set the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Initialize the quantity-related views
        TextView quantityTextView = dialogView.findViewById(R.id.quantity);
        TextView minusButton = dialogView.findViewById(R.id.minus);
        TextView plusButton = dialogView.findViewById(R.id.plus);

        // Set up initial quantity
        int[] quantity = {1}; // Use an array to modify the value within inner classes
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Handle the minus button click
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity[0]++;
                quantityTextView.setText(String.valueOf(quantity[0]));
            }
        });

        // Inside this dialog, find the confirmation button (addToCartBtn)
        Button confirmAddToCartBtn = dialogView.findViewById(R.id.addToCartBtn);
        confirmAddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected letter from the AutoCompleteTextView
                String selectedLetter = autoCompleteTextView.getText().toString();

                // Check if the selected letter is empty
                if (selectedLetter.isEmpty()) {
                    // Show a warning message if no letter is selected
                    autoCompleteTextView.setError("Please select a number");
                    autoCompleteTextView.requestFocus();
                    return; // Stop further execution until a letter is selected
                }

                // Your logic for confirming adding to cart with the selected letter and quantity
                addToCartFunctionality(selectedLetter, quantity[0]);

                // Dismiss the dialog after confirmation
                dialog.dismiss();
            }
        });

        dialog.show(); // Display the dialog
    }

    private void showLEDAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.quantocart_dialog, null);
        builder.setView(dialogView);
        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Initialize the quantity-related views
        TextView quantityTextView = dialogView.findViewById(R.id.quantity);
        TextView minusButton = dialogView.findViewById(R.id.minus);
        TextView plusButton = dialogView.findViewById(R.id.plus);

        // Set up initial quantity
        int[] quantity = {1}; // Use an array to modify the value within inner classes
        quantityTextView.setText(String.valueOf(quantity[0]));

        // Handle the minus button click
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    quantityTextView.setText(String.valueOf(quantity[0]));
                } else {
                    Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity[0]++;
                quantityTextView.setText(String.valueOf(quantity[0]));
            }
        });

        // Inside this dialog, find the confirmation button (addToCartBtn)
        Button confirmAddToCartBtn = dialogView.findViewById(R.id.addToCartBtn);
        confirmAddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Your logic for confirming adding to cart with the selected letter and quantity
                addToCartFunctionality("", quantity[0]);

                // Dismiss the dialog after confirmation
                dialog.dismiss();
            }
        });

        dialog.show(); // Display the dialog
    }

    // Define the method for adding to the cart with selected letter and quantity
    private void addToCartFunctionality(String selectedLetter, int quantityLetter) {
        // Example logic for adding the item to the cart
        showAddToCartDialog(selectedLetter, quantityLetter);
        // Implement your actual add-to-cart logic here
    }

    private void setupBalloonColors() {
        RecyclerView balloonColorsRecyclerView = binding.balloonColors;
        List<BalloonColorModel> balloonColorList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        balloonColorList.add(new BalloonColorModel(R.drawable.assorter, "Assorted"));
        balloonColorList.add(new BalloonColorModel(R.drawable.blue_balloon, "Blue"));
        balloonColorList.add(new BalloonColorModel(R.drawable.darkpink_balloon, "Dark Pink"));
        balloonColorList.add(new BalloonColorModel(R.drawable.green_balloon, "Green"));
        balloonColorList.add(new BalloonColorModel(R.drawable.pink_balloon, "Pink"));
        balloonColorList.add(new BalloonColorModel(R.drawable.yellow_balloon, "Yellow"));
        balloonColorList.add(new BalloonColorModel(R.drawable.white_balloon, "White"));
        balloonColorList.add(new BalloonColorModel(R.drawable.purple_balloon, "Purple"));
        balloonColorList.add(new BalloonColorModel(R.drawable.red_balloon, "Red"));

        // Initialize adapter and layout manager for balloon colors
        BalloonColorAdapter balloonColorAdapter = new BalloonColorAdapter(requireActivity(), balloonColorList, colorSelected -> {
            color = colorSelected;
        });

        balloonColorsRecyclerView.setAdapter(balloonColorAdapter);
        balloonColorsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        balloonColorsRecyclerView.setHasFixedSize(true);
        balloonColorsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupBalloonLatexColors() {
        RecyclerView balloonColorsRecyclerView = binding.balloonColors;
        List<BalloonColorModel> balloonColorList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_assorted, "Assorted"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_gold, "Gold"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_blue, "Blue"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_darkpink, "Dark Pink"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_green, "Green"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_pink, "Pink"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_purple, "Purple"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_rosegold, "Rosegold"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_silver, "Silver"));
        balloonColorList.add(new BalloonColorModel(R.drawable.metallic_teal, "Teal"));

        // Initialize adapter and layout manager for balloon colors
        BalloonColorAdapter balloonColorAdapter = new BalloonColorAdapter(requireActivity(), balloonColorList, colorSelected -> {
            color = colorSelected;
        });

        balloonColorsRecyclerView.setAdapter(balloonColorAdapter);
        balloonColorsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        balloonColorsRecyclerView.setHasFixedSize(true);
        balloonColorsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupBalloonLEDColors() {
        RecyclerView balloonColorsRecyclerView = binding.balloonColors;
        List<BalloonColorModel> balloonColorList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        balloonColorList.add(new BalloonColorModel(R.drawable.led_blue, "LED BLue"));
        balloonColorList.add(new BalloonColorModel(R.drawable.led_green, "LED Green"));
        balloonColorList.add(new BalloonColorModel(R.drawable.led_pink, "LED Pink"));
        balloonColorList.add(new BalloonColorModel(R.drawable.led_purple, "LED Purple"));
        balloonColorList.add(new BalloonColorModel(R.drawable.led_red, "LED Red"));
        balloonColorList.add(new BalloonColorModel(R.drawable.led_yellow, "LED Yellow"));

        // Initialize adapter and layout manager for balloon colors
        BalloonColorAdapter balloonColorAdapter = new BalloonColorAdapter(requireActivity(), balloonColorList, colorSelected -> {
            color = colorSelected;
        });

        balloonColorsRecyclerView.setAdapter(balloonColorAdapter);
        balloonColorsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        balloonColorsRecyclerView.setHasFixedSize(true);
        balloonColorsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupBalloonNumberColors() {
        RecyclerView balloonColorsRecyclerView = binding.balloonColors;
        List<BalloonColorModel> balloonColorList = new ArrayList<>();

        // Add balloon colors to the list (Images should exist in drawable folder)
        balloonColorList.add(new BalloonColorModel(R.drawable.gold_num, "Gold"));
        balloonColorList.add(new BalloonColorModel(R.drawable.sil_num, "Silver"));
        balloonColorList.add(new BalloonColorModel(R.drawable.rose_num, "Rose Gold"));
        balloonColorList.add(new BalloonColorModel(R.drawable.cream_num, "Cream"));
        balloonColorList.add(new BalloonColorModel(R.drawable.pink_num, "Pink"));

        // Initialize adapter and layout manager for balloon colors
        BalloonColorAdapter balloonColorAdapter = new BalloonColorAdapter(requireActivity(), balloonColorList, colorSelected -> {
            color = colorSelected;
        });

        balloonColorsRecyclerView.setAdapter(balloonColorAdapter);
        balloonColorsRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false));
        balloonColorsRecyclerView.setHasFixedSize(true);
        balloonColorsRecyclerView.setNestedScrollingEnabled(false);
    }



    private void showAddToCartDialog(String selectedLetter, int quantityLetter) {
        // Create a dialog
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.addtocartdialog); // Inflate your dialog layout
        dialog.setCancelable(true);

        // Make the dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Reference to FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Automatically add item to the cart when the dialog is shown
        if (userId != null) {
            saveCartItem(userId, selectedLetter, quantityLetter); // Method to add to cart
        } else {
            Toast.makeText(getActivity(), "Please log in to add items to the cart.", Toast.LENGTH_SHORT).show();
        }

        Button btnCart = dialog.findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
            goToCart(); // Navigate to the cart fragment
        });

        // Use findViewById to get the TextView and set an OnClickListener
        TextView btnContinue = dialog.findViewById(R.id.btnContinue); // Keep it as TextView
        btnContinue.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
            goToShop(); // Navigate back to the shop fragment
        });

        // Show the dialog
        dialog.show();
    }

    private void saveCartItem(String userId, String selectedLetter, int quantityLetter) {
        // Extract the product details
        String priceText = productPrice.getText().toString();
        int unitPrice = Integer.parseInt(priceText.replaceAll("[^\\d]", ""));
        int totalPrice = unitPrice * quantityLetter;
        String imageUrl = productShopModel.getimageUrl();
        long timestamp = System.currentTimeMillis();
        String productName = productShopModel.getName();
        String uniqueKey = color + " | " + selectedLetter; // Create a unique key for color and letter combination

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if the product with the same color and letter already exists in the cart
        db.collection("Users")
                .document(userId)
                .collection("cartItems")
                .whereEqualTo("productName", productName)
                .whereEqualTo("cakeSize", uniqueKey)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Item exists, update the quantity and totalPrice
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String docId = document.getId();
                            int existingQuantity = document.getLong("quantity").intValue();
                            String existingTotalPriceStr = document.getString("totalPrice");

                            // Calculate the new quantity and total price
                            int newQuantity = existingQuantity + quantityLetter;
                            int existingTotalPrice = Integer.parseInt(existingTotalPriceStr.replaceAll("[^\\d]", ""));
                            int newTotalPrice = existingTotalPrice + (unitPrice * quantityLetter);

                            // Update the existing cart item
                            db.collection("Users")
                                    .document(userId)
                                    .collection("cartItems")
                                    .document(docId)
                                    .update("quantity", newQuantity, "totalPrice", "₱" + newTotalPrice)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully updated the cart item
                                        Toast.makeText(getActivity(), "Cart item updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Failed to update cart item.", Toast.LENGTH_SHORT).show();
                                        Log.e("CartItem", "Error updating cart item: ", e);
                                    });
                        }
                    } else {
                        // Item doesn't exist, create a new cart item
                        AddToCartModel cartItem = new AddToCartModel(
                                productShopModel.getId(),
                                productName,
                                uniqueKey,
                                quantityLetter,
                                "₱" + totalPrice,
                                imageUrl,
                                timestamp
                        );

                        // Add the new item to the cart
                        db.collection("Users")
                                .document(userId)
                                .collection("cartItems")
                                .add(cartItem)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getActivity(), "Item added to cart", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Failed to add item to cart.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CartItem", "Error fetching cart items: ", e);
                });
    }



    private void handleBuyNow(String selectedNumLetter, int quantityNumLetter) {
        // Create a CartItemModel for the selected cake and quantity
        int price = Integer.parseInt(productPrice.getText().toString().replace("₱", ""));
        int totalPrice = price * quantityNumLetter;

        // Create a CartItemModel for this selection
        CartItemModel cartItem = new CartItemModel(
                productShopModel.getId(),
                productShopModel.getName(),
                color + " | " + selectedNumLetter,
                quantityNumLetter,
                "₱" + totalPrice, // Include price with quantity
                productShopModel.getimageUrl() // Product Image URL
        );

        // Bundle the cart item to pass to the CheckoutFragment
        List<CartItemModel> selectedItems = new ArrayList<>();
        selectedItems.add(cartItem);

        // Log the details of the cart item
        Log.d("CartItem", "Item: " + cartItem.getProductName() + ", Quantity: " + cartItem.getQuantity() + ", Total Price: " + cartItem.getTotalPrice());

        // Create a Bundle to pass the selected items to CheckoutFragment
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("selectedItems", (ArrayList<? extends Parcelable>) selectedItems);

        // Navigate to CheckoutFragment and pass the selected items
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_balloonClassicDescription_to_checkoutFragment, bundle);
    }

    private void loadProductDetails(String productId) {
        DocumentReference productRef = firestore.collection("products").document(productId);

        productListener = productRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle the error
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Fetch and display product details
                    String imageUrl = documentSnapshot.getString("imageUrl"); // Fetch the imageUrl
                    String name = documentSnapshot.getString("name");
                    String description = documentSnapshot.getString("description");
                    String rate = documentSnapshot.getString("rate");
                    String numReviewsStr = documentSnapshot.getString("numreviews");
                    String price = documentSnapshot.getString("price");

                    // Populate the views with data
                    Glide.with(BalloonClassicDescription.this).load(imageUrl).into(productImage); // Load image with Glide
                    productName.setText(name);
                    productDescription.setText(description);
                    ratePercent.setText(rate);
                    numReviews.setText(numReviewsStr);
                    productPrice.setText("₱" +  price);
                    productShopModel.setimageUrl(imageUrl); // Save image URL to productShopModel
                }
            }
        });
    }

    private void goToCart() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_balloonClassicDescription_to_cartFragment);
    }

    private void goToShop() {

        // Create a bundle with the flag to load ShopFragment
        Bundle args = new Bundle();
        args.putBoolean("loadShop", true);

        // Replace the current fragment with the menu page fragment and pass the argument
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_balloonClassicDescription_to_homePageFragment, args);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the listener to prevent memory leaks
        if (productListener != null) {
            productListener.remove();
        }
    }
}