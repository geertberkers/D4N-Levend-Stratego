package fhict.design4nature.levendstratego;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutionException;

import fhict.design4nature.levendstratego.datahandlers.GetCurrentLocationTask;
import fhict.design4nature.levendstratego.datahandlers.SendNotificationTask;

/**
 * Created by fhict.
 */
public class MainActivitySelfTest extends AppCompatActivity implements OnMapReadyCallback {

    // Static fields FCM topic
    // Change this if multiple users are testing application.
    private static final String TOPIC = "game";

    // Static fields for GPS listener
    private final static int MIN_DISTANCE_BETWEEN_UPDATES = 1;
    private final static int MIN_TIME_INTERVAL_BETWEEN_UPDATES = 5000;

    private Button stopGame;
    private Button gpsButton;
    private static Button lostFlag;


    private TextView gpsInfo;
    private TextView flagInfo;
    private static TextView flagStatus;

    private boolean flagFound;

    private Marker flagMarker;
    private GoogleMap googleMap;

    private LocationManager locationManager;
    private GPSLocationListenerSelfTest locationListener;

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

        System.out.println("FIREBASE TOKEN:" + FirebaseInstanceId.getInstance().getToken());
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);

        stopGame = (Button) findViewById(R.id.stopGame);
        gpsButton = (Button) findViewById(R.id.gpsButton);
        lostFlag = (Button) findViewById(R.id.lostFlag);

        gpsInfo = (TextView) findViewById(R.id.gpsInfo);
        flagInfo = (TextView) findViewById(R.id.flagInfo);
        flagStatus = (TextView) findViewById(R.id.flagStatus);
        flagStatus.setText("Vlag gevonden: Nee");

        locationListener = new GPSLocationListenerSelfTest(gpsInfo);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Location currentLocation = getCurrentLocation();

        LatLng currentLatLng = new LatLng(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

        this.googleMap = googleMap;
    }
    public void lostFlag(View view) {
        if (view.getId() == lostFlag.getId()) {
            locationManager.removeUpdates(locationListener);
            flagMarker.remove();
            Location flagLocation = getCurrentLocation();

            addFlagMarker(flagLocation);
            locationListener.newGame(flagLocation);
            locationListener.flagLost(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_INTERVAL_BETWEEN_UPDATES,
                    MIN_DISTANCE_BETWEEN_UPDATES,
                    locationListener);
            gpsButton.setText(R.string.start_game);
            lostFlag.setEnabled(false);
            gpsButton.setEnabled(true);
        }
    }
    public void hideFlag(View view) {
        if (view.getId() == gpsButton.getId()) {
            if (gpsButton.getText().toString().equals(getString(R.string.hide_flag))) {
                Location flagLocation = getCurrentLocation();

                addFlagMarker(flagLocation);
                locationListener.newGame(flagLocation);

                gpsButton.setText(R.string.start_game);
                stopGame.setEnabled(true);

                //TODO: Send Flag to other device using FCM

            } else {
                gpsButton.setText(R.string.flag_hidden);
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

            if(flagMarker != null) {
                flagMarker.remove();
            }

            vibrator.cancel();

            sendNotification("Levend Stratego", "Het spel is afgelopen! Kom terug.");

        }
    }

    //TODO: Make static to use it on other classes?
    private void sendNotification(String title, String text){
        String sound = "default";
        String priority = "high";

        try {
            String result = new SendNotificationTask(TOPIC, title, text, sound, priority).execute("https://fcm.googleapis.com/fcm/send").get();
            System.out.println(result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get current location by requesting a single location update.
     * @return lastKnownLocation which is curent location
     */
    private Location getCurrentLocation() {
        Location currentLocation;

        try {
            currentLocation = new GetCurrentLocationTask().execute(locationManager).get();

            if(currentLocation == null){
                throw new NullPointerException("currentLocation is null!");
            }
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            e.printStackTrace();

            String provider = locationManager.getBestProvider(new Criteria(), true);
            currentLocation = locationManager.getLastKnownLocation(provider);
        }

        return currentLocation;
    }

    /**
     * Add flagMarker on googleMap
     * @param flagLocation location of the flag
     */
    private void addFlagMarker(Location flagLocation){
        LatLng latLng = new LatLng(flagLocation.getLatitude(), flagLocation.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(latLng).title("Vlag");
        flagMarker = googleMap.addMarker(marker);

        String latitude = "Latitude: " + flagLocation.getLatitude();
        String longitude = "Longitude: " + flagLocation.getLongitude();
        String flagInfoText = "Flag info:\n" + longitude + "\n" + latitude + "\n";

        flagInfo.setText(flagInfoText);
    }

    /**
     * Sent vibrations as hint to user
     * @param distance used to determine vibration patterns
     */
    public static void sendHintVibration(float distance) {
        //TODO: Find good patterns for sending hints
        if (distance < 2.5) {
            long pattern[] = {0, 5000};
            vibrator.vibrate(pattern, -1);
            //flag found
            // AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getApplicationContext());
            // dialogBuilder.setTitle("Vlag gevonden");
            // dialogBuilder.setMessage("Ga terug naar de basis!");
            // dialogBuilder.setCancelable(true);
            lostFlag.setEnabled(true);
            flagStatus.setText("Vlag gevonden: Ja");
        } else if (distance < 10) {
            long pattern[] = {0, 100, 100, 100, 100, 100, 100, 100, 100};
            vibrator.vibrate(pattern, -1);
        } else if (distance < 25) {
            long pattern[] = {0, 100, 100, 100, 100, 100, 100};
            vibrator.vibrate(pattern, -1);
        } else if (distance < 50) {
            long pattern[] = {0, 100, 100, 100, 100};
            vibrator.vibrate(pattern, -1);
        } else {
            long pattern[] = {0, 100};
            vibrator.vibrate(pattern, -1);
        }
    }
}
