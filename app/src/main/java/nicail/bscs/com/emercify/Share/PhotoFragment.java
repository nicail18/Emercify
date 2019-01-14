package nicail.bscs.com.emercify.Share;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import nicail.bscs.com.emercify.BuildConfig;
import nicail.bscs.com.emercify.Profile.AccountSettingsActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.Permissions;
import nicail.bscs.com.emercify.dialogs.KindPost;

public class PhotoFragment extends Fragment implements
        KindPost.OnNormalClickListener,
        KindPost.OnEmergencyClickListener{
    private static final String TAG = "HomeFragment";
    private static final int CAMERA_REQUEST_CODE = 5;
    private ExifInterface exif;
    private double latitude, longitude;
    private String mImageAddress;
    private Bitmap mBitmap;
    private TextView nonetcon1;
    private ImageView nonetconimage1;
    @Override
    public void onNormalClickListener(String image, Bitmap bitmap, double latitude,
                                      double longitude, String mImageAddress) {
        Intent intent = new Intent(getActivity(),NextActivity.class);
        Bundle b = new Bundle();
        b.putDouble(getString(R.string.image_latitude),latitude);
        b.putDouble(getString(R.string.image_longitude),longitude);
        b.putString("type","normal");
        intent.putExtra(getString(R.string.selected_bitmap),bitmap);
        intent.putExtra(getString(R.string.image_address),mImageAddress);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public void onEmergencyClickListener(String image, Bitmap bitmap, double latitude,
                                         double longitude, String mImageAddress) {
        Intent intent = new Intent(getActivity(),NextActivity.class);
        Bundle b = new Bundle();
        b.putDouble(getString(R.string.image_latitude),latitude);
        b.putDouble(getString(R.string.image_longitude),longitude);
        b.putString("type","emergency");
        intent.putExtra(getString(R.string.selected_bitmap),bitmap);
        intent.putExtra(getString(R.string.image_address),mImageAddress);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started  ");

        final Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        nonetcon1 = (TextView) view.findViewById(R.id.nonetcon1);
        nonetconimage1 = (ImageView) view.findViewById(R.id.nonetconimage1);
        class Task extends AsyncTask<String, Integer, Boolean> {
            @Override
            protected void onPreExecute() {
                btnLaunchCamera.setVisibility(View.GONE);
                nonetcon1.setVisibility(View.GONE);
                nonetconimage1.setVisibility(View.GONE);
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(Boolean result) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    btnLaunchCamera.setVisibility(View.VISIBLE);
                    nonetcon1.setVisibility(View.GONE);
                    nonetconimage1.setVisibility(View.GONE);
                } else {
                    nonetcon1.setVisibility(View.VISIBLE);
                    nonetconimage1.setVisibility(View.VISIBLE);
                    btnLaunchCamera.setVisibility(View.GONE);
                }
                super.onPostExecute(result);
            }
            @Override
            protected Boolean doInBackground(String... params) {

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        new Task().execute();

        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera");

                if (((ShareActivity) getActivity()).getCurrentTabNumber() == 1) {
                    if (((ShareActivity) getActivity()).checkPermissions(Permissions.CAMERA_PERMISSIONS[0])) {
                        Log.d(TAG, "onClick: starting camera");
                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(camera, 5);
                    } else {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen");
            mBitmap = (Bitmap) data.getExtras().get("data");

            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            Location location = (Location) lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
            latitude = latlng.latitude;
            longitude = latlng.longitude;
            mImageAddress = getCompleteAddressString(latitude,longitude);
            String filename = getRealPathFromURI(data.getData());
            geoTag(filename,latlng);
            Log.d(TAG, "onActivityResult: address" + mImageAddress);
            Log.d(TAG, "onActivityResult: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            Log.d(TAG, "onActivityResult: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));


            if(isRootTask()){
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: " + mBitmap);
                    openDialog();
                    /*Intent intent = new Intent(getActivity(),NextActivity.class);
                    Bundle b = new Bundle();
                    b.putDouble(getString(R.string.image_latitude),latitude);
                    b.putDouble(getString(R.string.image_longitude),longitude);
                    intent.putExtra(getString(R.string.selected_bitmap),bitmap);
                    intent.putExtra(getString(R.string.image_address),mImageAddress);
                    intent.putExtras(b);
                    startActivity(intent);*/
                }catch(NullPointerException e){
                    Log.e(TAG, "onActivityResult: NullPointerException" + e.getMessage() );
                }
            }
            else{
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: " + mBitmap);
                    Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap),mBitmap);
                    intent.putExtra(getString(R.string.image_address),mImageAddress);
                    intent.putExtra(getString(R.string.image_latitude),latitude);
                    intent.putExtra(getString(R.string.image_longitude),longitude);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }catch(NullPointerException e){
                    Log.e(TAG, "onActivityResult: NullPointerException" + e.getMessage() );
                }
            }
        }
    }



    public void openDialog(){
        KindPost kindPost = new KindPost();
        Bundle b = new Bundle();
        b.putDouble(getString(R.string.image_latitude),latitude);
        b.putDouble(getString(R.string.image_longitude),longitude);
        b.putString(getString(R.string.selected_image),null);
        b.putParcelable(getString(R.string.selected_bitmap),mBitmap);
        b.putString(getString(R.string.image_address),mImageAddress);
        kindPost.setArguments(b);
        kindPost.show(((FragmentActivity)getContext()).getSupportFragmentManager(),"KindPost");
        kindPost.setTargetFragment(this,1);
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.d(TAG,"My Current loction address" + strReturnedAddress.toString());
            } else {
                Log.d(TAG,"My Current loction address" + "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getCompleteAddressString: My Current loction address" + "Canont get Address!");
        }
        return strAdd;
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public void geoTag(String filename, LatLng geoTag){
        try {
            exif = new ExifInterface(filename);
            double latitude = Math.abs(geoTag.latitude);
            double longitude = Math.abs(geoTag.longitude);

            int num1Lat = (int) Math.floor(latitude);
            int num2Lat = (int) Math.floor((latitude - num1Lat) * 60);
            double num3Lat = (latitude - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;

            int num1Lon = (int) Math.floor(longitude);
            int num2Lon = (int) Math.floor((longitude - num1Lon) * 60);
            double num3Lon = (longitude - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;
            String lat = num1Lat + "/1," + num2Lat + "/1," + num3Lat + "/1000";
            String lon = num1Lon + "/1," + num2Lon + "/1," + num3Lon + "/1000";

            if (geoTag.latitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (geoTag.longitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }


            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPS.convert(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPS.latitudeRef(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPS.convert(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPS.longitudeRef(longitude));

            exif.saveAttributes();
            Log.d(TAG, "geoTag: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            Log.d(TAG, "geoTag: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
            Log.d(TAG, "geoTag: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            Log.d(TAG, "geoTag: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));

        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }

    }

    public static class GPS {
        private static StringBuilder sb = new StringBuilder(20);

        /**
         * returns ref for latitude which is S or N.
         * @param latitude
         * @return S or N
         */
        public static String latitudeRef(double latitude) {
            return latitude<0.0d?"S":"N";
        }

        public static String longitudeRef(double longitude) {
            return longitude<0.0d?"W":"E";
        }

        /**
         * convert latitude into DMS (degree minute second) format. For instance<br/>
         * -79.948862 becomes<br/>
         *  79/1,56/1,55903/1000<br/>
         * It works for latitude and longitude<br/>
         * @param latitude could be longitude.
         * @return
         */
        static synchronized public final String convert(double latitude) {
            latitude=Math.abs(latitude);
            int degree = (int) latitude;
            latitude *= 60;
            latitude -= (degree * 60.0d);
            int minute = (int) latitude;
            latitude *= 60;
            latitude -= (minute * 60.0d);
            int second = (int) (latitude*1000.0d);

            sb.setLength(0);
            sb.append(degree);
            sb.append("/1,");
            sb.append(minute);
            sb.append("/1,");
            sb.append(second);
            sb.append("/1000,");
            return sb.toString();
        }
    }
}
