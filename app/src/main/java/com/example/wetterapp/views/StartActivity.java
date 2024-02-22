package com.example.wetterapp.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.wetterapp.R;
import com.example.wetterapp.data.remote.ApiResponseCallback;
import com.example.wetterapp.data.remote.WeatherAPI;
import com.example.wetterapp.databinding.StartViewBinding;
import com.example.wetterapp.models.LocationDTO;
import com.example.wetterapp.viewmodel.LocationViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * StartActivity is the first activity to be displayed when launching the app. Displays a starting
 * screen & asks permission for current location. Navigates to next Activity based on if permission
 * is given or not
 **/
public class StartActivity extends AppCompatActivity {

    public WeatherAPI weatherAPIGSON;
    private ProgressBar spinner;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CancellationTokenSource cancellationTokenSource;
    private ExecutorService executor;
    private LocationViewModel locationViewModel;




    /**
     * Initialising of UI-Elements and required instances of API, LocationRepository, LocationProvider, ...
     * Checks if accessing current location is permitted and asks for permission if not
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartViewBinding startViewBinding = StartViewBinding.inflate(getLayoutInflater());
        setContentView(startViewBinding.getRoot());

        spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        weatherAPIGSON = new WeatherAPI();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        cancellationTokenSource = new CancellationTokenSource();
        executor = Executors.newSingleThreadExecutor();

        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

        if(hasLocationPermission()){
            getCurrentLocation();
        } else {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    private boolean hasLocationPermission(){
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }



    /**
     * This method is called after the user reacts to the location permission request
     * If permission is given this method calls getCurrentLocation
     * If permission is denied this method navigates to ListActivity
     *
     * @param requestCode  Code of permission request. (hardcoded since we are only asking one permission)
     * @param permissions  the requested permissions (access to current location)
     * @param grantResults the result of permission request
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) || (requestCode == 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
            getCurrentLocation();
        } else {
            spinner.setVisibility(View.VISIBLE);
            startActivity(new Intent(this, ListActivity.class));
        }
    }



    /**
     * Gets the current location coordinates via Google Play Services Location
     * MissingPermission is surpressed, since this method will only be called if permission was given
     * */

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        spinner.setVisibility(View.VISIBLE);
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    onLocationReceived(location);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No current location could be found", Toast.LENGTH_SHORT).show());
                }
            });
    }



    /**
     * this method is called after the current location coordinates were succesfully received
     * Navigate sto the detailview of the given current location
     *
     * @param location Der aktuelle Standort.
     */

    private void onLocationReceived(Location location) {
        weatherAPIGSON.getCurrentLocationDTO(location.getLatitude(), location.getLongitude(), new ApiResponseCallback<LocationDTO>() {
            @Override
            public void onSuccess(LocationDTO result) {
                showDetailView(result);
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(StartActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void showDetailView(LocationDTO location) {
        handleShowDetailView(location);
    }



    /**
     * handles displaying the detailview of the current location
     *
     * @param location the current location of the user for which the detailview should be opened
     */

    private void handleShowDetailView(LocationDTO location) {

        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Intent showPlaceIntent = new Intent(this, WeatherDetailActivity.class);
            location.setIsCurrentLocation(true);
            showPlaceIntent.putExtra(WeatherDetailActivity.DETAIL_VIEW_KEY, location);
            startActivity(showPlaceIntent);
        });
    }



    /**
     * Closes opened ressources once activity gets destroyed
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null && cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
            fusedLocationProviderClient = null;
            cancellationTokenSource = null;
        }

        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

}
