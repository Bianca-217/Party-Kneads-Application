package com.ignacio.partykneadsapp.sellermenus;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.adapters.FragmentViewPagerAdapter;
import com.ignacio.partykneadsapp.PendingSellerSideFragment;
import com.ignacio.partykneadsapp.DeliverOrderFragment;
import com.ignacio.partykneadsapp.CompleteOrderFragment;
import com.ignacio.partykneadsapp.CancelledOrderFragment;


public class OrderSellerSideFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView btnRefresh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout first
        View view = inflater.inflate(R.layout.fragment_order_seller_side, container, false);

        // Initialize tabLayout and viewPager using the inflated view
        tabLayout = view.findViewById(R.id.tablayout);
        viewPager = view.findViewById(R.id.viewpager);

        // Set up the adapter for the ViewPager
        FragmentViewPagerAdapter fragmentViewPagerAdapter = new FragmentViewPagerAdapter(
                getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        );
        fragmentViewPagerAdapter.addFragment(new PendingSellerSideFragment(), "Pending");
        fragmentViewPagerAdapter.addFragment(new DeliverOrderFragment(), "To Deliver");
        fragmentViewPagerAdapter.addFragment(new CompleteOrderFragment(), "Completed");
        fragmentViewPagerAdapter.addFragment(new CancelledOrderFragment(), "Cancelled");

        // Link ViewPager to the adapter and TabLayout
        viewPager.setAdapter(fragmentViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(v -> reloadFragment());

        return view;
    }
    private void reloadFragment() {
        // Reload the orderSellerSideFragment within the seller's homepage
        Fragment orderSellerSideFragment = new OrderSellerSideFragment(); // Create a new instance
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_contseller, orderSellerSideFragment) // Replace the frame layout's content
                .commit();
    }

}
