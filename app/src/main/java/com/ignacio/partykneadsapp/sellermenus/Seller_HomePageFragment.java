package com.ignacio.partykneadsapp.sellermenus;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentSellersMenuBinding;
import com.ignacio.partykneadsapp.customermenus.LikesFragment;
import com.ignacio.partykneadsapp.customermenus.NotificationFragment;

public class Seller_HomePageFragment extends Fragment {

    private FragmentSellersMenuBinding binding;
    private long lastClickTime = 0; // Track the last click time
    private static final long CLICK_DEBOUNCE_TIME = 500; // 500 milliseconds debounce time

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_contseller, new SellerHome())
                    .commit();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.home.setOnClickListener(v -> handleMenuClick(new SellerHome()));
        binding.orders.setOnClickListener(v -> handleMenuClick(new OrderSellerSideFragment()));
        binding.addItem.setOnClickListener(v -> {
            if (isClickAllowed()) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_seller_HomePageFragment_to_add_Items);
            }
        });
        binding.notif.setOnClickListener(v -> handleMenuClick(new NotificationFragment()));
        binding.profile.setOnClickListener(v -> {
            if (isClickAllowed()) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_seller_HomePageFragment_to_profileFragment);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentSellersMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Helper method to handle fragment replacement with debounce
    private void handleMenuClick(Fragment fragment) {
        if (isClickAllowed()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_contseller, fragment)
                    .commit();
        }
    }

    // Check if enough time has passed since the last click
    private boolean isClickAllowed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= CLICK_DEBOUNCE_TIME) {
            lastClickTime = currentTime;
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}