package com.ignacio.partykneadsapp;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ignacio.partykneadsapp.adapters.CarouselAdapter;
import com.ignacio.partykneadsapp.adapters.CategoriesAdapter;
import com.ignacio.partykneadsapp.adapters.OrderHistoryAdapter;
import com.ignacio.partykneadsapp.adapters.PopularAdapter;
import com.ignacio.partykneadsapp.adapters.ProductShopAdapter;
import com.ignacio.partykneadsapp.adapters.SearchProductAdapter;
import com.ignacio.partykneadsapp.customermenus.ShopFragment;
import com.ignacio.partykneadsapp.databinding.FragmentHomeBinding;
import com.ignacio.partykneadsapp.model.CategoriesModel;
import com.ignacio.partykneadsapp.model.OrderHistoryModel;
import com.ignacio.partykneadsapp.model.PopularModel;
import com.ignacio.partykneadsapp.model.ProductShopModel;
import com.ignacio.partykneadsapp.model.SearchProduct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;


public class HomeFragment extends Fragment implements NavigationBarView.OnItemSelectedListener {

    private CarouselAdapter adapter;
    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private TextView txtUser;
    private Button btnLogout;
    private FirebaseUser user;

    private ImageView[] dots;
    private LinearLayout cl;
    private ViewPager2 viewPager;
    private CarouselAdapter carouselAdapter; // Declare CarouselAdapter here

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationTextView;

    private SearchProductAdapter productAdapter;
    private List<SearchProduct> productList;
    private SearchView searchView;

    private ProductShopAdapter productShopAdapter;
    private List<ProductShopModel> shopproductList = new ArrayList<>();
    private RecyclerView shopRecyclerView; // Correctly name your RecyclerView
    private CollectionReference productsRef;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore
        productsRef = firestore.collection("products");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false; // Implement navigation item selection if needed
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Initialize the CarouselAdapter
        int[] images = {
                R.drawable.homepage_slider,
                R.drawable.slider_two,
                R.drawable.slider_three,
        };

        carouselAdapter = new CarouselAdapter(images);
        viewPager = binding.viewPager;
        viewPager.setAdapter(carouselAdapter);

        // Set the current item to the first real item (index 1)
        viewPager.setCurrentItem(1, false);

        setupDotsIndicator(); // Setup dots after initializing adapter

        // Initialize Shop RecyclerView
        shopRecyclerView = binding.ShoprecyclerView;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        shopRecyclerView.setLayoutManager(gridLayoutManager);

        // If `productList` is already a class-level variable, don't re-declare it
        if (productList == null) {
            productList = new ArrayList<>();
        }

        productShopAdapter = new ProductShopAdapter(getContext(), shopproductList);
        shopRecyclerView.setAdapter(productShopAdapter);

        // Load the most sold product
        loadMostSoldProduct();

