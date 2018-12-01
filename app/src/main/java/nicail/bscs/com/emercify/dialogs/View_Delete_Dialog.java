package nicail.bscs.com.emercify.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;;import nicail.bscs.com.emercify.R;

public class View_Delete_Dialog extends DialogFragment {
    private static final String TAG = "View_Delete_Dialog";
    TextView mView;
    TextView mDelete;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_delete_dialog, container, false);
        mView = (TextView) view.findViewById(R.id.view_dialog);
        mDelete = (TextView) view.findViewById(R.id.delete_dialog);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }
}
