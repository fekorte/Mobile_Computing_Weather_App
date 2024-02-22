package com.example.wetterapp.models;

import com.example.wetterapp.WeatherApp;
import com.example.wetterapp.data.LocationsDAO;
import com.example.wetterapp.data.WeatherAppDatabase;

import java.util.List;
/**

 The LocationsRepository class is responsible for managing the data operations for locations.
 The repository provides methods to interact with the LocationsDAO, which is responsible for accessing the data from the database.
 The repository handles operations such as getting all locations, inserting a new location, deleting a location, updating the current location status,
 removing the current location status, and checking if a location is already in the list or if there is a current location.
 */
public class LocationsRepository {

    private static LocationsRepository locationsRepository;
    private LocationsDAO locationDAO;

    public static LocationsRepository getInstance() {
        if (locationsRepository == null) {
            locationsRepository = new LocationsRepository();
        }
        return locationsRepository;
    }

    private LocationsRepository() {
        WeatherAppDatabase db = WeatherApp.database;
        locationDAO = db.getLocationsDao();
    }
    public List<LocationDTO> getLocations() {
        return locationDAO.getAllLocations();
    }
    public void insertLocation(LocationDTO location) {
        locationDAO.insertLocation(location);
    }
    public void deleteLocation(LocationDTO location) { locationDAO.deleteLocation(location); }

    /**
     * Remove the current location status from the database.
     * If there is no current location, nothing happens.
     */
    public void removeCurrentLocation(){
        LocationDTO location = getCurrentLocation();
        if(location == null) return;
        deleteLocation(location);
    }

    /**
     * Check if a location already exists in the location list.
     * The comparison is based on the equality of longitude and latitude values in the LocationDTO.
     * @param location The LocationDTO object to check.
     * @return The matching LocationDTO if found, or null if not found.
     */
    public LocationDTO getLocation(LocationDTO location) {
        List<LocationDTO> locations = getLocations();
        return locations != null ? locations.stream().filter(loc -> loc.equals(location)).findFirst().orElse(null) : null;
    }

    /**
     * Check if there is a LocationDTO with isCurrentLocation set to true in the location list.
     * @return The LocationDTO object if found, or null if not found.
     */
    public LocationDTO getCurrentLocation() {
        List<LocationDTO> locations = getLocations();
        if (locations != null) {
            return locations.stream()
                    .filter(LocationDTO::getIsCurrentLocation)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}

