package nicail.bscs.com.emercify.Home;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import nicail.bscs.com.emercify.Likes.LikesActivity;
import nicail.bscs.com.emercify.Likes.MapActivity;
import nicail.bscs.com.emercify.Login.LoginActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Share.ShareActivity;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.CheckInternet;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.MainfeedListAdapter;
import nicail.bscs.com.emercify.Utils.MainfeedRecyclerAdapter;
import nicail.bscs.com.emercify.Utils.Notify;
import nicail.bscs.com.emercify.Utils.Permissions;
import nicail.bscs.com.emercify.Utils.SectionsPagerAdapter;
import nicail.bscs.com.emercify.Utils.UniversalImageLoader;
import nicail.bscs.com.emercify.Utils.ViewCommentsFragment;
import nicail.bscs.com.emercify.Utils.ViewPostFragment;
import nicail.bscs.com.emercify.models.Photo;

public class HomeActivity extends AppCompatActivity implements
        ViewPostFragment.OnCommentThreadSelectedListener{

    @Override
    public void onCommentThreadSelecetedListener(Photo photo) {
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable("PHOTO",photo);
        args.putString(getString(R.string.home_activity),"Home Activity");
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    private HomeFragment fragment;
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;
    private static final int INTERVAL = 3000;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private Context mContext = HomeActivity.this;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ImageView nonetimage;
    private FirebaseMethods firebaseMethods;
    private RelativeLayout mViewPager;
    private FrameLayout mFrameLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    private ProgressBar pb;
    private RelativeLayout mRelativeLayout;
    private boolean mLocationPermissionGranted = false;
    private double latitude, longitude;
    private TextView nonet;
    private TextView noposts;
    private ImageView nopostimage;
    private MainfeedRecyclerAdapter mainfeedRecyclerAdapter;
    private RecyclerView recyclerView;
    private Handler handler = new Handler();
    private Runnable runnable;
    private ImageView emercifyhome;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");
        emercifyhome = findViewById(R.id.emercify_text);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        pb = (ProgressBar) findViewById(R.id.progress_Bar1);
        mViewPager = (RelativeLayout) findViewById(R.id.rellayout2);
        mFrameLayout = (FrameLayout) findViewById(R.id.home_container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);
        nonet = (TextView) findViewById(R.id.no_net);
        noposts = (TextView) findViewById(R.id.no_postavail);
        recyclerView = (RecyclerView) findViewById(R.id.listViewhome);
        nonetimage = (ImageView) findViewById(R.id.no_netimage);
        nopostimage = (ImageView) findViewById(R.id.nopost_image);
        setupFireBaseAuth();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                initImageLoader();
                setupBottomNavigationView();
                setupViewPager();
            } else {
                getLocationPermission();
            }
        }
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if(checkPermissionsArray()){
            mLocationPermissionGranted = true;
            initImageLoader();
            setupBottomNavigationView();
            setupViewPager();
        }
        else{
            verifyPermissions(Permissions.PERMISSIONS);
        }
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mLocationPermissionGranted = true;
//            initImageLoader();
//            setupBottomNavigationView();
//            setupViewPager();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//        }
    }

    public boolean checkPermissionsArray(){
        String[] permissions = Permissions.PERMISSIONS;
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

        int permissionRequest = ActivityCompat.checkSelfPermission(HomeActivity.this,permission);
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
        ActivityCompat.requestPermissions(HomeActivity.this,permissions,PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HomeActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HomeActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                Log.d(TAG, "onRequestPermissionsResult: granResults " + grantResults.toString());
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    initImageLoader();
                    setupBottomNavigationView();
                    setupViewPager();
                    mLocationPermissionGranted = true;
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    initImageLoader();
                    setupBottomNavigationView();
                    setupViewPager();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }


    public void OnCommentThreadSelected(Photo photo,String callingActivity){
        Log.d(TAG, "OnCommentThreadSelected: selected a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable("PHOTO",photo);
        args.putString(getString(R.string.home_activity),callingActivity);
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    public void OnViewClickListener(Photo photo){
        Log.d(TAG, "OnViewClickListener: HomeActivity " + photo.toString());
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable("PHOTO",photo);
        args.putInt(getString(R.string.activity_number),ACTIVITY_NUM);
        args.putString(getString(R.string.home_activity),"Home Activity");
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    public void OnDeleteClickListener(Photo photo,int position){
        Log.d(TAG, "OnDeleteClickListener: HomeActivity " + photo.toString());
        Log.d(TAG, "OnDeleteClickListener: " + position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseMethods.deletePhoto(photo.getUser_id(),photo.getPhoto_id());
                        fragment.updateMainFeed(position);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout(){
        Log.d(TAG, "hideLayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        int num = getSupportFragmentManager().getBackStackEntryCount();
//        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (num == 1) {
            finish();
        }
        else if(num == 2){
            showLayout();
        }
        else{
            getFragmentManager().popBackStack();
        }
        super.onBackPressed();
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
    //Responsible for adding 3 tabs camera, home, messages

    private void setupViewPager(){
        //SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = HomeActivity.this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.rellayout2,fragment);
        fragmentTransaction.addToBackStack("Home");
        fragmentTransaction.commit();
        getLastKnownLocation();
    }

    //Bottom Navigation View Setup
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        Log.d(TAG, "setupBottomNavigationView: " + bottomNavigationViewEx);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        int incoming = 0;
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx,incoming);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    //Firebase Section

    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in");
        if(user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void setupFireBaseAuth(){
        Log.d(TAG, "setupFireBaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        firebaseMethods = new FirebaseMethods(HomeActivity.this);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                checkCurrentUser(user);

                if(user != null){
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                }
                else{
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    if(location != null){
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        firebaseMethods.updateLocation(latitude,longitude);
                        firebaseMethods.updateDevice_token(
                                FirebaseInstanceId.getInstance().getToken());
                        firebaseMethods.updateOnlineStatus(true);
                    }
                    else{
                        Toast.makeText(mContext, "Can't find your Location.\nOpening Google Maps", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(HomeActivity.this,MapActivity.class);
                        startActivity(intent);
                    }

                    Log.d(TAG, "onComplete: latitude: " + latitude + "\n" + "longitude" + longitude);
                }
            }
        });
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
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
        stopNotificationRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseMethods.updateOnlineStatus(true);
        startNotificationRunnable();
    }

    @Override
    public void onPause() {
        super.onPause();
        firebaseMethods.updateOnlineStatus(false);
    }
}
