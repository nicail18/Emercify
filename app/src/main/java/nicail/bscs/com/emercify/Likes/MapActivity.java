package nicail.bscs.com.emercify.Likes;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.DownloadImageTask;
import nicail.bscs.com.emercify.Utils.FetchURL;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.LocationService;
import nicail.bscs.com.emercify.Utils.MyClusterManagerRenderer;
import nicail.bscs.com.emercify.Utils.Notify;
import nicail.bscs.com.emercify.Utils.TaskLoadedCallback;
import nicail.bscs.com.emercify.models.ClusterMarker;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        TaskLoadedCallback {

    private static final String TAG = "MapActivity";

    private static final int ACTIVITY_NUM = 3;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mMapView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Context mContext = MapActivity.this;
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary;
    private double latitude, longitude, lat, lon;
    private Photo mPhoto;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private Bitmap bit;
    private final String[] snippet = new String[1];
    private String username;
    private ImageView ivBackArrow;
    private FirebaseMethods firebaseMethods;
    private GeoApiContext geoApiContext = null;
    private Polyline polyline;
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate: starting");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ivBackArrow = (ImageView) findViewById(R.id.ivBackarrow);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait...");

        firebaseMethods = new FirebaseMethods(this);

        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initGoogleMap(savedInstanceState);

        setupBottomNavigationView();

    }

    private void checkIntent() {
        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (intent.hasExtra("PHOTO")) {
            mPhoto = intent.getParcelableExtra("PHOTO");
            Log.d(TAG, "checkIntent: " + mPhoto);
            latitude = mPhoto.getLatitude();
            longitude = mPhoto.getLongitude();
            getLastKnownLocation();
            //mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(mPhoto.getCaption()));
        }
        else if(intent.hasExtra("INTENT PHOTO")){
            mPhoto = intent.getParcelableExtra("INTENT PHOTO");
            latitude = mPhoto.getLatitude();
            longitude = mPhoto.getLongitude();
            getLastKnownLocation();
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            getLastKnownLocation();
        }
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    private void addMapMarkers(){
        if(mGoogleMap != null){
            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(),mGoogleMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        MapActivity.this,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            try{
                Log.d(TAG, "addMapMarkers: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference
                        .child(mContext.getString(R.string.dbname_users))
                        .orderByChild("user_id")
                        .equalTo(mPhoto.getUser_id());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            username = ds.getValue(User.class).getUsername();
                            if(mPhoto.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                snippet[0] = "This is your Post";
                            }
                            else{
                                snippet[0] = "This is " + ds.getValue(User.class).getUsername() +"'s post";
                            }
                            int avatar = R.mipmap.ic_emercify;
                            Log.d(TAG, "onDataChange: " + avatar);
                            new AsyncImageBitmap().execute(mPhoto.getImage_path());

                            ClusterMarker newClusterMarker = new ClusterMarker(
                                    new LatLng(mPhoto.getLatitude(),mPhoto.getLongitude()),
                                    ds.getValue(User.class).getUsername(),
                                    snippet[0],
                                    avatar,
                                    ds.getValue(User.class)
                            );
                            mClusterManager.addItem(newClusterMarker);
                            mClusterMarkers.add(newClusterMarker);

                        }
                        mClusterManager.cluster();
                        Intent intent = getIntent();
                        if(intent.hasExtra("PHOTO")){
                            setCameraView();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }catch(NullPointerException e){
                Log.d(TAG, "addMapMarkers: NullPointerException " + e.getMessage());
            }
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if(polyline != null){
            polyline.remove();
        }
        polyline = mGoogleMap.addPolyline((PolylineOptions) values[0]);
        zoomRoute(polyline.getPoints());
    }

    public class AsyncImageBitmap extends AsyncTask<String, Void, Bitmap> {

        @Override
        public Bitmap doInBackground(String... urls) {
            final String url = urls[0];
            Bitmap bitmap = null;

            try {
                final InputStream inputStream = new URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (final MalformedURLException malformedUrlException) {
                // Handle error
            } catch (final IOException ioException) {
                // Handle error
            }
            return bitmap;
        }

        @Override
        public void onPostExecute(Bitmap bitmap) {
            int markerWidth,markerHeight;
            ImageView imageView = new ImageView(getApplicationContext());
            IconGenerator iconGenerator = new IconGenerator(getApplicationContext());
            markerWidth = (int) getResources().getDimension(R.dimen.custom_marker_image);
            markerHeight = (int) getResources().getDimension(R.dimen.custom_marker_image);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth,markerHeight));
            int padding = (int) getResources().getDimension(R.dimen.custom_marker_padding);
            imageView.setPadding(padding,padding,padding,padding);
            iconGenerator.setContentView(imageView);
            imageView.setImageBitmap(bitmap);
            Bitmap bit = iconGenerator.makeIcon();
            mGoogleMap.clear();
            Intent intent = getIntent();
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude,longitude))
                    .title(username)
                    .snippet(snippet[0])
                    .icon(BitmapDescriptorFactory.fromBitmap(bit)));
            if(intent.hasExtra("INTENT PHOTO")){
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lon))
                        .title("Your Location"));
                String url = getUrl(new LatLng(lat,lon),new LatLng(latitude,longitude),"driving");
                new FetchURL(MapActivity.this).execute(url,"driving");
                startUserLocationsRunnable();
            }
            progressDialog.dismiss();
        }
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                MapActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving update locations.");
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserlocation();
                handler.postDelayed(runnable,LOCATION_UPDATE_INTERVAL);
            }
        },LOCATION_UPDATE_INTERVAL);
    }

    private void stopUserLocationRunnable(){
        handler.removeCallbacks(runnable);
    }

    private void retrieveUserlocation(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild("user_id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: ");
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    String device_token = ds.child("device_token").toString();
                    double userLat = (double) ds.child("latitude").getValue();
                    double userLong = (double) ds.child("longitude").getValue();
                    float[] distance = new float[1];
                    Location.distanceBetween(userLat, userLong
                            , latitude, longitude, distance);
                    Log.d(TAG, "onDataChange: retrieveUserlocation " + device_token);
                    Log.d(TAG, "onDataChange: retrieveUserlocation " + FirebaseInstanceId.getInstance().getToken());
                    Log.d(TAG, "onDataChange: retrieveUserlocation photo " + latitude);
                    Log.d(TAG, "onDataChange: retrieveUserlocation photo " + longitude);
                    Log.d(TAG, "onDataChange: retrieveUserlocation user " + userLat);
                    Log.d(TAG, "onDataChange: retrieveUserlocation user " + userLong);
                    if(distance[0] <= 25){
                        String message = "You Have Arrived In The Photo's Destination";
                        Log.d(TAG, "onDataChange: You Have Arrived In The Photo's Destination");
                        stopUserLocationRunnable();
                        new Notify(FirebaseInstanceId.getInstance().getToken(),message).execute();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startNotificationRunnable(){
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retrieveNotifs();
                handler.postDelayed(runnable,LOCATION_UPDATE_INTERVAL);
            }
        },LOCATION_UPDATE_INTERVAL);
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

    private void setCameraView() {
        LatLng latLng = new LatLng(latitude,longitude);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,18);
        mGoogleMap.animateCamera(cameraUpdate);
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    Log.d(TAG, "onComplete: latitude: " + lat + "\n" + "longitude" + lon);
                    Intent intent = getIntent();
                    if(intent.hasExtra("PHOTO")){
                        addMapMarkers();
                    }
                    else if(intent.hasExtra("INTENT PHOTO")){
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat,lon))
                                .title("Your Location"));
                        String url = getUrl(new LatLng(lat,lon),new LatLng(latitude,longitude),"driving");
                        new FetchURL(MapActivity.this).execute(url,"driving");
                        addMapMarkers();
                    }
                    else{
                        latitude = lat;
                        longitude = lon;
                        setCameraView();
                    }
                    startLocationService();
                }
            }
        });
    }

    private String getUrl(LatLng origin, LatLng destination, String directionMode){
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&key=" + getString(R.string.google_api_key);
        return url;
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(MapActivity.this, R.color.grey));
                    polyline.setClickable(true);
                    zoomRoute(polyline.getPoints());
                }
            }
        });
    }

    private void calculateDirections(double latitude,double longitude){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new  com.google.maps.model.LatLng(
                this.latitude,
                this.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        latitude,
                        longitude
                )
        );
        try{
            Log.d(TAG, "calculateDirections: destination: " + destination.toString());
            directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
                @Override
                public void onResult(DirectionsResult result) {
                    Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                    Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                    Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                    Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                    addPolylinesToMap(result);
                }

                @Override
                public void onFailure(Throwable e) {
                    Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
                }
            });
        }catch(ClassCastException e){
            Log.e(TAG, "calculateDirections: " + e.getMessage() );
        }
    }

    public void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.map_container);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_api_key))
                    .build();
        }
        progressDialog.show();
    }

    //Bottom Navigation View Setup
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx,ACTIVITY_NUM);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseMethods.updateOnlineStatus(true);
        startNotificationRunnable();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseMethods.updateOnlineStatus(false);
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopNotificationRunnable();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        map.setMyLocationEnabled(true);
        mGoogleMap = map;
        checkIntent();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public  void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
