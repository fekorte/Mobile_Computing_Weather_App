package com.example.wetterapp.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CurrentWeatherDTO {

    private Weather[] weather;

    private Main main;

    private Wind wind;
    
    private Clouds clouds;

    @SerializedName("sys")
    private SunsetSunrise sunsetSunrise;

    @SerializedName("dt")
    private long timeOfDataCalculation;

    private int timezone;

    public CurrentWeatherDTO() {
    }

    public Weather[] getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public SunsetSunrise getSunsetSunrise() {
        return sunsetSunrise;
    }

    public long getTimeOfDataCalculation() {
        return timeOfDataCalculation;
    }

    public String getFormattedLocalTimeOfDataCalculation(){
        LocalDateTime toDC = LocalDateTime.ofEpochSecond(timeOfDataCalculation + timezone, 0, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm");
        return toDC.format(formatter);
    }

    public int getTimezone() {
        return timezone;
    }

    public static class Weather {
        private String main;
        private String description;
        private String icon;


        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static class Main {
        private double temp;
        private double feels_like;
        private double temp_min;
        private double temp_max;

        // humidity is a percentage value
        private int humidity;


        public int getTemp() {
            return (int)temp;
        }

        public double getFeelsLike() {
            return feels_like;
        }

        public int getTempMin() {
            return (int)temp_min;
        }

        public int getTempMax() {
            return (int)temp_max;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Wind {
        private double speed;


        public double getSpeedInKmh() {
            // OpenWeatherMap gibt in m/sek an, Umrechnung in km/h
            double speedkmh = speed * 3.6;
            return Math.round(speedkmh * 100) / 100.0;
        }
    }

    public static class Clouds {
        private int all;


        public int getAll() {
            return all;
        }
    }

    public static class SunsetSunrise {
        private long sunrise;
        private long sunset;


        public long getSunrise() {
            return sunrise;
        }

        public long getSunset() {
            return sunset;
        }

        // OpenWeatherMap gibt Zeiten als UNIX-Timestamp an, Umrechnung in LocalDateTime
        public String getFormattedLocalSunrise(int timezone){
            LocalDateTime sunriseLDT = LocalDateTime.ofEpochSecond(sunrise + timezone, 0, ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return sunriseLDT.format(formatter);
        }

        public String getFormattedLocalSunset(int timezone){
            LocalDateTime sunsetLDT = LocalDateTime.ofEpochSecond(sunset + timezone, 0, ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return sunsetLDT.format(formatter);
        }
    }

}

