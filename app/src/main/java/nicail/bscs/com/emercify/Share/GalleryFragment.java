package nicail.bscs.com.emercify.Share;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nicail.bscs.com.emercify.Profile.AccountSettingsActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.FilePaths;
import nicail.bscs.com.emercify.Utils.FileSearch;
import nicail.bscs.com.emercify.Utils.GridImageAdapter;
import nicail.bscs.com.emercify.Utils.ViewWeightAnimationWrapper;
import nicail.bscs.com.emercify.dialogs.KindPost;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private String mAppend = "file:/";

    private GridView gridView;
    private RelativeLayout layoutTop;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private ImageButton imageButton;
    private ArrayList<String> directories;
    private String mSelectedImage;
    private String mImageAddress;
    private double latitude, longitude;

    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        layoutTop = (RelativeLayout) view.findViewById(R.id.rellayouttop);
        galleryImage = (ImageView) view.findViewById(R.id.galleryImageView);
        imageButton = (ImageButton) view.findViewById(R.id.hide_show_btn);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        directories = new ArrayList<>();
        Log.d(TAG, "onCreateView: started");


        ImageView shareClose = (ImageView) view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing gallery fragment");
                getActivity().finish();

            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.hide_show_btn:{

                        if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
                            mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                            expandMapAnimation();
                        }
                        else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
                            mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                            contractMapAnimation();
                        }
                        break;
                    }

                }

            }
        });
        TextView nextScreen = (TextView) view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to final share screen");
                if(isRootTask()){
                    /*Intent intent = new Intent(getActivity(),NextActivity.class);
                    Bundle b = new Bundle();
                    b.putDouble(getString(R.string.image_latitude),latitude);
                    b.putDouble(getString(R.string.image_longitude),longitude);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    intent.putExtra(getString(R.string.image_address),mImageAddress);
                    intent.putExtras(b);
                    startActivity(intent);*/
                    openDialog();
                }
                else{
                    Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                    Bundle b = new Bundle();
                    b.putDouble(getString(R.string.image_latitude),latitude);
                    b.putDouble(getString(R.string.image_longitude),longitude);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    intent.putExtra(getString(R.string.image_address),mImageAddress);
                    intent.putExtras(b);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        init();

        return view;
    }

    public void openDialog(){
        KindPost kindPost = new KindPost();
        Bundle b = new Bundle();
        b.putDouble(getString(R.string.image_latitude),latitude);
        b.putDouble(getString(R.string.image_longitude),longitude);
        b.putString(getString(R.string.selected_image),mSelectedImage);
        b.putString(getString(R.string.image_address),mImageAddress);
        kindPost.setArguments(b);
        kindPost.show(((FragmentActivity)getContext()).getSupportFragmentManager(),"KindPost");
        kindPost.setTargetFragment(this,1);
    }

    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }
        else{
            return false;
        }
    }

    private void init(){
        FilePaths filePaths = new FilePaths();
        ArrayList<String> resultIAV = new ArrayList<String>();
        resultIAV = filePaths.getFilePaths(getActivity());
        if(FileSearch.getDirectoryPaths(filePaths.PICTURES) != null){
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        directories.add(filePaths.CAMERA);
        ArrayList<String> directoryNames = new ArrayList<>();
        for(int i = 0;i < directories.size(); i++){
            int index = directories.get(i).lastIndexOf("/")+1;
            String string = directories.get(i).substring(index);
            directoryNames.add(string);
        }

        setupGridView(directories.get(0),resultIAV);
    }

    private void setupGridView(String selectedDirectory,ArrayList<String> resultIAV){
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = resultIAV;

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/3;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,mAppend,imgURLs);
        gridView.setAdapter(adapter);

        setImage(imgURLs.get(0),galleryImage,mAppend);
        imageButton.setVisibility(View.VISIBLE);
        mSelectedImage = imgURLs.get(0);
        try {
            ExifInterface exif = new ExifInterface(mSelectedImage);
            if(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null){
                String attrLatRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String attrLat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String attrLonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                String attrLon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                if(attrLatRef.equals("N")){
                    latitude = convertToDegree(attrLat);
                }
                else{
                    latitude = 0 - convertToDegree(attrLat);
                }

                if(attrLonRef.equals("E")){
                    longitude = convertToDegree(attrLon);
                }
                else{
                    longitude = 0 - convertToDegree(attrLon);
                }

                mImageAddress = getCompleteAddressString(latitude,longitude);

                Log.d(TAG, "onItemClick: address " + mImageAddress);

                Log.d(TAG, "onItemClick: latitude " + attrLatRef + " " + latitude);
                Log.d(TAG, "onItemClick: longitude " + attrLonRef + " " + longitude);
            }
//            else{
//                Toast.makeText(getActivity(), "Your Photo Don't Have a Tagged Location" + "\n" +
//                        "Please enable location tag in your camera", Toast.LENGTH_LONG).show();
//                getActivity().finish();
//            }
        } catch (IOException e) {
            Log.e(TAG, "onItemClick: " + e.getMessage());
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected image " + imgURLs.get(position));

                setImage(imgURLs.get(position),galleryImage,mAppend);
                imageButton.setVisibility(View.VISIBLE);
                mSelectedImage = imgURLs.get(position);
                try {
                    ExifInterface exif = new ExifInterface(mSelectedImage);
                    if(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null){
                        String attrLatRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                        String attrLat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String attrLonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                        String attrLon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        if(attrLatRef.equals("N")){
                            latitude = convertToDegree(attrLat);
                        }
                        else{
                            latitude = 0 - convertToDegree(attrLat);
                        }

                        if(attrLonRef.equals("E")){
                            longitude = convertToDegree(attrLon);
                        }
                        else{
                            longitude = 0 - convertToDegree(attrLon);
                        }

                        mImageAddress = getCompleteAddressString(latitude,longitude);


                        Log.d(TAG, "onItemClick: address " + mImageAddress);

                        Log.d(TAG, "onItemClick: latitude " + attrLatRef + " " + latitude);
                        Log.d(TAG, "onItemClick: longitude " + attrLonRef + " " + longitude);
                    }
//                    else{
//                        Toast.makeText(getActivity(), "Your Photo Don't Have a Tagged Location" + "\n" +
//                                "Please enable location tag in your camera", Toast.LENGTH_LONG).show();
//                        getActivity().finish();
//                    }
                } catch (IOException e) {
                    Log.e(TAG, "onItemClick: " + e.getMessage());
                }
            }
        });
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

    private Double convertToDegree(String stringDMS){
        double result = 0;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;


        result = new Double(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    };

    private void setImage(String imgURL, ImageView image, String append){
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(gridView);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                40,
                90);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(layoutTop);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                60,
                10);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(gridView);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                90,
                40);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(layoutTop);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                10,
                60);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }


}
