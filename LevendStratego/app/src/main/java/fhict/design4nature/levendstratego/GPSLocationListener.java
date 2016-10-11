package fhict.design4nature.levendstratego;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import fhict.design4nature.levendstratego.MainActivity;

/**
 * Created by fhict.
 */
class GPSLocationListener implements LocationListener {

    private final TextView gpsInfo;
    private Location flagLocation;

    public GPSLocationListener(TextView gpsInfo) {
        this.gpsInfo = gpsInfo;
    }

    public void newGame(Location flagLocation) {
        this.flagLocation = flagLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        float distance = calculateDistanceToFlag(location);

        String longitude = "Longitude: " + location.getLongitude();
        String latitude = "Latitude: " + location.getLatitude();

        System.out.println(longitude);
        System.out.println(latitude);
        System.out.println("Distance: " + distance + " m");

        String gpsInfoText = "GPS info:\n" + longitude + "\n" + latitude + "\nDistance to flag: " + distance;
        gpsInfo.setText(gpsInfoText);

        sentHintVibration(distance);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        System.out.println("Status changed: " + s);
    }

    @Override
    public void onProviderEnabled(String s) {
        System.out.println("Provider enabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        System.out.println("Provider disabled: " + s);
    }

    private float calculateDistanceToFlag(Location currentLocation) {
        float[] results = new float[1];
        Location.distanceBetween(
                flagLocation.getLatitude(),
                flagLocation.getLongitude(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                results);

        return results[0];
    }

    private void sentHintVibration(float distance) {
        MainActivity.sendHintVibration(distance);
    }

}
