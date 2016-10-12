package fhict.design4nature.levendstratego.datahandlers;

import android.os.AsyncTask;

import java.net.URL;

/**
 * Created by fhict.
 */
public class SendNotificationTask extends AsyncTask<String, Void, String> {

    private static final String KEY = "AIzaSyCF3c4IIcmF1AKs8SU0ny5g5SG05DbvCbY";

    private final String topic;
    private final String title;
    private final String message;
    private final String sound;
    private final String priority;

    private APIConnector APIConnector;

    public SendNotificationTask(String topic, String title, String message, String sound, String priority){
        this.topic = topic;
        this.title = title;
        this.message = message;
        this.sound = sound;
        this.priority = priority;
    }
    @Override
    protected String doInBackground(String... params) {

        return getJSONResult(params[0]);
    }

    private String getJSONResult(String URL) {
        String result = null;

        try {
            URL url = new URL(URL);
            APIConnector = new APIConnector(url, "POST", KEY);
            APIConnector.createJSONToSend(topic, title, message, sound, priority);
            result = APIConnector.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            APIConnector.closeConnection();
        }

        System.out.println("AsyncTask Completed. Result: " + result);
        return result;
    }
}