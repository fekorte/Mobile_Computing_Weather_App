package com.example.wetterapp.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.wetterapp.models.LocationDTO;

import java.util.List;
@Dao
public interface LocationsDAO {

    @Query("SELECT * FROM locations")
    List<LocationDTO> getAllLocations();
    @Insert
    void insertLocation(LocationDTO location);
    @Delete
    void deleteLocation(LocationDTO location);
}
