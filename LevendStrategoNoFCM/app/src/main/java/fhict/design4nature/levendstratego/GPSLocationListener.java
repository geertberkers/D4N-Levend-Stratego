package fhict.design4nature.levendstratego;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by fhict.
 */
public class GPSLocationListener implements LocationListener {

    private boolean flagDropped;
    private boolean gameStarted;
    private boolean flagLost;

    private Location flagLocation;

    private TextView gpsInfo;
    private TextView flagInfo;


    public GPSLocationListener(TextView flagInfo, TextView gpsInfo) {
        this.gpsInfo = gpsInfo;
        this.flagInfo = flagInfo;
    }


    public void newGame() {
        flagDropped = false;
        gameStarted = false;
        flagLost = false;
    }
    public void startGame(){
        gameStarted = true;
    }
    @Override
    public void onLocationChanged(Location location) {
        String longitude = "Longitude: " + location.getLongitude();
        String latitude = "Latitude: " + location.getLatitude();

        if (!flagDropped) {
            flagLocation = location;
            MainActivity.addFlagMarker(location);
            flagInfo.setText("Flag info:\n" + longitude + "\n" + latitude + "\n");
            flagDropped = true;
            return;
        }

        if(gameStarted) {
            float distance = calculateDistanceToFlag(location);

            System.out.println(longitude);
            System.out.println(latitude);

            gpsInfo.setText("GPS info:\n" + longitude + "\n" + latitude + "\nDistance to flag: " + distance);
            sentHintVibration(distance);
        }
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
