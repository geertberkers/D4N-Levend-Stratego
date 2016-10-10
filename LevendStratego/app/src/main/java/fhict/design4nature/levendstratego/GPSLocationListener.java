package fhict.design4nature.levendstratego;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by fhict.
 */
public class GPSLocationListener implements LocationListener {

    private boolean flagDropped;

    private Location flagLocation;

    private TextView gpsInfo;
    private TextView flagInfo;

    public GPSLocationListener(TextView flagInfo, TextView gpsInfo){
        this.gpsInfo = gpsInfo;
        this.flagInfo = flagInfo;
    }


    public void newGame(){
        flagDropped = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        String longitude = "Longitude: " + location.getLongitude();
        String latitude = "Latitude: " + location.getLatitude();

        if(!flagDropped){
            flagLocation = location;
            MainActivity.addFlagMarker(location);
            flagInfo.setText("Flag info:\n" + longitude + "\n" + latitude + "\n");
            flagDropped = true;
            return;
        }

        //TODO: Handle result of timer, give tips for hidden flag. (sound/vibrate)
        float distance = calculateDistanceToFlag(location);

        System.out.println(longitude);
        System.out.println(latitude);

        gpsInfo.setText("GPS info:\n"+ longitude + "\n" + latitude + "\nDistance to flag: " + distance);
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

    private float calculateDistanceToFlag(Location currentLocation){
        float[] results = new float[1];
        Location.distanceBetween(
                flagLocation.getLatitude(),
                flagLocation.getLongitude(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                results);

        return results[0];
    }
}
