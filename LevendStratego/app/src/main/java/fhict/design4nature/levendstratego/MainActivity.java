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
import android.os.Looper;
import android.os.Vibrator;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private static ProgressBar pb;

    private static Button loseFlag;

    private TextView tv_timer;
    private static TextView tv_timer_flag;

    private FrameLayout frameLayout;

    private LocationManager locationManager;
    private static GPSLocationListener locationListener;

    private static Vibrator vibrator;
    private static CountDownTimer timer;
    private static CountDownTimer waitTimer;
    private static CountDownTimer waitView;
    private int timerSec = 20;
    private static int timerFlag = 5;

    private static boolean flagFound;
    private static boolean gameIsStarted;
    private static boolean timerIsRunning;

    private static boolean flagPlaced;
    private static boolean enemyFlagPlaced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        setHideFlagView();

        gameIsStarted = false;
        timerIsRunning = false;
        flagPlaced = false;
        enemyFlagPlaced = false;

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
        setFrameView(R.layout.phone_in_pocket);

        waitView.start();
    }

    private void setBackToBaseView() {
        setFrameView(R.layout.back_to_base_activity);
        revive = (ImageView) findViewById(R.id.revive);
    }

    private void setWaitFlagView(){
        setFrameView(R.layout.wait_flag_placed_activity);
        tv_timer = (TextView) findViewById(R.id.tv_timer);
        pb = (ProgressBar)findViewById(R.id.wait_progressBar);
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
                tv_timer_flag.setText(Integer.toString(timerFlag));
                timerFlag--;
                // TODO: Vibrate in here instead of in sendHintVibration()?
            }

            @Override
            public void onFinish() {
                flagCaptured();
                tv_timer_flag.setText("Gevonden"+ "\n"+" ga terug naar basis");
            }
        };

        waitTimer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerSec--;
                tv_timer.setText(Integer.toString(timerSec));

            }

            @Override
            public void onFinish() {
                setGameView();
            }
        };

        waitView = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                setFrameView(R.layout.game_activity);

                loseFlag = (Button) findViewById(R.id.loseFlag);
                flagInfo = (ImageView) findViewById(R.id.flagInfo);
                tv_timer_flag = (TextView)findViewById(R.id.tv_timer_near_flag);

                flagFound = false;
                gameIsStarted = true;

                loseFlag.setEnabled(false);
                locationListener.setGameStarted(true);

                flagInfo.setImageResource(R.drawable.flag_icon_false);
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
            setWaitFlagView();
            String latitude = Double.toString(getCurrentLocation().getLatitude());
            String longitude = Double.toString(getCurrentLocation().getLongitude());
            sendNotification("Het andere team heeft de vlag geplaatst",latitude+","+ longitude, OTHER_TOPIC);
            flagPlaced = true;
            if(enemyFlagPlaced) {
                pb.setVisibility(View.INVISIBLE);
                waitTimer.start();
            }
        }
    }
    public static Handler UIHandler = new Handler(Looper.getMainLooper());

    public static void enemyHideFlag(String location){
        String[] split = location.split(",");
        String latitude = split[0];
        String longitude = split[1];
        Location flag = new Location("");
        flag.setLatitude(Double.valueOf(latitude));
        flag.setLongitude(Double.valueOf(longitude));
        locationListener.setFlagLocation(flag);
        enemyFlagPlaced = true;
        if(flagPlaced){
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    pb.setVisibility(View.INVISIBLE);
                    waitTimer.start();
                }
            });

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
        }
    }

    /***
     * Ask user if he lost the flag. To avoid pressing button accidentally
     */
    public void confirmLostFlag() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Vlag verloren");
        dialogBuilder.setMessage("Weet je zeker dat je de vlag verloren bent?");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setNeutralButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setBackToBaseView();
                timer.cancel();
                flagFound = false;
                gameIsStarted = false;
                timerIsRunning = false;
                locationListener.setGameStarted(false);
                locationListener.setFlagLocation(getCurrentLocation());
                flagInfo.setImageResource(R.drawable.flag_icon_false);
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

        sendNotification("Vlag gepakt!", "Probeer te verdedigen!", OTHER_TOPIC);
    }

    /**
     * Send notification to other device
     *
     * @param title notifications title
     * @param text  notifications text
     */
    private void sendNotification(final String title, final String text, final String topic) {
        String sound = "default";
        String priority = "high";

        System.out.println("Sent to: " + topic);
        try {
            String result = new SendNotificationTask(topic, title, text, sound, priority).execute("https://fcm.googleapis.com/fcm/send").get();
            System.out.println(result);
            notificationSent = true;
        } catch (InterruptedException | ExecutionException | NullPointerException e) {

            Handler handler = new Handler();

            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    if (!notificationSent) {
                        sendNotification(title, text, topic);
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
                    tv_timer_flag.setText("");
                    timerFlag = 5;
                }
                long pattern[] = {0, 100};
                vibrator.vibrate(pattern, -1);
            }
            else{ //>20
                tv_timer_flag.setText("");
                timerFlag = 5;
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