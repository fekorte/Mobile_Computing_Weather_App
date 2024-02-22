package com.example.wetterapp.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wetterapp.R;
import com.example.wetterapp.data.CurrentLocationListener;
import com.example.wetterapp.data.remote.ApiResponseCallback;
import com.example.wetterapp.data.remote.WeatherAPI;
import com.example.wetterapp.databinding.ListViewBinding;
import com.example.wetterapp.models.LocationDTO;
import com.example.wetterapp.viewmodel.LocationViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * The ListActivity class is responsible for displaying a list of locations and handling user interactions.
 * It extends the AppCompatActivity class and implements the LocationListAdapter.LocationListCallbacks and
 * CurrentLocationListener.LocationUpdateCallback interfaces.
 *
 * This activity provides the following features:
 * - Swipe-to-delete: Allows the user to swipe an item in the location list to delete it.
 * - Location search: Enables the user to search for new places and displays the search results in the list.
 * - Detailed view: Shows the detailed view of each location when the user clicks on an item.
 * - Current location updates: Provides a floating action button to activate and deactivate location updates
 *   from the current position. When enabled, the app periodically retrieves the user's current location
 *   and updates the location list accordingly.
 *
 * The ListActivity uses the LocationViewModel to manage the location data and observe changes in the
 * location list. It communicates with the LocationsRepository to retrieve, insert, and delete locations.
 * The WeatherAPI is used for location search and retrieving current location details.
 */

public class ListActivity extends AppCompatActivity implements LocationListAdapter.LocationListCallbacks, CurrentLocationListener.LocationUpdateCallback {
    private LocationViewModel locationViewModel;
    private ListViewBinding listViewBinding;
    private LocationListAdapter listAdapter;
    private Toolbar toolbar;
    private LocationManager locationManager;
    private CurrentLocationListener locationListener;
    private boolean hasLocationUpdates;
    private ExecutorService executor;
    private Handler handler;
    private boolean isListening;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listViewBinding = ListViewBinding.inflate(getLayoutInflater());
        setContentView(listViewBinding.getRoot());

        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        isListening = false;

        if(getIntent().hasExtra("activateLocationUpdates")){
            hasLocationUpdates = getIntent().getBooleanExtra("activateLocationUpdates", false);
        }

