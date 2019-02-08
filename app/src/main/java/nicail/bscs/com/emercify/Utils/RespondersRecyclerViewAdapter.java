package nicail.bscs.com.emercify.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Responder;

public class RespondersRecyclerViewAdapter extends RecyclerView.Adapter<RespondersRecyclerViewAdapter.ViewHolder> {


    private static final String TAG = "RespondersRecyclerViewA";

    private ArrayList<Responder> responders;
    private Context context;

    public RespondersRecyclerViewAdapter(ArrayList<Responder> responders) {
        this.responders = responders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment
                ,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        this.context = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView username, status;
        CircleImageView profile_photo;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            status = itemView.findViewById(R.id.status);
            profile_photo = itemView.findViewById(R.id.profile_photo);
        }
    }
}
