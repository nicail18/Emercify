package nicail.bscs.com.emercify.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Responder;
import nicail.bscs.com.emercify.models.UserAccountSettings;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_respondent_recycleritem,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        this.context = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + responders.get(position));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(context.getString(R.string.dbname_user_account_settings))
                .orderByChild(context.getString(R.string.field_user_id))
                .equalTo(responders.get(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    GlideApp
                            .with(context.getApplicationContext())
                            .load(ds.getValue(UserAccountSettings.class).getProfile_photo())
                            .placeholder(R.color.grey)
                            .centerCrop()
                            .into(((ViewHolder)holder).profile_photo);

                    ((ViewHolder)holder).username.setText(ds.getValue(UserAccountSettings.class).getUsername());

                    if(responders.get(position).getStatus()!= null || responders.get(position).getStatus() != ""){
                        ((ViewHolder)holder).status.setText(responders.get(position).getStatus());
                    }
                    else{
                        ((ViewHolder)holder).status.setText("STATUS: NONE");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return responders.size();
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
