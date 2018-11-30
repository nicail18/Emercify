package nicail.bscs.com.emercify.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.Circle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Notifications;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class NotifListAdapter extends ArrayAdapter<Notifications> {

    public interface OnLoadMoreItemListener{
        void onLoadMoreItems();
    }
    NotifListAdapter.OnLoadMoreItemListener mOnLoadMoreItemListener;

    private static final String TAG = "NotifListAdapter";

    private LayoutInflater mLayoutInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;

    public NotifListAdapter(@NonNull Context context, int resource, @NonNull List<Notifications> objects) {
        super(context, resource, objects);

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder{
        CircleImageView profile_photo;
        TextView username,notif_message,timestamp;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        Notifications notifications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if(convertView == null){
            convertView = mLayoutInflater.inflate(mLayoutResource,parent,false);
            holder = new ViewHolder();

            holder.profile_photo = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.notif_message = (TextView) convertView.findViewById(R.id.notif_message);
            holder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);

            convertView.setTag(holder);
        }
        else{
            holder = (NotifListAdapter.ViewHolder) convertView.getTag();
        }

        String timestampDiff = getTimeStampDifference(getItem(position));
        holder.timestamp.setText(timestampDiff);

        holder.notif_message.setText(getItem(position).getMessage());

        Query query = mReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild("user_id")
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    holder.username.setText(ds.getValue(UserAccountSettings.class).getUsername());

                    GlideApp
                            .with(mContext)
                            .load(ds.getValue(UserAccountSettings.class).getProfile_photo())
                            .placeholder(R.mipmap.ic_emercify_launcher)
                            .error(R.drawable.ic_error)
                            .centerCrop()
                            .into(holder.profile_photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(reachedEndOfList(position)){
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position){
        Log.d(TAG, "reachedEndOfList: " + position);
        return position == getCount() - 1;
    }

    private void loadMoreData(){
        try{
            mOnLoadMoreItemListener = (NotifListAdapter.OnLoadMoreItemListener) getContext();
        }catch(ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException" + e.getMessage() );
        }

        try{
            mOnLoadMoreItemListener.onLoadMoreItems();
        }catch(NullPointerException e){
            Log.e(TAG, "loadMoreData: NullPointerException" + e.getMessage() );
        }
    }

    private String getTimeStampDifference(Notifications notification){
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+8"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = notification.getTimestamp();
        try{
            timestamp = sdf.parse(photoTimeStamp);
            Log.d(TAG, "getTimeStampDifference: "
                    + Math.round((today.getTime() - timestamp.getTime()) / 1000 ) + "\n"
                    + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60) + "\n"
                    + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60) + "\n"
                    + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24) + "\n");
            if(Math.round((today.getTime() - timestamp.getTime()) / 1000) > 60){
                if(Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60) > 60){
                    if((Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60) > 24)){
                        Log.d(TAG, "getTimeStampDifference: " + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24));
                        difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 / 60 / 60 / 24))) + "d";
                    }
                    else{
                        Log.d(TAG, "getTimeStampDifference: " + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60));
                        difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 / 60 / 60 ))) + "h";
                    }
                }
                else{
                    Log.d(TAG, "getTimeStampDifference: " + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60));
                    difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 / 60 ))) + "m";
                }
            }
            else{
                Log.d(TAG, "getTimeStampDifference: " + Math.round((today.getTime() - timestamp.getTime()) / 1000));
                difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 ))) + "s";
            }
            Log.d(TAG, "getTimeStampDifference: " + difference);
        }catch(ParseException e){
            Log.e(TAG, "getTimeStampDifference: ParseException" + e.getMessage());
            difference = "0";
        }
        return difference;
    }
}
