package nicail.bscs.com.emercify.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nicail.bscs.com.emercify.R;

public class dialog_lochoose extends AppCompatDialogFragment {
    private static final String TAG = "dialog_lochoose";

    @Override
    public Dialog onCreateDialog(Bundle savedInstaceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_chooseloc,null);


        return builder.create();
    }
}
