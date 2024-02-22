package com.example.wetterapp.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ForecastWeatherDTO {

    private Main main;

    private List<Weather> weather;
    @SerializedName("dt_txt")
    private String timeForForcast;

    public ForecastWeatherDTO() {
    }

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public static class Main {
        private double temp;

        public int getTemp() {
            return (int)temp;
        }
    }

    public static class Weather {
        private String icon;

        public String getIcon() {
            return icon;
        }
    }

    public String getTimeForForcast() {
        return timeForForcast;
    }

    public String getWeekDayAndTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(timeForForcast, formatter);
        int hour = dateTime.getHour();
        String weekday = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String dayOfMonth = String.format(Locale.getDefault(), "%02d", dateTime.getDayOfMonth());
        return weekday + ", " + dayOfMonth + "." + dateTime.getMonthValue() + ". - " + hour + ":00";
    }
}
