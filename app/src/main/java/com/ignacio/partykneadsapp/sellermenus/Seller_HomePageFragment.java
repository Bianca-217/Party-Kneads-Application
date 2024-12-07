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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentSellersMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setActiveIcon(R.id.home); // Default active icon to Home on launch

        binding.home.setOnClickListener(v -> handleMenuClick(R.id.home, new SellerHome()));
        binding.orders.setOnClickListener(v -> handleMenuClick(R.id.orders, new OrderSellerSideFragment()));
        binding.addItem.setOnClickListener(v -> {
            if (isClickAllowed()) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_seller_HomePageFragment_to_add_Items);
            }
        });
        binding.notif.setOnClickListener(v -> handleMenuClick(R.id.notif, new NotificationFragment()));
        binding.profile.setOnClickListener(v -> {
            if (isClickAllowed()) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_seller_HomePageFragment_to_profileFragment);
                setActiveIcon(R.id.profile); // Set the profile icon as active
            }
        });
    }

    // Helper method to handle menu item clicks
    private void handleMenuClick(int menuId, Fragment fragment) {
        if (isClickAllowed()) {
            loadFragment(fragment);
            setActiveIcon(menuId); // Update the active icon based on the selected menu
        }
    }

    // Helper method to update icons based on selected menu
    private void setActiveIcon(int selectedId) {
        // Set the icons to show the filled or outlined state based on the selected menu item
        binding.home.setImageResource(selectedId == R.id.home ? R.drawable.home_filled : R.drawable.home_outline);
        binding.orders.setImageResource(selectedId == R.id.orders ? R.drawable.order_filled : R.drawable.order);
        binding.notif.setImageResource(selectedId == R.id.notif ? R.drawable.notif_filled : R.drawable.notif_outline);
        binding.profile.setImageResource(selectedId == R.id.profile ? R.drawable.person_filled : R.drawable.person_outline);
    }

    // Utility method to load fragments in `fragment_contseller`
    private void loadFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_contseller, fragment)
                .commit();
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
