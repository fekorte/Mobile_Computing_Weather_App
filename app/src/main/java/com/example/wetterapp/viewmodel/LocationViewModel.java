package com.example.wetterapp.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wetterapp.models.LocationDTO;
import com.example.wetterapp.models.LocationsRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The LocationViewModel class is responsible for managing the location data and operations.
 * It interacts with the LocationsRepository to perform CRUD operations and provide the location data to the UI.
 *
 * It exposes LiveData<List<LocationDTO>> to observe the location data changes in the UI.
 */

public class LocationViewModel extends ViewModel {
    private MutableLiveData<List<LocationDTO>> locationsLiveData;
    private LocationsRepository repository;

    public LocationViewModel() {
        repository = LocationsRepository.getInstance();
        locationsLiveData = new MutableLiveData<>();
        loadLocations();
    }

    public LiveData<List<LocationDTO>> getLocations(){
        return locationsLiveData;
    }

    /**
     * Retrieves the locations from the repository, sorts them alphabetically, and places the currentLocation at the top of the list.
     * Updates the locationsLiveData to propagate the changes.
     */
    public void loadLocations(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<LocationDTO> locations = repository.getLocations();

            //Code to filter the location list with the current location on top and alphabetically
            //Find current location if available
            Optional<LocationDTO> currentLocation = locations.stream()
                    .filter(LocationDTO::getIsCurrentLocation)
                    .findFirst();
            //Remove in order to sort the rest alphabetically
            currentLocation.ifPresent(locations::remove);
            locations.sort(Comparator.comparing(LocationDTO::getName, String.CASE_INSENSITIVE_ORDER));
            //Add the current location on top of the list
            currentLocation.ifPresent(location -> locations.add(0, location));

            locationsLiveData.postValue(locations);
        });
    }

    /**
     * Retrieves a specific location from the repository based on the provided LocationDTO object.
     *
     * @param location The LocationDTO object representing the location to retrieve.
     * @return The LocationDTO object if found, or null if not found.
     */
    public LocationDTO getLocation(LocationDTO location) {
        return repository.getLocation(location);
    }

    /**
     * Retrieves the current location from the repository
     *
     * @return The LocationDTO object if found, or null if not found.
     */
    private LocationDTO getCurrentLocation() {
        return repository.getCurrentLocation();
    }

    /**
     * Deletes a location from the repository if it exists and refreshes the location data.
     *
     * @param location The LocationDTO object representing the location to delete.
     */
    public void deleteLocation(LocationDTO location){
        if(getLocation(location) != null){
            repository.deleteLocation(location);
            loadLocations();
        }
    }


    /**
     * Inserts a new location into the repository.
     *
     * @param location The LocationDTO object representing the location to insert.
     */

    public void insertLocation(LocationDTO location) {
        repository.insertLocation(location);
    }

    /**
     * Removes the previous current location.
     * Inserts the current location into the repository and marks it as the current location.
     *
     * @param currentLocation The LocationDTO object representing the current location to insert or update.
     */
    public void insertCurrentLocation(LocationDTO currentLocation){
        //Check if there was a previous current location
        removeCurrentLocation();
        currentLocation.setIsCurrentLocation(true);
        repository.insertLocation(currentLocation);
    }

    /**
     * Removes the current location designation from any location entry in the repository.
     */
    public void removeCurrentLocation(){
        repository.removeCurrentLocation();
    }
}
