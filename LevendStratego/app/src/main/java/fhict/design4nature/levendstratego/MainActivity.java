package fhict.design4nature.levendstratego;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutionException;

import fhict.design4nature.levendstratego.datahandlers.SendNotificationTask;

/**
 * Created by fhict.
 */
public class MainActivity extends AppCompatActivity {

    // Static fields FCM topic.
    // Switch these after deploying to an device
    private static final String TOPIC = "TEAM";
    private static final String OTHER_TOPIC = "GAME";

    // Static fields for GPS listener
    private final static int MIN_DISTANCE_BETWEEN_UPDATES = 0;
    private final static int MIN_TIME_INTERVAL_BETWEEN_UPDATES = 1000;

    // private Button stopGame;
    private ImageView revive;
    private ImageView hideFlag;
    private ImageView flagInfo;
    private ImageView phoneChange;
    private static Button loseFlag;

    private FrameLayout frameLayout;

    private LocationManager locationManager;
    private GPSLocationListener locationListener;

    private static Vibrator vibrator;
    private static CountDownTimer timer;

    private static boolean flagFound;
    private static boolean gameIsStarted;
    private static boolean timerIsRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        setHideFlagView();

        gameIsStarted = false;
        timerIsRunning = false;

        subscribeToFireBaseTopic(TOPIC);
        unSubscribeToFireBaseTopic(OTHER_TOPIC);

        initControls();
        startGPSListener();
    }

    private void setFrameView(int resId) {
        View view = LayoutInflater.from(this).inflate(resId, null);
        frameLayout.removeAllViews();
        frameLayout.addView(view);
    }

    private void setHideFlagView() {
        setFrameView(R.layout.hide_flag_activity);
        hideFlag = (ImageView) findViewById(R.id.hideFlag);
    }

    private void setChangePhoneView() {
        setFrameView(R.layout.change_phone_activity);
        phoneChange = (ImageView) findViewById(R.id.phoneChange);
    }

    private void setGameView() {
        setFrameView(R.layout.game_activity);

        loseFlag = (Button) findViewById(R.id.loseFlag);
        flagInfo = (ImageView) findViewById(R.id.flagInfo);

        flagFound = false;
        gameIsStarted = true;

        loseFlag.setEnabled(false);
        locationListener.setGameStarted(true);

        flagInfo.setImageResource(R.drawable.flag_icon_false);
    }

    private void setBackToBaseView() {
        setFrameView(R.layout.back_to_base_activity);
        revive = (ImageView) findViewById(R.id.revive);
    }

    private void subscribeToFireBaseTopic(String topic) {
        System.out.println("Subscribed to " + topic);
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    private void unSubscribeToFireBaseTopic(String topic) {
        System.out.println("Unsubscribed from " + topic);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }

    private void initControls() {
        locationListener = new GPSLocationListener();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Tick
                // TODO: Vibrate in here instead of in sendHintVibration()?
            }

            @Override
            public void onFinish() {
                flagCaptured();
            }
        };
    }

    private void startGPSListener() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_TIME_INTERVAL_BETWEEN_UPDATES,
                MIN_DISTANCE_BETWEEN_UPDATES,
                locationListener);
    }

    public void hideFlag(View view) {
        if (view.getId() == hideFlag.getId()) {
            locationListener.setFlagLocation(getCurrentLocation());
            setChangePhoneView();
        }
    }

    public void phoneChange(View view) {
        if (view.getId() == phoneChange.getId()) {
            setGameView();
        }
    }

    // TODO: Send notification flag is lost?
    public void loseFlag(View view) {
        if (view.getId() == loseFlag.getId()) {

            confirmLostFlag();

            if(!flagFound) {
                setBackToBaseView();

                timer.cancel();
                //flagFound = false;
                gameIsStarted = false;
                timerIsRunning = false;
                locationListener.setGameStarted(false);
                locationListener.setFlagLocation(getCurrentLocation());
                flagInfo.setImageResource(R.drawable.flag_icon_false);
            }
        }
    }

    /***
     * Ask user if he lost the flag. To avoid pressing button accidentally
     */
    public void confirmLostFlag() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Vlag verloren");
        dialogBuilder.setMessage("Weet u zeker dat u de vlag verloren bent?");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setNeutralButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                flagFound = false;
            }
        });


        dialogBuilder.setNegativeButton("Nee", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                flagFound = true;
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void revive(View view) {
        if (view.getId() == revive.getId()) {
            setGameView();
        }
    }

    private void flagCaptured() {
        flagFound = true;
        loseFlag.setEnabled(true);

        long pattern[] = {0, 3000};
        vibrator.vibrate(pattern, -1);

        flagInfo.setImageResource(R.drawable.flag_icon_true);

        sendNotification("Vlag gepakt!", "Probeer te verdedigen!");
    }

    /**
     * Send notification to other device
     *
     * @param title notifications title
     * @param text  notifications text
     */
    private void sendNotification(final String title, final String text) {
        String sound = "default";
        String priority = "high";

        System.out.println("Sent to: " + OTHER_TOPIC);
        try {
            String result = new SendNotificationTask(OTHER_TOPIC, title, text, sound, priority).execute("https://fcm.googleapis.com/fcm/send").get();
            System.out.println(result);
            notificationSent = true;
        } catch (InterruptedException | ExecutionException | NullPointerException e) {

            Handler handler = new Handler();

            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    if (!notificationSent) {
                        sendNotification(title, text);
                    }
                }
            };

            handler.postDelayed(runner, 1000);

            e.printStackTrace();
        }
    }

    boolean notificationSent = false;

    /**
     * Get current location by requesting a single location update.
     *
     * @return lastKnownLocation which is current location
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
        if (!flagFound && gameIsStarted) {
            if (distance < 10) {
                if (!timerIsRunning) {
                    timer.start();
                    timerIsRunning = true;
                }
                long pattern[] = {0, 500, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500};
                vibrator.vibrate(pattern, -1);
                //}
            } else if (distance < 20) {
                if (timerIsRunning) {
                    timer.cancel();
                    timerIsRunning = false;
                }
                long pattern[] = {0, 100};
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    /**
     * Called when application finished!
     */
    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(locationListener);
        vibrator.cancel();
        timer.cancel();

        //TODO: Kill notification if there is one

        super.onDestroy();
    }

    /***
     * Override onBackPressed to let the user decide what happens with the application when someone tries to close it.
     */
    @Override
    public void onBackPressed() {
        if (gameIsStarted) {
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}