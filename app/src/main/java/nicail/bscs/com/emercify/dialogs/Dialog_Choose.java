package nicail.bscs.com.emercify.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nicail.bscs.com.emercify.R;

public class Dialog_Choose extends DialogFragment {
    private static final String TAG = "Dialog_Choose";

    public interface OnCurrentLocationClickListener{
        void onCurrentLocationClickLsitener(String type, String image);
    }

    public interface OnSearchLocationClickListener{
        void onSearchLocationClickListener(String type,String image);
    }

    OnCurrentLocationClickListener onCurrentLocationClickListener;
    OnSearchLocationClickListener onSearchLocationClickListener;

    public Dialog_Choose() {
        super();
        setArguments(new Bundle());
    }

    TextView current_location;
    TextView search_location;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chooseloc,container,false);
        current_location = (TextView) view.findViewById(R.id.current_location);
        search_location = (TextView) view.findViewById(R.id.search_location);

        current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCurrentLocationClickListener.onCurrentLocationClickLsitener(getTypeFromBundle(),getImageFromBundle());
                getDialog().dismiss();
            }
        });

        search_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchLocationClickListener.onSearchLocationClickListener(getTypeFromBundle(),getImageFromBundle());
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onCurrentLocationClickListener = (OnCurrentLocationClickListener) getTargetFragment();
        onSearchLocationClickListener = (OnSearchLocationClickListener) getTargetFragment();
    }

    public String getTypeFromBundle(){
        Bundle b = this.getArguments();
        return b.getString("type");
    }

    public String getImageFromBundle(){
        Bundle b = this.getArguments();
        return b.getString("image");
    }
}
