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
import android.widget.ImageView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutionException;

import fhict.design4nature.levendstratego.datahandlers.SendNotificationTask;

/**
 * Created by fhict.
 */
public class MainActivity extends AppCompatActivity {

    // Static fields FCM topic
    // Change this if multiple users are testing application.
    private static final String TOPIC = "game";

    // Static fields for GPS listener
    private final static int MIN_DISTANCE_BETWEEN_UPDATES = 0;
    private final static int MIN_TIME_INTERVAL_BETWEEN_UPDATES = 1000;

    private Button stopGame;
    private ImageView hideFlag;
    private ImageView phoneChange;
    private static Button lostFlag;

    private LocationManager locationManager;
    private GPSLocationListener locationListener;

    private static Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hide_flag_activity);

        System.out.println("FIREBASE TOKEN:" + FirebaseInstanceId.getInstance().getToken());
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);


        stopGame = (Button) findViewById(R.id.stopGame);
        hideFlag = (ImageView) findViewById(R.id.hideFlag);
        phoneChange = (ImageView) findViewById(R.id.phoneChange);
        lostFlag = (Button) findViewById(R.id.lostFlag);

        locationListener = new GPSLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void lostFlag(View view) {
        if (view.getId() == lostFlag.getId()) {
            locationManager.removeUpdates(locationListener);
            Location flagLocation = getCurrentLocation();

            locationListener.newGame(flagLocation);
            locationListener.flagLost(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_INTERVAL_BETWEEN_UPDATES,
                    MIN_DISTANCE_BETWEEN_UPDATES,
                    locationListener);
            lostFlag.setEnabled(false);
        }
    }

    public void hideFlag(View view) {
        if (view.getId() == hideFlag.getId()) {
            locationListener.setFlagLocation(getCurrentLocation());
            setContentView(R.layout.change_phone_activity);
            phoneChange = (ImageView) findViewById(R.id.phoneChange);

        }
    }

    public void phoneChange(View view) {
        if (view.getId() == phoneChange.getId()) {
            setContentView(R.layout.back_to_base_activity);
        }
    }

    //TODO: StopGame
    public void stopGame(View view) {
        if (view.getId() == stopGame.getId()) {
            stopGame.setEnabled(false);

            locationManager.removeUpdates(locationListener);

            vibrator.cancel();

            sendNotification("Levend Stratego", "Het spel is afgelopen! Kom terug.");

        }
    }

    //TODO: Make static to use it on other classes?
    private void sendNotification(String title, String text) {
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
     *
     * @return lastKnownLocation which is curent location
     */
    private Location getCurrentLocation() {
        String provider = locationManager.getBestProvider(new Criteria(), true);
        return locationManager.getLastKnownLocation(provider);
    }


    /**
     * Sent vibrations as hint to user
     *
     * @param distance used to determine vibration patterns
     */
    public static void sendHintVibration(float distance) {
        if (distance < 2.5) {
            long pattern[] = {0, 3000};
            vibrator.vibrate(pattern, -1);
            lostFlag.setEnabled(true);
        } else if (distance < 10) {
            long pattern[] = {0, 100, 100, 100, 100, 100, 100, 100, 100};
            vibrator.vibrate(pattern, -1);
        }
    }

    /**
     * Called when application finished!
     */
    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(locationListener);
        vibrator.cancel();

        super.onDestroy();
    }

    /***
     * Override onBackPressed to let the user decide what happens with the application when someone tries to close it.
     */
    @Override
    public void onBackPressed() {
        //TODO: Only game is playing show dialog

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
}
