package fhict.design4nature.levendstratego;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
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

    private static Vibrator vibrator;

    // region Android Activity Lifecycle...

    // http://www.javatpoint.com/images/androidimages/Android-Activity-Lifecycle.png

    /**
     * Called after onCreate, when app opened or when screen unlocked
     */
    @Override
    protected void onStart() {
        System.out.println("onStart");
        super.onStart();
    }

    /**
     * Called after onStart
     */
    @Override
    protected void onResume() {
        System.out.println("onResume");
        super.onResume();
    }

    /**
     * Called before onStop
     */
    @Override
    protected void onPause() {
        System.out.println("onPause");
        super.onPause();
    }

    /**
     * Called on minimize or screen locked
     */
    @Override
    protected void onStop() {
        System.out.println("onStop");
        super.onStop();
    }

    /***
     * Called when application finished!
     */
    @Override
    protected void onDestroy() {
        System.out.println("onDestroy");
        locationManager.removeUpdates(locationListener);
        vibrator.cancel();
        super.onDestroy();
    }

    /***
     * Override onBackPressed to let the user decide what happens with the application when someone tries to close it.
     */
    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Applicatie sluiten");
        dialogBuilder.setMessage("Weet u zeker dat u de app wilt sluiten?\nDit beëindigd het spel!");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setNeutralButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        dialogBuilder.setPositiveButton("Minimaliseer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
        });
        dialogBuilder.setNegativeButton("Nee", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
    // endregion

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

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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
            if (gpsButton.getText().toString().equals(getString(R.string.hide_flag))) {
                locationListener.newGame();

                gpsButton.setText(R.string.start_game);
                //TODO: Get gps once and show it. now it shows on else
            } else {
                gpsButton.setText(R.string.flag_hidden);
                stopGame.setEnabled(true);
                gpsButton.setEnabled(false);

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_INTERVAL_BETWEEN_UPDATES,
                        MIN_DISTANCE_BETWEEN_UPDATES,
                        locationListener);
            }
        }
    }

    public void stopGame(View view) {
        if (view.getId() == stopGame.getId()) {
            stopGame.setEnabled(false);
            gpsButton.setEnabled(true);

            gpsButton.setText(R.string.hide_flag);
            locationManager.removeUpdates(locationListener);

            flagInfo.setText(R.string.flag_info);
            gpsInfo.setText(R.string.gps_info);

            flagMarker.remove();
        }
    }

    public static void sendHintVibration(float distance) {
        //TODO: Find good patterns for sending hints
        if (distance < 2.5) {
            long pattern[] = {0, 1000, 200, 1000, 200};
            vibrator.vibrate(pattern, 0);
        } else if(distance < 10){
            long pattern[] = {0, 200, 200, 200, 200};
            vibrator.vibrate(pattern, 1);
        }
    }
}