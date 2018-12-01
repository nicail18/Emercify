package nicail.bscs.com.emercify.Utils;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.Home.CameraFragment;
import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.Home.HomeFragment;
import nicail.bscs.com.emercify.Likes.MapActivity;
import nicail.bscs.com.emercify.Profile.ProfileActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.dialogs.View_Delete_Dialog;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.Like;
import nicail.bscs.com.emercify.models.Notifications;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemListener mOnLoadMoreItemListener;

    private static final String TAG = "MainfeedListAdapter";

    private LayoutInflater mLayoutInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";
    private String token;
    private String likeMessage;
    private Fragment fragment;

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects,Fragment fragment) {
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
        this.fragment = fragment;
    }

    static class ViewHolder{
        FirebaseMethods firebaseMethods;

        CircleImageView mProfileImage;
        String likeString;
        TextView username, timeStamp, caption, likes, comments,address;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment, map, ellipses;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    public void openDialog(){
        View_Delete_Dialog dialog1 = new View_Delete_Dialog();
        dialog1.show(((FragmentActivity)mContext).getSupportFragmentManager(),"View_Delete_Dialog");
        dialog1.setTargetFragment(fragment,1);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(mLayoutResource,parent,false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_Link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeStamp = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.map = (ImageView) convertView.findViewById(R.id.ivMap);
            holder.ellipses = (ImageView) convertView.findViewById(R.id.ivEllipses);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ellipses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });


        holder.heart = new Heart(holder.heartWhite,holder.heartRed);
        holder.photo = getItem(position);
        holder.detector = new GestureDetector(mContext, new GestureListener(holder));
        holder.users = new StringBuilder();
        holder.firebaseMethods = new FirebaseMethods(getContext());

        getCurrentUsername();
        getLikeString(holder);
        holder.caption.setText(getItem(position).getCaption());
        holder.address.setText(getItem(position).getAddress());
        holder.address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putParcelable("PHOTO",getItem(position));
                Log.d(TAG, "onClick: " + b.getParcelable("PHOTO"));
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtras(b);
                mContext.startActivity(intent);
            }
        });
        List<Comment> comments = getItem(position).getComments();
        if(comments.size() > 0){
            holder.comments.setVisibility(View.VISIBLE);
            holder.comments.setText("View all " + comments.size() + " comments");
        }
        else{
            holder.comments.setVisibility(View.GONE);
        }
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)mContext).OnCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));

                ((HomeActivity)mContext).hideLayout();
            }
        });

        String timestampDifference = getTimeStampDifference(getItem(position));
        holder.timeStamp.setText(timestampDifference);

        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(),holder.image);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild("user_id")
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    //currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of that user");
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.mProfileImage);
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of that user");
                            Log.d(TAG, "onClick: " + holder.photo.toString());
                            Log.d(TAG, "onClick: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            if(holder.user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                mContext.startActivity(intent);
                            }else{
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra(mContext.getString(R.string.calling_activity),mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                                mContext.startActivity(intent);
                            }
                        }
                    });


                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity)mContext).OnCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));

                            ((HomeActivity)mContext).hideLayout();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild("user_id")
                .equalTo(getItem(position).getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).getUsername());
                    holder.user = singleSnapshot.getValue(User.class);
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
            mOnLoadMoreItemListener = (OnLoadMoreItemListener) getContext();
        }catch(ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException" + e.getMessage() );
        }

        try{
            mOnLoadMoreItemListener.onLoadMoreItems();
        }catch(NullPointerException e){
            Log.e(TAG, "loadMoreData: NullPointerException" + e.getMessage() );
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected");
            Log.d(TAG, "onDoubleTap: " + mHolder.photo.toString());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child("likes");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        String keyID = singleSnapshot.getKey();
                        Log.d(TAG, "onDataChange: " + mHolder.photo.toString());
                        if(mHolder.likedByCurrentUser && singleSnapshot.getValue(Like.class)
                                .getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                             mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();


                            mHolder.heart.toggleLike();
                            getLikeString(mHolder);
                        }
                        else if(!mHolder.likedByCurrentUser){
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(final ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child("likes")
                .child(newLikeID)
                .setValue(like);
        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child("likes")
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikeString(holder);
        Log.d(TAG, "addNewLike: " + holder.photo.getUser_id());
        Log.d(TAG, "addNewLike: " + holder.photo.toString());
        Log.d(TAG, "addNewLike: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(!holder.photo.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            String user_id = holder.photo.getUser_id();
            String from_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String type = "like";
            token = holder.settings.getDevice_token();
            likeMessage = currentUsername + " liked your post \"" + holder.photo.getCaption() + "\"";
            holder.firebaseMethods.addNotification(user_id,from_id,type,likeMessage);
            new Notify(token,likeMessage).execute();
        }
    }

    private void getLikeString(final ViewHolder holder){
        Log.d(TAG, "getLikeString: getting likes string ");
        try {
            final int[] count = {0};
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child("likes");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: likes " + singleSnapshot.getValue(Like.class).toString());
                        Log.d(TAG, "onDataChange: count " + count[0]);
                        Log.d(TAG, "onDataChange: " + holder.photo.toString());
                        count[0]++;
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild("user_id")
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");

                                if (holder.users.toString().contains(currentUsername + ",")) {
                                    holder.likedByCurrentUser = true;
                                } else {
                                    holder.likedByCurrentUser = false;

                                }

                                int length = splitUsers.length;
                                if (length == 1) {
                                    holder.likeString = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    holder.likeString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                                } else if (length > 2) {
                                    holder.likeString = "Liked by " + splitUsers[0] + " and " + (splitUsers.length - 1) + " others";
                                }
                                setupLikesString(holder,holder.likeString);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        holder.likeString = "";
                        holder.likedByCurrentUser = false;
                        setupLikesString(holder,holder.likeString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch(NullPointerException e){
            Log.e(TAG, "getLikeString: NullPointerException" + e.getMessage() );
            holder.likeString = "";
            holder.likedByCurrentUser = false;
        }
    }

    private void setupLikesString(final ViewHolder holder, String likesString){
        Log.d(TAG, "setupLikesString: likes string " + holder.likes);
        if(holder.likedByCurrentUser){
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        else{
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        if(holder.mLikesString == ""){
            holder.likes.setVisibility(View.GONE);
        }
        else{
            holder.likes.setVisibility(View.VISIBLE);
            holder.likes.setText(likesString);
        }
    }

    private String getTimeStampDifference(Photo photo){
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+8"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = photo.getDate_created();
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

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild("user_id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
