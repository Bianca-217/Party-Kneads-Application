package com.ignacio.partykneadsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.ignacio.partykneadsapp.adapters.FragmentViewPagerAdapter;


public class OrderHistory extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout first
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        // Initialize tabLayout and viewPager using the inflated view
        tabLayout = view.findViewById(R.id.tablayout);
        viewPager = view.findViewById(R.id.viewpager);

        // Set up the adapter for the ViewPager
        FragmentViewPagerAdapter fragmentViewPagerAdapter = new FragmentViewPagerAdapter(
                getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        );
        fragmentViewPagerAdapter.addFragment(new PendingFragment(), "Pending");
        fragmentViewPagerAdapter.addFragment(new ToShipFragment(), "To Ship");
        fragmentViewPagerAdapter.addFragment(new ToReceiveFragment(), "To Receive");
        fragmentViewPagerAdapter.addFragment(new CompletedFragment(), "Completed");

        // Link ViewPager to the adapter and TabLayout
        viewPager.setAdapter(fragmentViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }
}
