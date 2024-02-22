package com.example.wetterapp.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wetterapp.models.CurrentWeatherDTO;
import com.example.wetterapp.models.ForecastWeatherDTO;
import com.example.wetterapp.models.LocationDTO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WeatherAPI {
    private static final String API_BASE_URL = "https://api.openweathermap.org/";
    private static final String API_KEY = "a2280d0c667d3410b4fee82f29c8baee";

    private final OkHttpClient client;
    private Gson gson;

    public WeatherAPI(){
        client = new OkHttpClient();
        gson = new Gson();
    }



    /************************************+********************************************************
    ******************************** CURRENT WEATHER *********************************************
     *
     * @param latitude  latitude of selected Location
     * @param longitude longitude of selected Location
     * @param callback  called once response by OpenWeatherMap is received
     *
     * called to display current weather information in detail view
    **********************************************************************************************/

    public void getCurrentWeather(double latitude, double longitude, final ApiResponseCallback<CurrentWeatherDTO> callback){
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(API_BASE_URL + "data/2.5/weather"))
                .newBuilder()
                .addQueryParameter("lat", String.valueOf(latitude))
                .addQueryParameter("lon", String.valueOf(longitude))
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("units", "metric")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String json = responseBody.string();
                        CurrentWeatherDTO currentWeather = parseWeatherFromJson(json);
                        callback.onSuccess(currentWeather);
                    } else {
                        callback.onError("Response body is null");
                    }
                } else {
                    callback.onError("Request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Request failed: " + e.getMessage());
            }
        });
    }

    public CurrentWeatherDTO parseWeatherFromJson(String json) {
            return gson.fromJson(json, CurrentWeatherDTO.class);
    }




    /************************************+********************************************************
     ********************************* LOCATIONS-LIST ********************************************
     *
     * @param searchInput location name inputted by user
     * @param callback  called once response by OpenWeatherMap is received
     *
     * used in ListView to display search results
     *********************************************************************************************/

    public void getLocationList(String searchInput, final ApiResponseCallback<List<LocationDTO>> callback){
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(API_BASE_URL + "geo/1.0/direct"))
                .newBuilder()
                .addQueryParameter("q", searchInput)
                .addQueryParameter("limit", "5")
                .addQueryParameter("appid", API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage = "Request failed: " + e.getMessage();
                Log.e("WeatherAPI", errorMessage);
                callback.onError("Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String json = responseBody.string();
                        List<LocationDTO> locations = parseLocationListFromJson(json);
                        callback.onSuccess(locations);
                    } else {
                        callback.onError("Response body is null");
                    }
                } else {
                    callback.onError("Request failed: " + response.code());
                }
            }
        });
    }

    public List<LocationDTO> parseLocationListFromJson(String json) {
        Type locationListType = new TypeToken<List<LocationDTO>>() {}.getType();

        return gson.fromJson(json, locationListType);
    }




    /************************************+********************************************************
     ************************************ FORECAST-LIST ******************************************
     *************************** next 48-hours in 3 hour intervals *******************************
     *
     * @param latitude  latitude of selected Location
     * @param longitude longitude of selected Location
     * @param callback  called once response by OpenWeatherMap is received
     *
     * wird für Darstellung des Forecasts in DetailView genutzt
     ************************************+********************************************************/

    public void getWeatherForecast(double latitude, double longitude, final ApiResponseCallback<List<ForecastWeatherDTO>> callback){
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(API_BASE_URL + "data/2.5/forecast"))
                .newBuilder()
                .addQueryParameter("lat", String.valueOf(latitude))
                .addQueryParameter("lon", String.valueOf(longitude))
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("units", "metric")
                .addQueryParameter("cnt", "16")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage = "Request failed: " + e.getMessage();
                Log.e("WeatherAPI", errorMessage);
                callback.onError("Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String json = responseBody.string();
                        List<ForecastWeatherDTO> forecast = parseForecastFromJSON(json);
                        callback.onSuccess(forecast);
                    } else {
                        callback.onError("Response body is null");
                    }
                } else {
                    callback.onError("Request failed: " + response.code());
                }
            }
        });
    }

    public List<ForecastWeatherDTO> parseForecastFromJSON(String json){
        // Zugriff auf Liste an Forecast-Elementen in der Response
        JsonElement jsonElement = JsonParser.parseString(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray listArray = jsonObject.getAsJsonArray("list");
        Type weatherListType = new TypeToken<List<ForecastWeatherDTO>>() {}.getType();
        return gson.fromJson(listArray, weatherListType);
    }




    /************************************+*******************************************************
     ********************************** Current Location ****************************************
     ********************* based on current coordinates of user  ********************************
     *
     * @param latitude  latitude provided by Google Play Location Services
     * @param longitude longitude provided by Google Play Location Services
     * @param callback  called once response by OpenWeatherMap is received
     *
     * used to get a LocationDTO after getting coordinates through Access Location
     ********************************************************************************************/

    public void getCurrentLocationDTO(double latitude, double longitude, final ApiResponseCallback<LocationDTO> callback){
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(API_BASE_URL + "geo/1.0/reverse"))
                .newBuilder()
                .addQueryParameter("lat", String.valueOf(latitude))
                .addQueryParameter("lon", String.valueOf(longitude))
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("limit", "1")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage = "Request failed: " + e.getMessage();
                Log.e("WeatherAPI", errorMessage);
                callback.onError("Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String json = responseBody.string();
                        List<LocationDTO> locations = parseLocationListFromJson(json);
                        LocationDTO currentLocation = locations.get(0);
                        callback.onSuccess(currentLocation);
                    } else {
                        callback.onError("Response body is null");
                    }
                } else {
                    callback.onError("Request failed: " + response.code());
                }
            }
        });
    }


}
