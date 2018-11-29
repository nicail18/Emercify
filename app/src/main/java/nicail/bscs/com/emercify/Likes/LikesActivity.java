package nicail.bscs.com.emercify.Likes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.GlideApp;

public class LikesActivity extends AppCompatActivity {
    private static final String TAG = "LikesActivity";

    private static final int ACTIVITY_NUM = 3;

    private Context mContext = LikesActivity.this;
    private ImageView ivMap,test;
    private String token;

    ListView simpleList;
    String descList [] = {"has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!"};
    String timeList [] = {
            "7 hours ago",
            "7 hours ago",
            "7 hours ago",
            "7 hours ago",
            "7 hours ago",
            "7 hours ago",
            "7 hours ago",
            "7 hours ago",
            "7 hours ago",
            "7 hours ago"};
    String countryList[] = {"Duterte", "Batman", "Flash", "Kuya Jobert", "Ferdinand", "Panot", "Pedro Pendukot", "Pokwang", "Superman", "Wonderwoman ko"};
    int flags[] = {
            R.drawable.duterte,
            R.drawable.batman,
            R.drawable.flash,
            R.drawable.kuyajobert,
            R.drawable.marcos,
            R.drawable.noynoy,
            R.drawable.pedro,
            R.drawable.pokwang,
            R.drawable.superman,
            R.drawable.wonderwoman};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifs);
        ivMap = (ImageView) findViewById(R.id.ivMap);
        test = (ImageView) findViewById(R.id.testNotif);
        Log.d(TAG, "onCreate: starting.");


        ivMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LikesActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Notify().execute();
            }
        });
        simpleList = (ListView) findViewById(R.id.notif_listview);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), countryList, flags);
        simpleList.setAdapter(customAdapter);

        token = FirebaseInstanceId.getInstance().getToken();

        setupBottomNavigationView();
    }

    public class Notify extends AsyncTask<Void, Void, Void>{
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
                info.put("body","Hello Test Notification");


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

    //Bottom Navigation View Setup
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx, ACTIVITY_NUM);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    public class CustomAdapter extends BaseAdapter {
        Context context;
        String countryList[];
        int flags[];
        LayoutInflater inflter;

        public CustomAdapter(Context applicationContext, String[] countryList, int[] flags) {
            this.context = context;
            this.countryList = countryList;
            this.flags = flags;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return countryList.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.layout_notifs_listview, null);

            TextView country = (TextView) view.findViewById(R.id.username);
            TextView desc = (TextView) view.findViewById(R.id.notif_message);
            TextView time = (TextView) view.findViewById(R.id.timestamp);
            CircleImageView icon = (CircleImageView) view.findViewById(R.id.profile_photo);

            country.setText(countryList[i]);
            desc.setText(descList[i]);
            time.setText(timeList[i]);

            String internetUrl = "http://futurefemaleleader.com/wp-content/uploads/2017/08/150508211850-kim-jong-un-sub-missile-test-0905-full-169_clipped_rev_1.png";

            GlideApp
                    .with(getApplicationContext())
                    .load(internetUrl)
                    .placeholder(R.mipmap.ic_emercify_launcher)
                    .error(R.drawable.ic_error)
                    .centerCrop()
                    .into(icon);


            return view;

        }
    }
}
