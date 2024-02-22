package com.example.wetterapp.views;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;


import com.example.wetterapp.R;
import com.example.wetterapp.data.remote.ApiResponseCallback;
import com.example.wetterapp.data.remote.WeatherAPI;
import com.example.wetterapp.databinding.LocationListItemBinding;
import com.example.wetterapp.models.CurrentWeatherDTO;
import com.example.wetterapp.models.LocationDTO;

/**

 The LocationListAdapter class is responsible for binding the location data to the RecyclerView in the ListActivity.
 It extends the RecyclerView.Adapter class and provides the necessary methods to create and bind ViewHolder objects.
 The adapter also handles user interactions with the items in the list, such as clicking on an item to show the detailed view.
 The LocationListAdapter class implements the LocationListCallbacks interface, which defines a callback method to show the detailed view of a location.
 The adapter maintains a list of LocationDTO objects, which represent the locations to be displayed in the RecyclerView.
 It dynamically fetches the current weather for each location using the WeatherAPI and updates the corresponding views accordingly.

 The LocationViewHolder class is a static inner class that represents the ViewHolder for each item in the RecyclerView.
 It holds references to the views in the item layout and provides a constructor to bind the views.
 */

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {

    /**
     * The LocationListCallbacks interface provides a callback method to show the detailed view of a location.
     */
    public interface LocationListCallbacks {
        void showDetailView(LocationDTO location);
    }

    private List<LocationDTO> locationList;
    private LocationListCallbacks callbacks;
    public LocationListAdapter(LocationListCallbacks callbacks) {
        locationList = new ArrayList<>();
        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LocationListItemBinding binding = LocationListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new LocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationDTO location = locationList.get(position);
        holder.locationNameText.setText(location.getName());

        String state = (location.getState() == null) ? "" : location.getState();
        holder.countryCodeText.setText(holder.itemView.getContext().getString(R.string.list_country_description, location.getCountryCode(), state));

        // Fetch the current weather for the location and update the temperature view
        WeatherAPI weatherAPIGSON = new WeatherAPI();
        weatherAPIGSON.getCurrentWeather(location.getLatitude(), location.getLongitude(), new ApiResponseCallback<CurrentWeatherDTO>() {
            @Override
            public void onSuccess(CurrentWeatherDTO result) {
                holder.itemView.post(() -> {
                    double temperature = result.getMain().getTemp();
                    holder.degreeTextView.setText(holder.itemView.getContext().getString(R.string.list_degree, temperature));
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Adapter", "Error fetching current weather: " + errorMessage);
            }
        });
        // Show or hide the current location icon and text based on the isCurrentLocation flag
        holder.locationIcon.setVisibility(location.getIsCurrentLocation() ? View.VISIBLE : View.GONE);
        holder.locationText.setVisibility(location.getIsCurrentLocation() ? View.VISIBLE : View.GONE);

        // Set the click listener to show the detailed view of the location
        holder.itemView.setOnClickListener(v -> callbacks.showDetailView(location));
    }

    public void setLocations(List<LocationDTO> locations) {
        // Update the location list and notify the adapter to refresh the views
        locationList = locations;
        notifyDataSetChanged();
    }

    public LocationDTO getLocationAtPosition(int position){
        return locationList.get(position);
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    /**
     * The LocationViewHolder class represents the ViewHolder for each item in the RecyclerView.
     * It holds references to the views in the item layout and provides a constructor to bind the views.
     */
    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        public TextView locationNameText;
        public TextView countryCodeText;
        public TextView degreeTextView;
        public ImageView locationIcon;
        public TextView locationText;
        public LocationViewHolder(LocationListItemBinding binding) {
            super(binding.getRoot());
            locationNameText = binding.locationNameTextView;
            countryCodeText = binding.countryCodeTextView;
            degreeTextView = binding.degreeListTextView;
            locationIcon = binding.locationIcon;
            locationText = binding.currentLocationTextView;
        }
    }
}
