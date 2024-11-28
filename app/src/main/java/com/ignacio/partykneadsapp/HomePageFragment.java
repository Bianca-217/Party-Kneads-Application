package com.ignacio.partykneadsapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ignacio.partykneadsapp.databinding.FragmentCustomersMenuPageBinding;
import com.ignacio.partykneadsapp.customermenus.LikesFragment;
import com.ignacio.partykneadsapp.customermenus.NotificationFragment;
import com.ignacio.partykneadsapp.customermenus.ShopFragment;
public class HomePageFragment extends Fragment {

    private FragmentCustomersMenuPageBinding binding;
    private long lastClickTime = 0; // Track the last click time
    private static final long CLICK_DEBOUNCE_TIME = 500; // 500 milliseconds debounce time

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean loadShopFragment = getArguments() != null && getArguments().getBoolean("loadShop", false);
        Fragment fragmentToLoad = loadShopFragment ? new ShopFragment() : new HomeFragment();

        if (savedInstanceState == null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_cont, fragmentToLoad)
                    .commit();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCustomersMenuPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setActiveIcon(R.id.home); // Default active icon to Home on launch

        binding.home.setOnClickListener(v -> handleMenuClick(R.id.home, new HomeFragment()));
        binding.likes.setOnClickListener(v -> handleMenuClick(R.id.likes, new LikesFragment()));
        binding.shop.setOnClickListener(v -> handleMenuClick(R.id.shop, new ShopFragment()));
        binding.notif.setOnClickListener(v -> handleMenuClick(R.id.notif, new NotificationFragment()));
        binding.profile.setOnClickListener(v -> {
            if (isClickAllowed()) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_homePageFragment_to_profileFragment);
                setActiveIcon(R.id.profile);
            }
        });
    }

    // Helper method to handle menu item clicks
    private void handleMenuClick(int menuId, Fragment fragment) {
        if (isClickAllowed()) {
            loadFragment(fragment);
            setActiveIcon(menuId);
        }
    }

    // Helper method to update icons based on selected menu
    private void setActiveIcon(int selectedId) {
        binding.homeMenu.setImageResource(selectedId == R.id.home ? R.drawable.home_filled : R.drawable.home_outline);
        binding.heartMenu.setImageResource(selectedId == R.id.likes ? R.drawable.favorite_filled : R.drawable.favorite_outline);
        binding.shopMenu.setImageResource(selectedId == R.id.shop ? R.drawable.shop : R.drawable.shop);
        binding.notifMenu.setImageResource(selectedId == R.id.notif ? R.drawable.notif_filled : R.drawable.notif_outline);
        binding.profileMenu.setImageResource(selectedId == R.id.profile ? R.drawable.person_filled : R.drawable.person_outline);
    }

    // Utility method to load fragments in `fragment_cont`
    private void loadFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_cont, fragment)
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