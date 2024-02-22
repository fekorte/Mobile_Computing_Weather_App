package com.example.wetterapp;

import android.app.Application;

import androidx.room.Room;

import com.example.wetterapp.data.WeatherAppDatabase;

/**
 * This class represents the application class for the WeatherApp.
 * It extends the Application class and is responsible for initializing the application and setting up the database.
 */

public class WeatherApp extends Application {
    public static WeatherAppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        database = Room.databaseBuilder(
                        getApplicationContext(),
                        WeatherAppDatabase.class,
                        "weatherApp.db")
                .fallbackToDestructiveMigration()
                .build();
    }
}

