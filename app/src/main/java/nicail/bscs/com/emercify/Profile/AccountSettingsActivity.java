package nicail.bscs.com.emercify.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.SectionsStatePagerAdapater;

public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingsActivity";
    private static final int ACTIVITY_NUM = 4;
    private Context mContext;
    public SectionsStatePagerAdapater pagerAdapater;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;

    private String address;
    private double latitude, longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        mContext = AccountSettingsActivity.this;
        Log.d(TAG, "onCreate: started");
        mViewPager = (ViewPager) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rellayout1);


        setupSettingsList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();

        //Setup the backarrow for navigating back to "ProfileActivity"
        ImageView backarrow = (ImageView) findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigatiing back to 'Profile Activity' ");
                finish();
            }
        });
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap))) {
            Log.d(TAG, "getIncomingIntent: new incoming image url");

            address = intent.getStringExtra(getString(R.string.image_address));
            latitude =  b.getDouble(getString(R.string.image_latitude));
            longitude =  b.getDouble(getString(R.string.image_longitude));
            if (intent.getStringExtra(mContext.getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))) {
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto("profile_photo", null, 0,
                            intent.getStringExtra(mContext.getString(R.string.selected_image)), null,address,latitude,longitude);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto("profile_photo", null, 0,
                            null, (Bitmap) intent.getParcelableExtra(mContext.getString(R.string.selected_bitmap)),address,latitude,longitude);
                }

            }
        }

        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "getIncomingIntent: received incoming intent from " + getString(R.string.profile_activity));
            setViewPager(pagerAdapater.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    private void setupFragments(){
        pagerAdapater = new SectionsStatePagerAdapater(getSupportFragmentManager());
        pagerAdapater.addFragment(new EditProfileFragment(),getString(R.string.edit_profile_fragment));
        pagerAdapater.addFragment(new SignOutFragment(),getString(R.string.sign_out_fragment));
    }

    public void setViewPager(int fragmentNumber){
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setmViewPager: navigation to fragment #:" + fragmentNumber);
        mViewPager.setAdapter(pagerAdapater);
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupSettingsList(){
        Log.d(TAG, "setupSettingsList: Account Settings List");
        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        ArrayAdapter adapter  = new ArrayAdapter(mContext,android.R.layout.simple_list_item_1,options);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigating to fragment#:" + position);
                setViewPager(position);
            }
        });
    }

    //Bottom Navigation View Setup
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


}
