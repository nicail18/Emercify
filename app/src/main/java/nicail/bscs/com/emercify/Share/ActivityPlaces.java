package nicail.bscs.com.emercify.Share;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nicail.bscs.com.emercify.R;

public class ActivityPlaces extends AppCompatActivity {

    private EditText etPlaces;

    private static final String TAG = "ActivityPlaces";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        etPlaces = (EditText) findViewById(R.id.etPlaces);

        init();
    }

    private void init(){
        Log.d(TAG, "init: initializing");
        etPlaces.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    geolocate();
                }
                return false;
            }
        });
    }

    private void geolocate(){
        Log.d(TAG, "geolocate: geolocating");

        String searchString = etPlaces.getText().toString();
        Geocoder geocoder = new Geocoder(ActivityPlaces.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString,1);
        }catch(IOException e){
            Log.e(TAG, "geolocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG, "geolocate: found location: " + address.toString() );
        }
    }
}
