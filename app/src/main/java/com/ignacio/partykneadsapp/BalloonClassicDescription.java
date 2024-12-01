package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
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
    private TextView productName, productPrice, productDescription;
    private ImageView productImage, btnBack;
    private TextView quantityTextView, stockValue;
    private Button btnAddtoCart, btnBuyNow; // Add to Cart and Buy Now buttons
    private ProductShopModel productShopModel;
    private FirebaseFirestore firestore;
    private ListenerRegistration productListener;
    private String color;
    private int quantity = 1;
    private ConstraintLayout cl;// Initial quantity

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

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(v));

        firestore = FirebaseFirestore.getInstance();

        // Get the passed arguments
        Bundle args = getArguments();
        if (args != null) {
            productShopModel = (ProductShopModel) args.getSerializable("detailed");
        }

        // Initialize views
        stockValue = view.findViewById(R.id.stockValue);
        productImage = view.findViewById(R.id.productImage);
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        productDescription = view.findViewById(R.id.productDescription);
        btnBack = view.findViewById(R.id.btnBack);
        // Initialize quantity controls
        btnAddtoCart = view.findViewById(R.id.btnAddtoCart); // Initialize Add to Cart button
        btnBuyNow = view.findViewById(R.id.btnBuyNow); // Initialize Buy Now button

        // Set button click listener for "Add to Cart"
        btnAddtoCart.setOnClickListener(v ->
                showAddToCartDialog(null, 1)
        );

        // Set button click listener for "Buy Now"
        btnBuyNow.setOnClickListener(v -> {
            String stock = stockValue.getText().toString(); // Get the stock value from the TextView

            // First check if stock is available
            if ("0".equals(stock) || "Out of Stock".equals(stock)) {
                // Show "Out of Stock" dialog
                showNoStockDialog();
            } else {
                // Proceed based on product category
                String productCategory1 = productShopModel.getCategory();

                // Check if the category is "Balloons - Classic" or "Balloons - Latex"
                if (productCategory1.equals("Balloons - Classic") || productCategory1.equals("Balloons - Latex")) {
                    // Proceed directly with checkout if category matches, no need for a dialog
                    handleBuyNow("", 1);
                } else if (productCategory1.equals("Balloons - Letter")) {
                    // Show dialog for Letter category and proceed when confirmed
                    String productId = productShopModel.getProductId(); // Fetch productId using the correct getter
                    showLetterBuyDialog(productId); // Pass productId to showLetterBuyDialog
                } else if (productCategory1.equals("Balloons - Number")) {
                    // Show dialog for Number category and proceed when confirmed
                    showNumberBuyDialog();
                } else if (productCategory1.equals("Balloons - LED")) {
                    // Show dialog for LED category and proceed when confirmed
                    showLEDBuyDialog();
                }
            }
        });


        if (productShopModel != null) {
            loadProductDetails(productShopModel.getId());
        }

        if (productShopModel.getCategory().equals("Balloons - Classic")) {
            color = "Assorted";
            setupBalloonColors();
        } else if (productShopModel.getCategory().equals("Balloons - Latex")) {
            color = "Assorted";
            setupBalloonLatexColors();
        } else if (productShopModel.getCategory().equals("Balloons - LED")) {
            color = "LED Blue";
            setupBalloonLEDColors();
        } else if (productShopModel.getCategory().equals("Balloons - Number")) {
            color = "Gold";
            setupBalloonNumberColors();
        } else if (productShopModel.getCategory().equals("Balloons - Letter")) {
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
                    showAddToCartDialog("color", 1);
                } else if (productShopModel.getCategory().equals("Balloons - Letter")) {
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

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showNoStockDialog() {
        // Create the dialog for "Out of Stock"
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.nostock_dialog); // Inflate your "Out of Stock" dialog layout
        dialog.setCancelable(true);

        // Make the dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Set button click listener for the dialog
        Button btnClose = dialog.findViewById(R.id.btnClose); // Assuming there's a "Close" button in the dialog
        btnClose.setOnClickListener(v -> dialog.dismiss()); // Dismiss the dialog when clicked

        // Show the dialog
        dialog.show();
    }

    private void showLetterBuyDialog(String productId) {
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

        // Initialize the stock quantity
        final int[] maxQuantity = {1}; // Default to 1 if stock is unavailable
        quantityTextView.setText(String.valueOf(maxQuantity[0]));

        // Fetch the product's stock from Firestore (using the productId passed dynamically)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference productRef = db.collection("products").document(productId);

        productRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Fetch the stock value from Firestore
                String stock = documentSnapshot.getString("stock");
                if (stock != null) {
                    try {
                        maxQuantity[0] = Integer.parseInt(stock); // Set the max quantity from Firestore stock
                    } catch (NumberFormatException e) {
                        maxQuantity[0] = 1; // Fallback to 1 if the stock is not a valid number
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // Handle the error (e.g., show a Toast or set maxQuantity to a default value)
            maxQuantity[0] = 1;
        });

        // Handle the minus button click
        minusButton.setOnClickListener(v -> {
            if (maxQuantity[0] > 1) {
                int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
                if (currentQuantity > 1) {
                    currentQuantity--;
                    quantityTextView.setText(String.valueOf(currentQuantity));
                } else {
                    Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
            if (currentQuantity < maxQuantity[0]) {
                currentQuantity++;
                quantityTextView.setText(String.valueOf(currentQuantity));
            } else {
                Toast.makeText(getContext(), "Cannot exceed stock limit", Toast.LENGTH_SHORT).show();
            }
        });

        // Inside this dialog, find the confirmation button (buyBtn)
        Button confirmBuyBtn = dialogView.findViewById(R.id.buyBtn);
        confirmBuyBtn.setOnClickListener(v -> {
            // Get the selected letter from the AutoCompleteTextView
            String selectedLetter = autoCompleteTextView.getText().toString();

            // Check if the selected letter is empty
            if (selectedLetter.isEmpty()) {
                // Show a warning message if no letter is selected
                autoCompleteTextView.setError("Please select a letter");
                autoCompleteTextView.requestFocus();
                return; // Stop further execution until a letter is selected
            }

            // Handle the purchase with the selected letter and quantity
            handleBuyNow(selectedLetter, Integer.parseInt(quantityTextView.getText().toString()));

            // Dismiss the dialog after confirmation
            dialog.dismiss();
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

        // Initialize the stock quantity
        final int[] maxQuantity = {1}; // Default to 1 if stock is unavailable
        quantityTextView.setText(String.valueOf(maxQuantity[0]));

        // Fetch the stock quantity from Firestore (using a static value for now)
        String stock = "10"; // You can replace this with a reference to your stock value (e.g., Firestore data)

        // Parse the stock value (this can come from Firestore or another data source)
        int availableStock = 0;
        try {
            availableStock = Integer.parseInt(stock);
        } catch (NumberFormatException e) {
            // Handle invalid stock value
            Toast.makeText(getContext(), "Invalid stock value", Toast.LENGTH_SHORT).show();
            return; // Exit the method if the stock is invalid
        }

        // If stock is 0 or out of stock, show a message and return
        if (availableStock <= 0) {
            Toast.makeText(getContext(), "Out of Stock", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set the maxQuantity based on the available stock
        maxQuantity[0] = availableStock;

        // Handle the minus button click
        minusButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
            if (currentQuantity > 1) {
                currentQuantity--;
                quantityTextView.setText(String.valueOf(currentQuantity));
            } else {
                Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
            if (currentQuantity < maxQuantity[0]) {
                currentQuantity++;
                quantityTextView.setText(String.valueOf(currentQuantity));
            } else {
                Toast.makeText(getContext(), "Cannot exceed available stock", Toast.LENGTH_SHORT).show();
            }
        });

        // Inside this dialog, find the confirmation button (buyBtn)
        FrameLayout confirmBuyBtnToCartBtn = dialogView.findViewById(R.id.buyBtn);
        confirmBuyBtnToCartBtn.setOnClickListener(v -> {
            // Handle the purchase with the selected quantity
            handleBuyNow("", Integer.parseInt(quantityTextView.getText().toString()));

            // Dismiss the dialog after confirmation
            dialog.dismiss();
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

        // Initialize the stock quantity
        final int[] maxQuantity = {1}; // Default to 1 if stock is unavailable
        quantityTextView.setText(String.valueOf(maxQuantity[0]));

        // Fetch the stock quantity from Firestore (using product ID or a static value)
        String stock = stockValue.getText().toString(); // Assume `stockValue` is a valid reference to the stock data

        // Parse the stock from the TextView (this can be fetched from Firestore or another source)
        int availableStock = 0;
        try {
            availableStock = Integer.parseInt(stock);
        } catch (NumberFormatException e) {
            // Handle invalid stock value
            Toast.makeText(getContext(), "Invalid stock value", Toast.LENGTH_SHORT).show();
            return; // Exit the method if the stock is invalid
        }

        // If stock is 0 or out of stock, show a message and return
        if (availableStock <= 0) {
            Toast.makeText(getContext(), "Out of Stock", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set the maxQuantity based on the available stock
        maxQuantity[0] = availableStock;

        // Handle the minus button click
        minusButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
            if (currentQuantity > 1) {
                currentQuantity--;
                quantityTextView.setText(String.valueOf(currentQuantity));
            } else {
                Toast.makeText(getContext(), "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle the plus button click
        plusButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
            if (currentQuantity < maxQuantity[0]) {
                currentQuantity++;
                quantityTextView.setText(String.valueOf(currentQuantity));
            } else {
                Toast.makeText(getContext(), "Cannot exceed available stock", Toast.LENGTH_SHORT).show();
            }
        });

        // Inside this dialog, find the confirmation button (buyBtn)
        Button confirmAddToCartBtn = dialogView.findViewById(R.id.buyBtn);
        confirmAddToCartBtn.setOnClickListener(v -> {
            // Handle the purchase with the selected quantity
            handleBuyNow("", Integer.parseInt(quantityTextView.getText().toString()));

            // Dismiss the dialog after confirmation
            dialog.dismiss();
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

        String stock = stockValue.getText().toString(); // Get current stock from UI

        // Check if stock is available
        if ("0".equals(stock) || "Out of Stock".equals(stock)) {
            // Show the out of stock dialog if no stock is available
            showNoStockDialog();
        } else {
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
    }

    private void saveCartItem(String userId, String selectedLetter, int quantityLetter) {
        // Extract the product details
        String priceText = productPrice.getText().toString();
        int unitPrice = Integer.parseInt(priceText.replaceAll("[^\\d]", ""));
        int totalPrice = unitPrice * quantityLetter;
        String imageUrl = productShopModel.getimageUrl();
        long timestamp = System.currentTimeMillis();
        String productName = productShopModel.getName();
        String uniqueKey = selectedLetter + " | " + color; // Create a unique key for color and letter combination

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String stock = stockValue.getText().toString(); // Get current stock from UI

        // Check if stock is available
        if ("0".equals(stock) || "Out of Stock".equals(stock)) {
            // Show the out of stock dialog if no stock is available
            showNoStockDialog();
        } else {
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
    }

    private void handleBuyNow(String selectedNumLetter, int quantityNumLetter) {
        // Get the stock value from the UI (stockValue TextView)
        String stockStr = stockValue.getText().toString();

        // Check if stock is available
        if ("0".equals(stockStr) || "Out of Stock".equals(stockStr)) {
            // Show "Out of Stock" dialog if no stock is available
            showNoStockDialog();
        } else {
            // Parse the stock and ensure it's a valid number
            int currentStock = 0;
            try {
                currentStock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                Log.e("StockParseError", "Invalid stock value: " + stockStr);
            }

            // Check if there is enough stock available
            if (currentStock > 0) {
                // Reduce stock by 1
                int updatedStock = currentStock - 1;

                // Update the stock in Firestore
                updateStockInFirestore(updatedStock);

                // Get the product price and calculate the total price based on quantity
                int price = Integer.parseInt(productPrice.getText().toString().replace("₱", ""));
                int totalPrice = price * quantityNumLetter;

                // Create the CartItemModel for this item
                CartItemModel cartItem = new CartItemModel(
                        productShopModel.getId(),
                        productShopModel.getName(),
                        color + " | " + selectedNumLetter,
                        quantityNumLetter,
                        "₱" + totalPrice, // Include price with quantity
                        productShopModel.getimageUrl() // Product Image URL
                );

                // Create a list to pass the cart item to the CheckoutFragment
                List<CartItemModel> selectedItems = new ArrayList<>();
                selectedItems.add(cartItem);

                // Log the details of the cart item for debugging
                Log.d("CartItem", "Item: " + cartItem.getProductName() + ", Quantity: " + cartItem.getQuantity() + ", Total Price: " + cartItem.getTotalPrice());

                // Create a Bundle to pass the selected items to CheckoutFragment
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("selectedItems", (ArrayList<? extends Parcelable>) selectedItems);

                // Navigate to the CheckoutFragment with the selected items
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_balloonClassicDescription_to_checkoutFragment, bundle);
            } else {
                // If stock is 0 or less, show out of stock message
                showNoStockDialog();
            }
        }
    }

    private void updateStockInFirestore(int newStock) {
        // Get the product ID from the productShopModel
        String productId = productShopModel.getId();

        // Get the reference to the Firestore product document
        DocumentReference productRef = firestore.collection("products").document(productId);

        // Update the stock value in Firestore
        productRef.update("stock", String.valueOf(newStock))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Stock", "Stock updated successfully: " + newStock);
                    // Optionally update the UI with the new stock value
                    stockValue.setText(String.valueOf(newStock));
                })
                .addOnFailureListener(e -> {
                    Log.e("Stock", "Error updating stock: ", e);
                    Toast.makeText(getActivity(), "Failed to update stock", Toast.LENGTH_SHORT).show();
                });
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
                    // Fetch product details from Firestore
                    String imageUrl = documentSnapshot.getString("imageUrl");
                    String name = documentSnapshot.getString("name");
                    String description = documentSnapshot.getString("description");
                    String rate = documentSnapshot.getString("rate");
                    String numReviewsStr = documentSnapshot.getString("numreviews");
                    String price = documentSnapshot.getString("price");

                    // Fetch the stock value from Firestore
                    String stock = documentSnapshot.getString("stock"); // Assuming "stock" is the field name in Firestore

                    // Populate the views with data
                    Glide.with(BalloonClassicDescription.this).load(imageUrl).into(productImage);
                    productName.setText(name);
                    productDescription.setText(description);
                    productPrice.setText("₱" + price);
                    productShopModel.setimageUrl(imageUrl);

                    // Set the stock value to stockValue TextView
                    if (stock != null) {
                        stockValue.setText(stock); // Set the stock value (assuming "stock" is a string number)
                    } else {
                        stockValue.setText("Out of Stock");
                    }
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