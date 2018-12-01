package nicail.bscs.com.emercify.Home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.dialogs.View_Delete_Dialog;

public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";
    Dialog MyDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera,container,false);
        ImageButton mEllipses = (ImageButton) view.findViewById(R.id.ellipses_dialog);

        mEllipses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        return view;
    }
    public void openDialog() {
        View_Delete_Dialog dialog1 = new View_Delete_Dialog();
        dialog1.show(getFragmentManager(),"View_Delete_Dialog");
        dialog1.setTargetFragment(CameraFragment.this,1);

    }
}
