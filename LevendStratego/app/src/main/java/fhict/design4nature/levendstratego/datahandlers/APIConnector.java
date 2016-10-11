package fhict.design4nature.levendstratego.datahandlers;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fhict.
 */
class APIConnector {

    private HttpURLConnection connection;

    public APIConnector(URL url, String requestMethod, String key) {
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "key=" + key);
            connection.setRequestMethod(requestMethod);
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createJSONToSend(String topic, String title, String body, String sound, String priority) {
        try {
            //Create JSONObject here
            JSONObject notificationJSON = new JSONObject();
            notificationJSON.put("title", title);
            notificationJSON.put("body", body);
            notificationJSON.put("sound", sound);
            notificationJSON.put("priority", priority);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("to", "/topics/" + topic);
            jsonParam.put("notification", notificationJSON);

            OutputStreamWriter out = new   OutputStreamWriter(connection.getOutputStream());
            out.write(jsonParam.toString());
            out.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public String getResult() {
        try {
            // Get Response from site
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder response = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            bufferedReader.close();

            String result = response.toString();
            Log.i("RESPONSE_SERVER", result);

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void closeConnection() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}