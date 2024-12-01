package com.ignacio.partykneadsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

public class SplashFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        // Find the VideoView
        VideoView splashVideoView = view.findViewById(R.id.splashVideoView);

        // Set the video path
        Uri videoUri = Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + R.raw.splash_new);
        splashVideoView.setVideoURI(videoUri);

        // Adjust the size of the VideoView when the video is prepared
        splashVideoView.setOnPreparedListener(mediaPlayer -> {
            int videoWidth = mediaPlayer.getVideoWidth();
            int videoHeight = mediaPlayer.getVideoHeight();

            // Get the height of the parent
            int parentHeight = container.getHeight();

            // Calculate the width based on the video's aspect ratio and parent's height
            int adjustedWidth = (int) (((float) videoWidth / videoHeight) * parentHeight);

            // Set the VideoView's layout parameters
            ViewGroup.LayoutParams layoutParams = splashVideoView.getLayoutParams();
            layoutParams.width = adjustedWidth; // Dynamically calculated width
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT; // Match parent's height

            splashVideoView.setLayoutParams(layoutParams);

            // Enable video looping
            mediaPlayer.setLooping(true);
        });

        // Start the video
        splashVideoView.start();

        // Navigate to the next fragment after the desired condition
        // Replace the VideoView completion listener with a trigger to proceed
        splashVideoView.setOnCompletionListener(mp -> {
            // This will trigger only if the video is allowed to stop (looping will prevent this)
            navigateToNextFragment();
        });

        // Use a timer or external condition to proceed
        view.postDelayed(this::navigateToNextFragment, 3000); // Adjust time as needed

        return view;
    }

    private void navigateToNextFragment() {
        if (onBoardingFinished()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_splashFragment_to_loginFragment);
        } else {
            NavHostFragment.findNavController(this).navigate(R.id.action_splashFragment_to_viewPagerFragment);
        }
    }

    private boolean onBoardingFinished() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("Finished", false);
    }
}
