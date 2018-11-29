package nicail.bscs.com.emercify.Likes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.GlideApp;

public class LikesActivity extends AppCompatActivity {
    private static final String TAG = "LikesActivity";

    private static final int ACTIVITY_NUM = 3;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private Context mContext = LikesActivity.this;
    private ImageView ivMap;
    private boolean mLocationPermissionGranted = false;

    ListView simpleList;
    String descList [] = {"has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!", "has reported an incident!"};
    String timeList [] = {"666 hours ago", "666 hours ago", "666 hours ago", "666 hours ago", "666 hours ago", "666 hours ago", "666 hours ago", "666 hours ago", "666 hours ago", "666 hours ago"};
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
        Log.d(TAG, "onCreate: starting.");


        ivMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LikesActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        simpleList = (ListView) findViewById(R.id.notif_listview);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), countryList, flags);
        simpleList.setAdapter(customAdapter);

        setupBottomNavigationView();


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

    private void loadImageByInternetUrl(){

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

            TextView country = (TextView) view.findViewById(R.id.notif_text_name);
            TextView desc = (TextView) view.findViewById(R.id.notif_text_desc);
            TextView time = (TextView) view.findViewById(R.id.notif_text_time);
            CircleImageView icon = (CircleImageView) view.findViewById(R.id.notif_list_item);

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
