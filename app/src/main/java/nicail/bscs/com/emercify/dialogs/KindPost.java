package nicail.bscs.com.emercify.dialogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nicail.bscs.com.emercify.R;

public class KindPost extends DialogFragment {

    private static final String TAG = "KindPost";

    public interface OnNormalClickListener{
        void onNormalClickListener(String image, Bitmap bitmap,double latitude,double longitude, String imageAddress);
    }

    public interface OnEmergencyClickListener{
        void onEmergencyClickListener(String image, Bitmap bitmap,double latitude,double longitude, String imageAddress);
    }

    OnNormalClickListener onNormalClickListener;
    OnEmergencyClickListener onEmergencyClickListener;

    public KindPost() {
        super();
        setArguments(new Bundle());
    }

    String image,imageAddress;
    Bitmap bitmap;
    static double latitude;
    static double longitude;

    TextView normal_post;
    TextView report_post;
    TextView emergency_post;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_post, container, false);
        normal_post = (TextView) view.findViewById(R.id.normal_post);

        emergency_post = (TextView) view.findViewById(R.id.emergency_post);

        image = getImageFromBundle();
        bitmap = getBitmapFromBundle();
        latitude = getLatitudeFromBundle();
        longitude = getLongitudeFromBundle();
        imageAddress = getAddressFromBundle();

        normal_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNormalClickListener.onNormalClickListener(image,bitmap,latitude,longitude,imageAddress);
                getDialog().dismiss();
            }
        });

        emergency_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEmergencyClickListener.onEmergencyClickListener(image,bitmap,latitude,longitude,imageAddress);
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            onNormalClickListener = (OnNormalClickListener) getTargetFragment();
            onEmergencyClickListener = (OnEmergencyClickListener) getTargetFragment();
        }catch(ClassCastException e){
            Log.e(TAG, "onAttach: " + e.getMessage() );
        }
    }

    public String getAddressFromBundle(){
        Bundle b = this.getArguments();
        return b.getString(getString(R.string.image_address));
    }

    public String getImageFromBundle(){
        Bundle b = this.getArguments();
        return b.getString(getString(R.string.selected_image));
    }

    public Bitmap getBitmapFromBundle(){
        Bundle b = this.getArguments();
        return b.getParcelable(getString(R.string.selected_bitmap));
    }

    public double getLatitudeFromBundle(){
        Bundle b = this.getArguments();
        return b.getDouble(getString(R.string.image_latitude));
    }

    public double getLongitudeFromBundle(){
        Bundle b = this.getArguments();
        return b.getDouble(getString(R.string.image_longitude));
    }
}
