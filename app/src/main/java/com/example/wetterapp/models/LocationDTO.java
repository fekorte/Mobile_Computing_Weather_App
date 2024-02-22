package com.example.wetterapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Entity(tableName = "locations", primaryKeys = {"longitude", "latitude","isCurrentLocation"})
public class LocationDTO implements Parcelable {

    private String name;
    @SerializedName("country")
    private String countryCode;
    private String state;
    @SerializedName("lon")
    private double longitude;
    @SerializedName("lat")
    private double latitude;
    private boolean isCurrentLocation;

    public LocationDTO() {
        isCurrentLocation = false;
    }
    protected LocationDTO(Parcel in) {
        name = in.readString();
        countryCode = in.readString();
        state = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        isCurrentLocation = in.readBoolean();
    }

    public static final Creator<LocationDTO> CREATOR = new Creator<LocationDTO>() {
        @Override
        public LocationDTO createFromParcel(Parcel in) {
            return new LocationDTO(in);
        }

        @Override
        public LocationDTO[] newArray(int size) {
            return new LocationDTO[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getState(){
        return state;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public boolean getIsCurrentLocation(){ return isCurrentLocation; }

    public void setName(String name) { this.name = name; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public void setState(String state){ this.state = state; }
    public void setLatitude(double latitude) {this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setIsCurrentLocation(boolean isCurrentLocation){ this.isCurrentLocation = isCurrentLocation; }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(countryCode);
        dest.writeString(state);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeBoolean(isCurrentLocation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        LocationDTO other = (LocationDTO) obj;
        return (Double.compare(other.longitude, longitude) == 0 &&
                Double.compare(other.latitude, latitude) == 0 &&
                other.isCurrentLocation == isCurrentLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, isCurrentLocation);
    }
}
