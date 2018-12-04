package nicail.bscs.com.emercify.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nicail.bscs.com.emercify.R;

public class KindPost extends DialogFragment {
    private static final String TAG = "kindpost";
    TextView normal_post;
    TextView report_post;
    TextView emergency_post;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_post, container, false);
        normal_post = (TextView) view.findViewById(R.id.normal_post);
        report_post = (TextView) view.findViewById(R.id.reports_post);
        emergency_post = (TextView) view.findViewById(R.id.emergency_post);
        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }
}
