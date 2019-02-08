package nicail.bscs.com.emercify.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Responder;

public class RespondentsDialog extends DialogFragment {

    private static final String TAG = "RespondentsDialog";
    
    public RespondentsDialog() {
        super();
        setArguments(new Bundle());
    }

    RecyclerView recyclerView;
    ArrayList<Responder> responders;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_respondents, container, false);
        getDialog().setCancelable(false);

        recyclerView = (RecyclerView) view.findViewById(R.id.listViewrespondents);

        responders = getRespondersFromBundle();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private ArrayList<Responder> getRespondersFromBundle(){
        Bundle bundle = this.getArguments();
        return bundle.getParcelableArrayList("RESPONDERS");
    }
}