        // Enable infinite looping behavior
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Loop to the last slide when swiping left from the first item
                if (position == 0) {
                    viewPager.setCurrentItem(carouselAdapter.getRealItemCount(), false);
                }
                // Loop to the first slide when swiping right from the last item
                else if (position == carouselAdapter.getItemCount() - 1) {
                    viewPager.setCurrentItem(1, false);
                }
                updateDots(position);
            }
        });

        return binding.getRoot();
    }

    private void loadMostSoldProduct() {
        if (productsRef == null) {
            Log.e("HomeFragment", "productsRef is null. Ensure Firestore is initialized.");
            return;
        }

        // Fetch products with sold count greater than 5 and limit to 6 items
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereGreaterThan("sold", 5) // Only get products with sold count > 5
                .orderBy("sold", Query.Direction.DESCENDING) // Sort by sold count in descending order
                .limit(10) // Limit to top 6 products
                .get()
                .addOnSuccessListener(productSnapshots -> {
                    List<ProductShopModel> mostSoldProducts = new ArrayList<>();

                    for (QueryDocumentSnapshot productSnapshot : productSnapshots) {
                        String id = productSnapshot.getId();
                        String imageUrl = productSnapshot.getString("imageUrl");
                        String name = productSnapshot.getString("name");
                        String price = productSnapshot.getString("price");
                        String description = productSnapshot.getString("description");
                        String rate = productSnapshot.getString("rate");
                        String numreviews = productSnapshot.getString("numreviews");
                        String category = productSnapshot.getString("categories");

                        // Get the sold count for this product
                        Long soldCountObject = productSnapshot.getLong("sold");

                        // If soldCount is null, default it to 0
                        long soldCount = (soldCountObject != null) ? soldCountObject : 0;

                        // Add the product to the list
                        ProductShopModel product = new ProductShopModel(id, imageUrl, name, price, description, rate, numreviews, category, soldCount);
                        mostSoldProducts.add(product);
                    }

                    // If there are top sold products, update the RecyclerView
                    if (!mostSoldProducts.isEmpty()) {
                        productShopAdapter.updateData(mostSoldProducts);
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching products: " + e.getMessage()));
    }


    public int getItemCount() {
        return (shopproductList != null) ? shopproductList.size() : 0;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogout = view.findViewById(R.id.btnLogout);
        txtUser = view.findViewById(R.id.txtUserFname);
        user = mAuth.getCurrentUser();

        if (user == null) {
            // Redirect to login if user is not logged in
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_loginFragment_to_homePageFragment);
        } else {
            fetchUserFirstName(user.getUid());
        }

        binding.btnCart.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_cont);
            navController.navigate(R.id.action_homePageFragment_to_cartFragment);
        });

        cl = view.findViewById(R.id.clayout);
        cl.setOnClickListener(v -> hideKeyboard(view));

        binding.imgUserProfile.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_cont);
            navController.navigate(R.id.action_homePageFragment_to_profileFragment);
        });

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationTextView = view.findViewById(R.id.location);
        requestLocationPermissions();

        // Initialize RecyclerView and SearchView using binding
        recyclerView = binding.SearchrecyclerView;  // Reference RecyclerView from binding
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            productList = new ArrayList<>(); // Ensure productList is initialized
            productAdapter = new SearchProductAdapter(productList);
            recyclerView.setAdapter(productAdapter);
        } else {
            Log.e("HomeFragment", "RecyclerView is null from binding");
        }

        // Initialize SearchView
        searchView = view.findViewById(R.id.searchView);
        searchView.setQueryHint("Search products...");

