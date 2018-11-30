package nicail.bscs.com.emercify.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.sql.Connection;
import java.sql.Statement;

import nicail.bscs.com.emercify.Likes.LikesActivity;
import nicail.bscs.com.emercify.R;


public class FireIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FireIDService";

    @Override
    public void onTokenRefresh() {

            
        String tkn = FirebaseInstanceId.getInstance().getToken();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            FirebaseMethods firebaseMethods = new FirebaseMethods(getApplicationContext());
            firebaseMethods.updateDevice_token(tkn);
        }
        Log.d(TAG, "onTokenRefresh: " + tkn);
        sendRegistrationToServer(tkn);
    }


    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.

        Intent intent = new Intent(this, LikesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1410 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Welcome to Emercify")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1410 /* ID of notification */, notificationBuilder.build());
    }
}
