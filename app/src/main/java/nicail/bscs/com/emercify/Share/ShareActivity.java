package nicail.bscs.com.emercify.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.Permissions;
import nicail.bscs.com.emercify.Utils.SectionsPagerAdapter;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";

    private static final int ACTIVITY_NUM = 2;
    private static final int INTERVAL = 3000;

    private ViewPager mViewPager;
    private FirebaseMethods firebaseMethods;

    private Context mContext = ShareActivity.this;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        firebaseMethods = new FirebaseMethods(this);
        Log.d(TAG, "onCreate: starting.");

        if(checkPermissionsArray(Permissions.PERMISSIONS)){
            setUpViewPager();
        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }

    }

    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }


    private void setUpViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText("GALLERY");
        tabLayout.getTabAt(1).setText("PHOTO");
    }

    public int getTask(){
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    //Permissions
    private int count = 0;
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permission array");

        for(int i = 0; i< permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this,permission);
        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: Permission is not granted for " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: Permission is granted for " + permission);
            return true;
        }
    }

    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(ShareActivity.this,permissions,2);
    }
    private void startNotificationRunnable(){
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retrieveNotifs();
                handler.postDelayed(runnable,INTERVAL);
            }
        },INTERVAL);
    }

    private void stopNotificationRunnable(){
        handler.removeCallbacks(runnable);
    }

    private void retrieveNotifs(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_notification))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    boolean check = (boolean) ds.child("badge_seen").getValue();
                    if(!check){
                        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
                        BottomNavigationViewHelper.showBadge(
                                mContext,
                                bottomNavigationViewEx,
                                R.id.ic_alert,
                                "1");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setUpViewPager();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseMethods.updateOnlineStatus(true);
        startNotificationRunnable();
    }

    @Override
    public void onPause() {
        super.onPause();;
        firebaseMethods.updateOnlineStatus(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseMethods.updateOnlineStatus(false);
        stopNotificationRunnable();
    }
}
