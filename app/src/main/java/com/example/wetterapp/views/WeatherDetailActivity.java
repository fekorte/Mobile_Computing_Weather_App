package com.example.wetterapp.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetterapp.R;
import com.example.wetterapp.data.remote.ApiResponseCallback;
import com.example.wetterapp.data.remote.WeatherAPI;
import com.example.wetterapp.models.CurrentWeatherDTO;
import com.example.wetterapp.models.ForecastWeatherDTO;
import com.example.wetterapp.models.LocationDTO;
import com.example.wetterapp.viewmodel.LocationViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The WeatherDetailActivity represents the activity for displaying detailed weather information.
 * It shows general and detailed weather info for selected locations and a 48h-forecast.
 */
public class WeatherDetailActivity extends AppCompatActivity {

    private WeatherAPI weatherAPIGSON;
    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;
    private LocationViewModel locationViewModel;
    public final static String DETAIL_VIEW_KEY = "DETAIL_VIEW_KEY";
    private CurrentWeatherDTO weather;
    private String location;
    private LocationDTO selectedLocation;
    private boolean isFavorite;
    private boolean isCurrentLocation;


    /**
     * initializes the UI elements. It retrieves the selected location from the intent
     * and sets up the forecastRecyclerView and adapter and fetches the current weather and the weather forecast
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);

        selectedLocation = getIntent().getParcelableExtra(DETAIL_VIEW_KEY);
        isCurrentLocation = selectedLocation.getIsCurrentLocation();
        location = selectedLocation.getName();

        forecastRecyclerView = findViewById(R.id.forecastList);
        forecastAdapter = new ForecastAdapter();
        forecastRecyclerView.setAdapter(forecastAdapter);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weatherAPIGSON = new WeatherAPI();

        TextView placeName = findViewById(R.id.locationName);
        placeName.setText(location);

        onMenuClick();
        getCurrentWeather();
        getWeatherForecast();

        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        setUpFavoriteStar();
    }

    /** This method sets up the "favorite"-icon based on the selected location's favorite status */
    private void setUpFavoriteStar(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if(locationViewModel.getLocation(selectedLocation) != null){
                createFilledStar();
            } else {
                createEmptyStar();
            }
        });
    }

    /** This method created the filled star icon for the favorites functionality */
    private void createFilledStar(){
        isFavorite = true;
        TextView favoriteStar = findViewById(R.id.favoriteStar);
        favoriteStar.setText("\u2605");
        favoriteStar.setTextColor(Color.YELLOW);
    }

    /** This method created the empty star icon for the favorites functionality */
    private void createEmptyStar(){
        isFavorite = false;
        TextView favoriteStar = findViewById(R.id.favoriteStar);
        favoriteStar.setText("\u2606");
        favoriteStar.setTextColor(Color.WHITE);
    }

    /** This method is called when the star icon is clicked and it delegates the functionality to the separate handling method */
    public void onFavoriteSelected(View view){
        handleFavoriteSelected();
    }

    /**
     * This method handles the logic for selecting or unselecting favorites
     * and updates the UI accordingly
     */
    private void handleFavoriteSelected(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        if(isFavorite){
            executor.execute(() -> {
                if(isCurrentLocation)
                    selectedLocation.setIsCurrentLocation(true);
                locationViewModel.deleteLocation(selectedLocation);
                handler.post(this::createEmptyStar);
            });
        } else {
            executor.execute(() -> {
                if(isCurrentLocation){
                    locationViewModel.insertCurrentLocation(selectedLocation);
                } else {
                    locationViewModel.insertLocation(selectedLocation);
                }
                handler.post(this::createFilledStar);
            });
        }
    }

    /**
     * This method uses the WeatherAPI-class to fetch current weather data for
     * the selected location and updates the weather object.
     */
    private void getCurrentWeather() {
        weatherAPIGSON.getCurrentWeather(selectedLocation.getLatitude(), selectedLocation.getLongitude(), new ApiResponseCallback<CurrentWeatherDTO>() {
                @Override
                public void onSuccess(CurrentWeatherDTO result) {
                    weather = result;
                    handleAPICallbacks();
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("WeatherDetailActivity", "Error: " + errorMessage);
                }
            });
        }

    /**
     * This method uses the WeatherAPI-class to fetch the forecast weather data for the selected location. */
        private void getWeatherForecast(){
            weatherAPIGSON.getWeatherForecast(selectedLocation.getLatitude(), selectedLocation.getLongitude(), new ApiResponseCallback<List<ForecastWeatherDTO>>() {
                public void onSuccess(List<ForecastWeatherDTO> result) {
                    for (ForecastWeatherDTO forecast : result){
                        createForecastView(forecast);
                    }
                    handleAPICallbacks();
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("WeatherDetailActivity", "Error: " + errorMessage);
                }
            });
        }

    /** This method handles the API callbacks and updates the UI when the weather data is available. */
    private void handleAPICallbacks(){
        if (weather != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayWeatherData();
                }
            });
        }
    }

    /**
     * This method displays the weather data on the UI elements and loads weather icons from the Weather API into the UI using Picasso.
     * It also sets the background image based on the weather condition depending on the icon-ID.
     */
    private void displayWeatherData(){

        String currentTempString = String.valueOf(weather.getMain().getTemp());
        String temperatureString = currentTempString + " C°";
        String descriptionString = weather.getWeather()[0].getDescription();
        String iconId = weather.getWeather()[0].getIcon();
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        if (iconId.contains("n")){
            nestedScrollView.setBackgroundResource(R.drawable.bg_night);
        } else if (iconId.contains("d")){
            String path = "bg_" + iconId;
            int resourceId = getResources().getIdentifier(path, "drawable", getPackageName());
            if (resourceId != 0){
                nestedScrollView.setBackgroundResource(resourceId);
            } else {
                nestedScrollView.setBackgroundResource(R.drawable.bg_def);
            }
        } else {
            nestedScrollView.setBackgroundResource(R.drawable.bg_def);
        }

        String iconUrl = "https://openweathermap.org/img/wn/" + iconId + ".png";
        int minTemp = weather.getMain().getTempMin();
        int maxTemp = weather.getMain().getTempMax();
        double windspeed = weather.getWind().getSpeedInKmh();
        int humidity = weather.getMain().getHumidity();
        String sunrise = weather.getSunsetSunrise().getFormattedLocalSunrise(weather.getTimezone());
        String sunset = weather.getSunsetSunrise().getFormattedLocalSunset(weather.getTimezone());

        TextView temperature = findViewById(R.id.temperature);
        temperature.setText(temperatureString);
        TextView description = findViewById(R.id.description);
        description.setText(descriptionString);
        ImageView imageView = findViewById(R.id.currentWeatherIcon);

        try {
            Picasso.with(WeatherDetailActivity.this).load(iconUrl).into(imageView);

        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView minMaxTemperature = findViewById(R.id.minMaxTemperature);
        minMaxTemperature.setText("min: " + minTemp + " C° max: " + maxTemp + " C°");

        TextView windspeedText = findViewById(R.id.windspeed);
        windspeedText.setText("windspeed: " + windspeed + " km/h");

        TextView humidityText = findViewById(R.id.humidity);
        humidityText.setText("humidity: " + humidity + " %");

        TextView sunriseText = findViewById(R.id.sunrise);
        sunriseText.setText("sunrise: " + sunrise + " am");

        TextView sunsetText = findViewById(R.id.sunset);
        sunsetText.setText("sunset: " + sunset + " pm");

    }

    /**
     * This method creates the forecast view for a given forecast weather object.
     * It adds the forecast weather object to the forecast adapter.
     * @param forecast
     */
    private void createForecastView(ForecastWeatherDTO forecast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forecastAdapter.addForecast(forecast);
            }
        });
    }

    /**
     * This method handles the functionality when the menu button is clicked.
     * It adds a ClickListener and creates an intent to navigate to the ListActivity.
     * It passes information if the location is marked as a favorite and starts the ListActivity.
     */
    private void onMenuClick(){
        ImageView menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(v -> {
                Intent menuIntent = new Intent(WeatherDetailActivity.this, ListActivity.class);
                if(this.isCurrentLocation){
                    menuIntent.putExtra("activateLocationUpdates", this.isFavorite);
                    startActivity(menuIntent);
                    return;
                }

                if(getIntent().hasExtra("LocationUpdate")){
                    boolean hasLocationUpdates = getIntent().getBooleanExtra("LocationUpdate", false);
                    menuIntent.putExtra("activateLocationUpdates", hasLocationUpdates);
                }
            startActivity(menuIntent);
        });
    }
}