        setUpRecyclerViewAndAdapter();
        setUpToolBar();
        setUpSearchButton();
        setUpCurrentLocationButton();
    }

    private void setUpRecyclerViewAndAdapter() {
        RecyclerView listView = listViewBinding.locationList;
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        listAdapter = new LocationListAdapter(this);
        listView.setAdapter(listAdapter);
    }

    private void setUpToolBar() {
        toolbar = listViewBinding.goBack;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSearchButton() {
        Button searchButton = listViewBinding.searchLocationButton;
        EditText searchField = listViewBinding.enterLocationName;
        Context context = searchButton.getContext();

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable or disable the search button based on the text length
                searchButton.setEnabled(s.length() > 0);

                // Change the background color of the search button based on its enabled state
                int enabledButtonColor = ContextCompat.getColor(context, R.color.enabledButtonColor);
                int disabledButtonColor = ContextCompat.getColor(context, R.color.disabledButtonColor);
                searchButton.setBackgroundColor(s.length() > 0 ? enabledButtonColor : disabledButtonColor);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setUpCurrentLocationButton(){
        FloatingActionButton fabGetCurrentLocation = listViewBinding.fabGetCurrentLocation;

        //Check if user gave the permission for the current location, if no then hide floating action button and return
        boolean locationPermission = hasLocationPermission();
        fabGetCurrentLocation.setVisibility(locationPermission ? View.VISIBLE : View.GONE);
        if(!locationPermission) return;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new CurrentLocationListener(this);

        //Set up start setting for location updates. If the user selected the current location shown at the beginning as a favorite
        //the location updates are automatically on, otherwise they are off
        if (hasLocationUpdates) {
            setUpHasLocationUpdates();
        } else {
            setUpNoLocationUpdates();
        }

        //Click listener to start or stop current location updates and change the icon
        fabGetCurrentLocation.setOnClickListener(v -> {
            if(hasLocationUpdates){
                // Remove the current location designation and stop location updates
                executor.execute(() ->{
                    locationViewModel.removeCurrentLocation();
                    locationViewModel.loadLocations();
                    handler.post(this::setUpNoLocationUpdates);
                });
            } else {
                // Start location updates and mark the current location
                setUpHasLocationUpdates();
            }
        });
    }

    private void setUpHasLocationUpdates() {
        FloatingActionButton fabGetCurrentLocation = listViewBinding.fabGetCurrentLocation;
        if (!isListening) {
            startLocationUpdates();
            isListening = true;
        }
        hasLocationUpdates = true;
        fabGetCurrentLocation.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.remove));
    }

    private void setUpNoLocationUpdates() {
        FloatingActionButton fabGetCurrentLocation = listViewBinding.fabGetCurrentLocation;
        if (isListening) {
            stopLocationUpdates();
            isListening = false;
        }
        hasLocationUpdates = false;
        fabGetCurrentLocation.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.target));
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpViewModel();
        addSwipeToDelete();
        if (hasLocationPermission()) {
            if (hasLocationUpdates && !isListening) {
                startLocationUpdates();
                isListening = true;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isListening) {
            stopLocationUpdates();
            isListening = false;
        }
    }
    private void setUpViewModel() {
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        locationViewModel.getLocations().observe(this, locationList -> listAdapter.setLocations(locationList));
    }

    private void addSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                executor.execute(() -> locationViewModel.deleteLocation(listAdapter.getLocationAtPosition(viewHolder.getAdapterPosition())));
            }
        }).attachToRecyclerView(listViewBinding.locationList);
    }

    @Override
    public boolean onSupportNavigateUp() {
        toolbar.setVisibility(View.GONE);
        locationViewModel.loadLocations();
        return true;
    }

    public void onSearch(View view) {
        handleSearchLocation();
    }

    private void handleSearchLocation() {
        EditText searchField = listViewBinding.enterLocationName;
        String locationName = searchField.getText().toString();
        listViewBinding.enterLocationName.setText("");

        WeatherAPI weatherAPIGSON = new WeatherAPI();
        weatherAPIGSON.getLocationList(locationName, new ApiResponseCallback<List<LocationDTO>>() {
            @Override
            public void onSuccess(List<LocationDTO> locations) {
                // Update the location list with the search results
                runOnUiThread(() -> listAdapter.setLocations(locations));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(ListActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
        toolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDetailView(LocationDTO location) {
        // Handle the location item click to show the detail view
        handleShowDetailView(location);
    }

    private void handleShowDetailView(LocationDTO location) {
        // Show the detail view for the selected location
        Intent showPlaceIntent = new Intent(this, WeatherDetailActivity.class);
        showPlaceIntent.putExtra(WeatherDetailActivity.DETAIL_VIEW_KEY, location);
        showPlaceIntent.putExtra("LocationUpdate", this.hasLocationUpdates);
        startActivity(showPlaceIntent);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationUpdates() {
        // Start receiving location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Request location updates with a desired interval and minimum distance
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2000, locationListener);
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onLocationChanged(double latitude, double longitude) {

        executor.execute(() ->{
            // Handle the location change callback from the CurrentLocationListener
            WeatherAPI weatherAPIGSON = new WeatherAPI();
            weatherAPIGSON.getCurrentLocationDTO(latitude, longitude, new ApiResponseCallback<LocationDTO>() {
                @Override
                public void onSuccess(LocationDTO result) {
                    // Update the current location in the ViewModel and reload the location list
                    locationViewModel.insertCurrentLocation(result);
                    locationViewModel.loadLocations();

                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(ListActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}