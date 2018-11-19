package nicail.bscs.com.emercify.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.Permissions;
import nicail.bscs.com.emercify.Utils.SectionsPagerAdapter;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private static final int ACTIVITY_NUM = 2;

    private ViewPager mViewPager;

    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
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
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permission array");

        for(int i = 0; i< permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)){
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

        ActivityCompat.requestPermissions(ShareActivity.this,permissions,1);
    }
}
