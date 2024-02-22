package com.example.wetterapp.data;

import android.location.Location;
import android.location.LocationListener;

public class CurrentLocationListener implements LocationListener {
    private LocationUpdateCallback callback;

    public interface LocationUpdateCallback {
        void onLocationChanged(double latitude, double longitude);
    }

    public CurrentLocationListener(LocationUpdateCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onLocationChanged(Location location) {
        callback.onLocationChanged(location.getLatitude(), location.getLongitude());
    }
}
