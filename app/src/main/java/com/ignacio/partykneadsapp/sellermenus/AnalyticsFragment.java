package com.ignacio.partykneadsapp.sellermenus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.databinding.FragmentAnalyticsBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalyticsFragment extends Fragment {
    private FragmentAnalyticsBinding binding;
    private BarChart barChart;
    private BarChart secondaryBarChart; // New BarChart for hourly data
    private CollectionReference ordersRef;

    private static final String PREFS_NAME = "AnalyticsPrefs";
    private static final String LAST_DAY_KEY = "lastDay";
    private static final String LAST_WEEK_KEY = "lastWeek";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the bar charts
        barChart = binding.barChart;
        secondaryBarChart = binding.secondaryBarChart;

        // Setup Firestore reference
        ordersRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document("QqqccLchjigd0C7zf8ewPXY0KZc2")
                .collection("Orders");

        // Fetch daily revenue
        fetchDailyRevenue();

        // Fetch hourly revenue for a single day
        fetchHourlyRevenue();

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_analyticsFragment_to_seller_HomePageFragment);
        });
    }

    private void fetchHourlyRevenue() {
        // Define the hourly intervals
        List<String> hourlyIntervals = Arrays.asList(
                "8 AM", "9 AM", "10 AM", "11 AM", "12 PM",
                "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM"
        );
        List<BarEntry> hourlyEntries = new ArrayList<>();

        // Get today's date in the required format (only month and day)
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
        String todayDate = sdf.format(new Date());

        // Retrieve the last stored date from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastSavedDay = prefs.getString(LAST_DAY_KEY, "");

        if (!todayDate.equals(lastSavedDay)) {
            // Reset the hourly chart data if it's a new day
            resetHourlyData();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LAST_DAY_KEY, todayDate);
            editor.apply();
        }

        // Real-time listener for orders
        ordersRef.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("AnalyticsFragment", "Error listening for updates: ", error);
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                // Clear previous entries
                hourlyEntries.clear();

                // Loop through the hourly intervals and calculate revenue
                for (int i = 0; i < hourlyIntervals.size(); i++) {
                    String interval = hourlyIntervals.get(i);
                    double hourlyRevenue = 0.0;

                    // Loop through documents to sum the revenue for the current interval
                    for (DocumentSnapshot document : documents) {
                        if (document.exists()) {
                            String timestamp = document.getString("timestamp");
                            String totalPrice = document.getString("totalPrice");

                            if (timestamp != null && totalPrice != null) {
                                // Extract the date and hour from the timestamp
                                String orderDate = extractDateFromTimestamp(timestamp);
                                String orderHour = extractHourFromTimestamp(timestamp);

                                // Check if the order matches today's date and the current hour interval
                                if (orderDate.equals(todayDate) && orderHour.equals(interval)) {
                                    try {
                                        double totalPriceDouble = Double.parseDouble(totalPrice.replace("₱", "").trim());
                                        hourlyRevenue += totalPriceDouble;
                                    } catch (NumberFormatException e) {
                                        Log.e("AnalyticsFragment", "Invalid totalPrice format: " + totalPrice, e);
                                    }
                                }
                            }
                        }
                    }

                    // Add the hourly revenue to the entries
                    hourlyEntries.add(new BarEntry(i, (float) hourlyRevenue));
                }

                // Update the secondary chart for hourly revenue
                updateSecondaryBarChart(hourlyEntries, hourlyIntervals);
            } else {
                Log.e("AnalyticsFragment", "No documents found");
            }
        });
    }

    private void resetHourlyData() {
        // Reset data for hourly chart
        secondaryBarChart.clear();
    }

    private void updateSecondaryBarChart(List<BarEntry> entries, List<String> hourlyIntervals) {
        // Ensure entries are sorted by X-axis values
        entries.sort((e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        BarDataSet dataSet = new BarDataSet(entries, "Hourly Revenue");
        dataSet.setColor(Color.GREEN);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // Set bar width

        secondaryBarChart.setData(barData);
        secondaryBarChart.setFitBars(true);

        // Disable the legend
        secondaryBarChart.getLegend().setEnabled(false);

        // Customize X-axis
        XAxis xAxis = secondaryBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < hourlyIntervals.size()) {
                    return hourlyIntervals.get(index);
                }
                return "";
            }
        });

        // Customize chart appearance
        secondaryBarChart.getDescription().setEnabled(false);
        secondaryBarChart.setDrawGridBackground(false);
        secondaryBarChart.invalidate();

        // Add the title showing the current day
        String currentDay = getCurrentDay(); // Method to get the current day (e.g., "Monday, Dec 08")
        TextView titleTextView = binding.getRoot().findViewById(R.id.chart_title_textview); // Assuming you have a TextView with id "chart_title_textview" in your layout
        titleTextView.setText("Hourly Revenue - " + currentDay);
    }

    private String getCurrentDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd", Locale.ENGLISH); // Format to display day of the week and date (e.g., "Monday, Dec 08")
        Date currentDate = new Date();
        return sdf.format(currentDate); // Return the current formatted day
    }

    private String extractHourFromTimestamp(String timestamp) {
        try {
            // Parse the timestamp to extract the hour (e.g., "8 AM")
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM dd, yyyy, h:mm a", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("h a", Locale.ENGLISH);

            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("Timestamp Parsing", "Error parsing timestamp: " + timestamp, e);
            return ""; // Return an empty string if parsing fails
        }
    }

    private void fetchDailyRevenue() {
        List<String> currentWeekDays = getCurrentWeekDays();
        List<BarEntry> revenueEntries = new ArrayList<>();

        // Retrieve the last saved week from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastSavedWeek = prefs.getString(LAST_WEEK_KEY, "");

        // Get the current week
        SimpleDateFormat sdf = new SimpleDateFormat("w", Locale.ENGLISH); // Week number format
        String currentWeek = sdf.format(new Date());

        if (!currentWeek.equals(lastSavedWeek)) {
            // Reset daily data if it's a new week
            resetDailyData();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LAST_WEEK_KEY, currentWeek);
            editor.apply();
        }

        // Real-time listener for orders
        ordersRef.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("AnalyticsFragment", "Error listening for updates: ", error);
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                // Clear previous entries
                revenueEntries.clear();

                // Loop through the days of the week and calculate revenue
                for (int i = 0; i < currentWeekDays.size(); i++) {
                    String day = currentWeekDays.get(i);
                    double dailyRevenue = 0.0;

                    // Loop through documents to sum the revenue for the current day
                    for (DocumentSnapshot document : documents) {
                        if (document.exists()) {
                            String timestamp = document.getString("timestamp");
                            String totalPrice = document.getString("totalPrice");

                            if (timestamp != null && totalPrice != null) {
                                String orderDateString = extractDateFromTimestamp(timestamp);

                                if (orderDateString.equals(day)) {
                                    try {
                                        double totalPriceDouble = Double.parseDouble(totalPrice.replace("₱", "").trim());
                                        dailyRevenue += totalPriceDouble;
                                    } catch (NumberFormatException e) {
                                        Log.e("AnalyticsFragment", "Invalid totalPrice format: " + totalPrice, e);
                                    }
                                }
                            }
                        }
                    }

                    // Add the daily revenue to the entries
                    revenueEntries.add(new BarEntry(i, (float) dailyRevenue));
                }

                // Update the bar chart with the latest data
                updateBarChart(revenueEntries, currentWeekDays);
            } else {
                Log.e("AnalyticsFragment", "No documents found");
            }
        });
    }

    private List<String> getCurrentWeekDays() {
        List<String> weekDays = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH); // "MMM dd" format (e.g., Dec 08)
        Calendar calendar = Calendar.getInstance();

        // Set calendar to the start of the week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        for (int i = 0; i < 7; i++) {
            weekDays.add(sdf.format(calendar.getTime())); // Add each day of the week in "MMM dd" format
            calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to the next day
        }

        return weekDays;
    }

    private void updateBarChart(List<BarEntry> entries, List<String> currentWeekDays) {
        // Ensure entries are sorted by X-axis values
        entries.sort((e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        BarDataSet dataSet = new BarDataSet(entries, "Daily Revenue");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.RED);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // Set bar width

        barChart.setData(barData);
        barChart.setFitBars(true);

        // Disable the legend
        barChart.getLegend().setEnabled(false);

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < currentWeekDays.size()) {
                    return currentWeekDays.get(index);
                }
                return "";
            }
        });

        // Customize chart appearance
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.invalidate();
    }

    private String extractDateFromTimestamp(String timestamp) {
        try {
            // Use SimpleDateFormat to parse the timestamp and format it into the desired "MMM dd" format (no year)
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM dd, yyyy, h:mm a", Locale.ENGLISH); // Timestamp format (e.g., "December 08, 2024, 10:30 AM")
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.ENGLISH); // Output format without year (e.g., "Dec 08")

            // Parse the timestamp string and reformat it to match the desired format
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date); // Return the formatted date as "MMM dd"
        } catch (ParseException e) {
            Log.e("Timestamp Parsing", "Error parsing timestamp: " + timestamp, e);
            return ""; // Return an empty string if parsing fails
        }
    }

    private void resetDailyData() {
        // Reset data for daily chart
        barChart.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
