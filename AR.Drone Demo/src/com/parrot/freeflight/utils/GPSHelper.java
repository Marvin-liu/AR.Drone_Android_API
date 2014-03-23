package com.parrot.freeflight.utils;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import java.util.List;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public final class GPSHelper {
    private LocationManager locationManager = null;
    private static volatile GPSHelper instance;

    private GPSHelper(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static GPSHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (GPSHelper.class) {
                if (instance == null) {
                    return new GPSHelper(context);
                }
            }

        }

        return instance;
    }

    public static boolean deviceSupportGPS(Context context) {
        boolean result = false;

        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        List<String> allProviders = manager.getAllProviders();

        if (allProviders.contains(LocationManager.GPS_PROVIDER)) {
            result = true;
        }

        return result;
    }

    public static boolean isGpsOn(Context context) {
        boolean result = false;

        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                result = true;
            }
        } catch (IllegalArgumentException e) {
            Log.d("GPS Helper", "Looks like we do not have gps on the device");
        }

        return result;
    }

    public void startListening(LocationListener theListener) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, theListener);
        }
    }

    public void stopListening(LocationListener theListener) {
        locationManager.removeUpdates(theListener);

    }
}
