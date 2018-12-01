package nicail.bscs.com.emercify.Utils;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Notify extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "Notify";

    private String token, message;

    public Notify(String token, String message) {
        this.token = token;
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try{
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","key=AIzaSyCdzpykpnX8MkBg7pRCczUVI39IJhpni7M");
            conn.setRequestProperty("Content-Type","application/json");

            JSONObject json = new JSONObject();

            json.put("to",token);

            JSONObject info = new JSONObject();
            info.put("title","Emercify");
            info.put("body",message);


            json.put("notification",info);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
            conn.getInputStream();
        }catch(Exception e){
            Log.d(TAG, "doInBackground: Exception" + e.getMessage());
        }

        return null;
    }
}
