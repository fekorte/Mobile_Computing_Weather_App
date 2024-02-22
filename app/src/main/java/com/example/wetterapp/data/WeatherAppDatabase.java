package com.example.wetterapp.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.wetterapp.models.LocationDTO;


@Database(entities = {LocationDTO.class}, version = 7, exportSchema = false)
public abstract class WeatherAppDatabase extends RoomDatabase {

    public abstract LocationsDAO getLocationsDao();
}
