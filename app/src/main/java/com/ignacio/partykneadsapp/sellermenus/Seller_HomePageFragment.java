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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_contseller, new SellerHome()) // assuming Frag1 is FragmentOne
                    .commit();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.home.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_contseller, new SellerHome())
                    .commit();
        });
        binding.orders.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_contseller, new OrderSellerSideFragment())
                    .commit();
        });

        binding.addItem.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_seller_HomePageFragment_to_add_Items);
        });

        binding.notif.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_contseller, new NotificationFragment())
                    .commit();
        });

        binding.profile.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_seller_HomePageFragment_to_profileFragment);
        });

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentSellersMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
