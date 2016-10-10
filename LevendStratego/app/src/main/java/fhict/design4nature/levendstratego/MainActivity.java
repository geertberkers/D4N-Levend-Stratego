package fhict.design4nature.levendstratego;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by fhict.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Static fields for GPS listener
    private final static int MIN_DISTANCE_BETWEEN_UPDATES = 0;
    private final static int MIN_TIME_INTERVAL_BETWEEN_UPDATES = 1000;

    // Static LatLng for zooming into  map
    private static final LatLng EINDHOVEN = new LatLng(51.45184971, 5.4820988);

    private Button stopGame;
    private Button gpsButton;

    private TextView gpsInfo;
    private TextView flagInfo;

    private static Marker flagMarker;
    private static GoogleMap googleMap;

    private LocationManager locationManager;
    private GPSLocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopGame = (Button) findViewById(R.id.stopGame);
        gpsButton = (Button) findViewById(R.id.gpsButton);

        gpsInfo = (TextView) findViewById(R.id.gpsInfo);
        flagInfo = (TextView) findViewById(R.id.flagInfo);

        locationListener = new GPSLocationListener(flagInfo, gpsInfo);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public static void addFlagMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(latLng).title("Vlag");
        flagMarker = googleMap.addMarker(marker);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(EINDHOVEN, 15));

        MainActivity.googleMap = googleMap;
    }

    public void hideFlag(View view) {
        if (view.getId() == gpsButton.getId()) {
            locationListener.newGame();

            stopGame.setEnabled(true);
            gpsButton.setEnabled(false);
            gpsButton.setText(getString(R.string.flag_hidden));

            //TODO: Wait for game to start
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_INTERVAL_BETWEEN_UPDATES,
                    MIN_DISTANCE_BETWEEN_UPDATES,
                    locationListener);
        }
    }

    public void stopGame(View view) {
        if (view.getId() == stopGame.getId()) {
            stopGame.setEnabled(false);
            gpsButton.setEnabled(true);

            gpsButton.setText(getString(R.string.hide_flag));
            locationManager.removeUpdates(locationListener);

            flagInfo.setText(R.string.flag_info);
            gpsInfo.setText(R.string.gps_info);

            flagMarker.remove();
        }
    }

    @Override
    protected void onStop() {
        locationManager.removeUpdates(locationListener);

        super.onStop();
    }
}