// Navigate to ShopFragment when SearchView is clicked
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) { // Check if the SearchView is focused
                Bundle args = new Bundle();
                args.putBoolean("focusSearchView", true); // Pass flag to focus the SearchView
                Fragment shopFragment = new ShopFragment();
                shopFragment.setArguments(args); // Set arguments for the fragment

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_cont, shopFragment)
                        .commit();
            }
        });


    }



    private void searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllProducts();
            return;
        }

        // Normalize the keyword to uppercase for consistency
        String normalizedKeyword = keyword.trim().toUpperCase();
        Log.d("HomeFragment", "Searching for products with keyword: " + normalizedKeyword);

        // Simple query that fetches all products - we'll filter in memory
        firestore.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("HomeFragment", "Query successful, processing results");

                        productList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String productName = document.getString("name");

                                // Basic null check
                                if (productName == null) continue;

                                // Convert to uppercase for comparison
                                String upperProductName = productName.toUpperCase();

                                // For single letter search, check if it starts with the letter
                                if (normalizedKeyword.length() == 1) {
                                    if (upperProductName.startsWith(normalizedKeyword)) {
                                        SearchProduct product = document.toObject(SearchProduct.class);
                                        if (product != null) {
                                            productList.add(product);
                                            Log.d("HomeFragment", "Added product: " + productName);
                                        }
                                    }
                                }
                                // For multiple letters, check if it contains the search term
                                else {
                                    if (upperProductName.contains(normalizedKeyword)) {
                                        SearchProduct product = document.toObject(SearchProduct.class);
                                        if (product != null) {
                                            productList.add(product);
                                            Log.d("HomeFragment", "Added product: " + productName);
                                        }
                                    }
                                }
                            }

                            // Sort the results
                            Collections.sort(productList, (p1, p2) -> {
                                String name1 = p1.getName().toUpperCase();
                                String name2 = p2.getName().toUpperCase();
                                return name1.compareTo(name2);
                            });

                            Log.d("HomeFragment", "Found " + productList.size() + " products");
                            productAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchUserFirstName(String userId) {
        DocumentReference userDocRef = firestore.collection("Users").document(userId);

        userDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the first name and profile picture URL from Firestore
                        String fname = documentSnapshot.getString("First Name");
                        String firstName = documentSnapshot.getString("firstName");
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");

                        // Check if firstName is not null before displaying
                        if (fname == null || fname.isEmpty()) {
                            // If fname is null or empty, use the "firstName" field
                            fname = firstName;
                        }

                        // Check if the fragment is still added and use getView() to safely update UI
                        if (isAdded() && getView() != null) {
                            // Set the greeting text
                            txtUser.setText("Hi, " + capitalizeFirstLetter(fname) + "!");

                            // Load profile picture using Glide if available
                            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                                Glide.with(requireContext()) // Using requireContext() here is safe
                                        .load(profilePictureUrl)
                                        .placeholder(R.drawable.round_person_24) // Default placeholder
                                        .error(R.drawable.img_placeholder) // Error placeholder
                                        .into(binding.imgUserProfile);
                            } else {
                                // Set default image if no profile picture URL is found
                                binding.imgUserProfile.setImageResource(R.drawable.img_placeholder);
                            }
                        }

                    } else {
                        // Handle case where the document doesn't exist
                        if (isAdded() && getView() != null) {
                            txtUser.setText("Hi, No Document Found!");
                            binding.imgUserProfile.setImageResource(R.drawable.img_placeholder); // Set default image if no document found
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error fetching user data", e);
                    if (isAdded() && getView() != null) {
                        txtUser.setText("Hi, Error Fetching Data!");
                        binding.imgUserProfile.setImageResource(R.drawable.img_placeholder); // Set default image in case of error
                    }
                });
    }

    private void loadAllProducts() {
        firestore.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                SearchProduct product = document.toObject(SearchProduct.class);  // Use SearchProduct
                                if (product != null) {
                                    productList.add(product);
                                }
                            }
                            productAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Log error if load fails
                        Log.e("HomeFragment", "Error loading products: ", task.getException());
                    }
                });
    }


    private void setupDotsIndicator() {
        int realDotsCount = carouselAdapter.getRealItemCount();
        dots = new ImageView[realDotsCount];

        // Add dots to the LinearLayout
        for (int i = 0; i < realDotsCount; i++) {
            dots[i] = new ImageView(getActivity());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.non_active_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(4, 0, 4, 0);
            binding.dotsIndicator.addView(dots[i], params);
        }

        // Set the first dot as active
        if (dots.length > 0) {
            dots[0].setImageDrawable(getResources().getDrawable(R.drawable.active_dot));
        }
    }

    private void updateDots(int position) {
        int realPosition = position - 1; // Adjust for extra items
        if (realPosition < 0) realPosition = dots.length - 1;
        else if (realPosition >= dots.length) realPosition = 0;

        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.non_active_dot));
        }
        dots[realPosition].setImageDrawable(getResources().getDrawable(R.drawable.active_dot));
    }



    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationTextView.setText("Location permission denied.");
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        displayLocation(location);
                    } else {
                        locationTextView.setText("Unable to get location");
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    locationTextView.setText("Error getting location: " + e.getMessage());
                });
    }

    private void displayLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressDetails = new StringBuilder();

                // Append address details
                if (address.getLocality() != null) {
                    addressDetails.append(address.getLocality()).append(", ");
                }
                if (address.getSubAdminArea() != null) {
                    addressDetails.append(address.getSubAdminArea()).append(", ");
                }

                // Update UI and save location
                locationTextView.setText(addressDetails.toString());
                saveLocationToFirestore(addressDetails.toString());
            } else {
                locationTextView.setText("Unable to get location");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveLocationToFirestore(String location) {
        user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference userDocRef = firestore.collection("Users").document(user.getUid());

            // Create a reference to the "Locations" sub-collection
            CollectionReference locationsCollection = userDocRef.collection("Locations");

            // Check if the Locations sub-collection is empty
            locationsCollection.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // Add a new document with a unique ID for the location
                            Map<String, Object> locationData = new HashMap<>();
                            locationData.put("location", location);
                            locationData.put("status", "Active"); // Optional: Add a timestamp

                            locationsCollection.add(locationData)
                                    .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Location saved successfully."))
                                    .addOnFailureListener(e -> Log.e("HomeFragment", "Error saving location", e));
                        } else {
                            Log.d("HomeFragment", "Location not saved; Locations collection is not empty.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("HomeFragment", "Error checking existing locations", e));
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}