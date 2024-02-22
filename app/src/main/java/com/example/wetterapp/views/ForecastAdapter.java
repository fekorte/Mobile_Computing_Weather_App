package com.example.wetterapp.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetterapp.R;
import com.example.wetterapp.models.ForecastWeatherDTO;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for populating forecast data in the RecyclerView.
 * It extends the RecyclerView.Adapter class and uses the ForecastViewHolder class for the item view management
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastWeatherDTO> forecastList;
    private View forecastView;


    public ForecastAdapter() {
        forecastList = new ArrayList<>();
    }

    /**
     * This method adds a given forecast weather object to the forecastList and notifies
     * the adapter that the data set has changed to update the RecyclerView.
     */
    public void addForecast(ForecastWeatherDTO forecast) {
        forecastList.add(forecast);
        notifyDataSetChanged();
    }

    /**
     * The ForecastViewHolder class represents an item view in the RecyclerView.
     * It holds references to the views in the forecast item layout.
     */
    public static class ForecastViewHolder extends RecyclerView.ViewHolder {

        public TextView forecastDayAndDescriptionTextView;
        public TextView forecastTemperatureTextView;
        public ImageView forecastIcon;


        public ForecastViewHolder(View forecastView) {
            super(forecastView);
            forecastDayAndDescriptionTextView = forecastView.findViewById(R.id.day_description);
            forecastTemperatureTextView = forecastView.findViewById(R.id.forecast_temperature);
            forecastIcon = forecastView.findViewById(R.id.forecast_icon);
        }
    }

    /**
     * This method is called when the RecyclerView needs a new ForecastViewHolder instance.
     * It Inflates the forecast item layout and returns the ForecastViewHolder instance.
     */
    @NonNull
    @Override
    public ForecastAdapter.ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        forecastView = LayoutInflater.from(parent.getContext()).inflate(R.layout.forecast_list_item, parent, false);
        return new ForecastViewHolder(forecastView);
    }

    /**
     * This method is called to bind data to a ForecastViewHolder at the given position.
     * It retrieves the forecast weather object from the forecastList based on this position and
     * updates the views within the ForecastViewHolder with the forecast data.
     */
    @Override
    public void onBindViewHolder(@NonNull ForecastAdapter.ForecastViewHolder holder, int position) {
        ForecastWeatherDTO forecast = forecastList.get(position);
        String dayAndDescription = forecast.getWeekDayAndTime() + ": ";
        String temperature = forecast.getMain().getTemp() + " C°";
        holder.forecastDayAndDescriptionTextView.setText(dayAndDescription);
        holder.forecastTemperatureTextView.setText(temperature);
        String forecastIconId = forecast.getWeather().get(0).getIcon();
        String forecastIconUrl = "https://openweathermap.org/img/wn/" + forecastIconId + ".png";

        try {
            Picasso.with(forecastView.getContext()).load(forecastIconUrl).into(holder.forecastIcon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** This method returns the amount of forecast items in the forecastList. */
    @Override
    public int getItemCount() {
        return forecastList.size();
    }
}
