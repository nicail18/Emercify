package nicail.bscs.com.emercify.Likes;

import android.Manifest;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
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
import java.util.ArrayList;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.DownloadImageTask;
import nicail.bscs.com.emercify.Utils.MyClusterManagerRenderer;
import nicail.bscs.com.emercify.models.ClusterMarker;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final int ACTIVITY_NUM = 3;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mMapView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Context mContext = MapActivity.this;
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary;
    private double latitude, longitude;
    private Photo mPhoto;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private Bitmap bit;
    private final String[] snippet = new String[1];
    private String username;
    private ImageView ivBackArrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate: starting");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ivBackArrow = (ImageView) findViewById(R.id.ivBackarrow);

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
            addMapMarkers();
            //mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(mPhoto.getCaption()));
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            getLastKnownLocation();
        }
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
                            int avatar = R.mipmap.ic_emercify_launcher_round;
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
                        setCameraView();
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
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude,longitude))
                    .title(username)
                    .snippet(snippet[0])
                    .icon(BitmapDescriptorFactory.fromBitmap(bit)));
            setCameraView();
        }
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
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d(TAG, "onComplete: latitude: " + latitude + "\n" + "longitude" + longitude);
                    setCameraView();
                }
            }
        });
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
    }

    //Bottom Navigation View Setup
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
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
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
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
