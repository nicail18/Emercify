package nicail.bscs.com.emercify.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import nicail.bscs.com.emercify.R;

public class SignOutDialog extends Dialog {
    private static final String TAG = "SignOutDialog";


    Button btnConfirmPassword;

    public SignOutDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signout);
        Log.d(TAG, "onCreateView: started");

        Button cancelDialog = (Button) findViewById(R.id.btnConfirmSignout);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Signing Out");
                dismiss();
            }
        });
    }

}
