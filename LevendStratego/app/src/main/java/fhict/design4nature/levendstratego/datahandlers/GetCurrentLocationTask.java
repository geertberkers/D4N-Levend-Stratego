package fhict.design4nature.levendstratego.datahandlers;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;

/**
 * Created by fhict.
 */
public class GetCurrentLocationTask extends AsyncTask<LocationManager, Void, Location> implements LocationListener {

    private Location location;

    @Override
    protected Location doInBackground(LocationManager... locationManagers) {
        LocationManager locationManager = locationManagers[0];
        Looper.prepare();
        // Request GPS updates. The third param is the looper to use, which defaults the the one for the current thread.
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

        // Start waiting...
        // When finished, this.location is the location
        Looper.loop();
        return this.location;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Store the location, then get the current thread's looper.
        this.location = location;
        Looper myLooper = Looper.myLooper();
        if (myLooper != null) {
            myLooper.quit();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
