package nicail.bscs.com.emercify.Utils;

import android.content.ContentValues;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder>{

    private static final String TAG = "CommentRecyclerAdapter";

    private ArrayList<Comment> comments;
    private Context mContext;

    public CommentRecyclerAdapter(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment
                ,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        this.mContext = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(comments.get(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    ((ViewHolder)holder).username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    GlideApp
                            .with(mContext)
                            .load( singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo())
                            .placeholder(R.color.grey)
                            .centerCrop()
                            .into(((ViewHolder)holder).profileImage);
                    ((ViewHolder)holder).comment.setText(comments.get(position).getComment());

                    String timeStampDifference = getTimeStampDifference(comments.get(position));
                    if(!timeStampDifference.equals("0")){
                        ((ViewHolder)holder).timestamp.setText(timeStampDifference);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });

        if(position == 0){
            ((ViewHolder)holder).like.setVisibility(View.GONE);
            ((ViewHolder)holder).likes.setVisibility(View.GONE);
            ((ViewHolder)holder).reply.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;

        public ViewHolder(View itemView) {
            super(itemView);
            comment = (TextView) itemView.findViewById(R.id.comment);
            username = (TextView) itemView.findViewById(R.id.comment_username);
            timestamp = (TextView) itemView.findViewById(R.id.comment_time_posted);
            reply = (TextView) itemView.findViewById(R.id.comment_reply);
            likes = (TextView) itemView.findViewById(R.id.comment_likes);
            profileImage = (CircleImageView) itemView.findViewById(R.id.comment_profile_image);
            like = (ImageView) itemView.findViewById(R.id.comment_like);

        }
    }

    private String getTimeStampDifference(Comment comment){
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+8"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = comment.getDate_created();
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
