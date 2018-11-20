package nicail.bscs.com.emercify.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Message;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class MessageListAdapter extends ArrayAdapter<Message> {

    private static final String TAG = "MessageListAdapter";

    private LayoutInflater mLayoutInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;

    public MessageListAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder{
        CircleImageView  profile_photo;
        TextView username, tvMessage, timestamp;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        Message message;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if(convertView == null){
            convertView = mLayoutInflater.inflate(mLayoutResource,parent,false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
            holder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            holder.profile_photo = (CircleImageView) convertView.findViewById(R.id.profile_photo);

            convertView.setTag(holder);
        }
        else{
            holder = (MessageListAdapter.ViewHolder) convertView.getTag();
        }

        holder.timestamp.setText(getItem(position).getDate_send());
        holder.tvMessage.setText(getItem(position).getMessage());

        final ImageLoader imageLoader = ImageLoader.getInstance();
        Query query = mReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild("user_id")
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.profile_photo);

                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return convertView;
    }
}
