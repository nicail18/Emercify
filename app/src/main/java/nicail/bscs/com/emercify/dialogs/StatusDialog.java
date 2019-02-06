package nicail.bscs.com.emercify.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import nicail.bscs.com.emercify.R;

public class StatusDialog extends Dialog {

    private static final String TAG = "StatusDialog";
    private Context context;


    public StatusDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public interface OnConfirmListener{
        public void onConfirm(String status);
    }
    OnConfirmListener onConfirmListener;

    public void setOnComfirmListener(OnConfirmListener listener){
        onConfirmListener = listener;
    }

    TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirm_password);

        mStatus = (TextView) findViewById(R.id.confirm_password);

        Log.d(TAG, "onCreateView: started");

        TextView cancelDialog = (TextView) findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the dialog");
                dismiss();
            }
        });

        TextView confirmDialog = (TextView) findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: captured password and confirming");
                String status = mStatus.getText().toString();
                if(!status.equals("")){
                    onConfirmListener.onConfirm(status);
                    dismiss();
                }
                else{
                    Toast.makeText(context, "You must Enter A Status", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
