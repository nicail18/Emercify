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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Photo;

public class View_Delete_Dialog extends DialogFragment {
    private static final String TAG = "View_Delete_Dialog";

    public interface OnViewClickListener{
        public void onViewClickListener(Photo photo);
    }

    public interface OnDeleteClickListener{
        public void onDeleteClickListener(Photo photo,int position);

    }

    public interface OnReportClickListener{
        void onReportClickListener(Photo photo);
    }
    OnViewClickListener mOnViewClickListener;
    OnDeleteClickListener mOnDeleteClickListener;
    OnReportClickListener mOnReportClickListener;

    public View_Delete_Dialog() {
        super();
        setArguments(new Bundle());
    };

    Photo photo;
    TextView mView;
    TextView mDelete;
    TextView mReport;
    ImageView ic_report, ic_delete;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_delete_dialog, container, false);
        mView = (TextView) view.findViewById(R.id.view_text);
        mDelete = (TextView) view.findViewById(R.id.delete_text);
        mReport = (TextView) view.findViewById(R.id.report_text);
        ic_report = (ImageView) view.findViewById(R.id.ic_report);
        ic_delete = (ImageView) view.findViewById(R.id.ic_delete);
        photo = getPhotoFromBundle();

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnViewClickListener.onViewClickListener(photo);
                getDialog().dismiss();
            }
        });
        if(photo.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnDeleteClickListener.onDeleteClickListener(photo,getPositionFromBundle());
                    getDialog().dismiss();
                }
            });
            ic_report.setVisibility(View.GONE);
            mReport.setVisibility(View.GONE);
        }
        else{
            mDelete.setVisibility(View.GONE);
            ic_delete.setVisibility(View.GONE);
            mReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnReportClickListener.onReportClickListener(photo);
                    getDialog().dismiss();
                }
            });
        }

        return view;
    }

    public Photo getPhotoFromBundle(){
        Bundle bundle = this.getArguments();
        return bundle.getParcelable("PHOTO");
    }

    public int getPositionFromBundle(){
        Bundle bundle = this.getArguments();
        return bundle.getInt("position");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mOnViewClickListener = (OnViewClickListener) getTargetFragment();
            mOnDeleteClickListener = (OnDeleteClickListener) getTargetFragment();
            mOnReportClickListener = (OnReportClickListener) getTargetFragment();
        }catch(ClassCastException e){
            Log.d(TAG, "onAttach: " + e.getMessage());
        }
    }
}
