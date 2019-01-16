package nicail.bscs.com.emercify.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.Likes.MapActivity;
import nicail.bscs.com.emercify.Profile.ProfileActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.dialogs.View_Delete_Dialog;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.Like;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class MainfeedRecyclerAdapter extends RecyclerView.Adapter<MainfeedRecyclerAdapter.ViewHolder>{

    private static final String TAG = "MainfeedRecyclerAdapter";

    private ArrayList<Photo> photos;
    private Fragment fragment;
    private Context mContext;
    private FirebaseMethods firebaseMethods;
    private DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    private String likeString;
    private String currentUsername, token,likeMessage;

    public MainfeedRecyclerAdapter(ArrayList<Photo> photo, Fragment fragment) {
        this.photos = photo;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mainfeed_listitem
                ,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        this.mContext = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        ((ViewHolder)holder).ellipses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(photos.get(position),position);
            }
        });
        ((ViewHolder)holder).photo = photos.get(position);

        ((ViewHolder)holder).heart = new Heart(holder.heartWhite,holder.heartRed);
        ((ViewHolder)holder).detector = new GestureDetector(mContext,
                new GestureListener((ViewHolder)holder));
        firebaseMethods = new FirebaseMethods(mContext);



        getCurrentUsername();
        getLikeString(((ViewHolder)holder));
        ((ViewHolder)holder).caption.setText(((ViewHolder)holder).photo.getCaption());
        ((ViewHolder)holder).address.setText(((ViewHolder)holder).photo.getAddress());
        ((ViewHolder)holder).address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putParcelable("PHOTO",photos.get(position));
                Log.d(TAG, "onClick: " + b.getParcelable("PHOTO"));
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtras(b);
                mContext.startActivity(intent);
            }
        });
        List<Comment> comments = photos.get(position).getComments();
        if(comments.size() > 0){
            ((ViewHolder)holder).comments.setVisibility(View.VISIBLE);
            ((ViewHolder)holder).comments.setText("View all " + comments.size() + " comments");
        }
        else{
            ((ViewHolder)holder).comments.setVisibility(View.GONE);
        }
        ((ViewHolder)holder).comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)mContext).OnCommentThreadSelected(photos.get(position), mContext.getString(R.string.home_activity));

                ((HomeActivity)mContext).hideLayout();
            }
        });

        String timestampDifference = getTimeStampDifference(photos.get(position));
        ((ViewHolder)holder).timeStamp.setText(timestampDifference);

        final ImageLoader imageLoader = ImageLoader.getInstance();
        //imageLoader.displayImage(photos.get(position).getImage_path(),holder.image);
        Log.d(TAG, "onBindViewHolder: " +photos.get(position).getImage_path());
        GlideApp
                .with(mContext)
                .load(photos.get(position).getImage_path())
                .placeholder(R.color.grey)
                .centerCrop()

                .into(((ViewHolder)holder).image);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild("user_id")
                .equalTo(photos.get(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    //currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    ((ViewHolder)holder).username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    ((ViewHolder)holder).username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of that user");
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),((ViewHolder)holder).user);
                            mContext.startActivity(intent);
                        }
                    });
                    GlideApp
                            .with(mContext)
                            .load(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo())
                            .placeholder(R.color.grey)
                            .centerCrop()
                            .into(((ViewHolder)holder).mProfileImage);

                    //imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.mProfileImage);
                    ((ViewHolder)holder).mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of that user");
                            Log.d(TAG, "onClick: " + ((ViewHolder)holder).photo.toString());
                            Log.d(TAG, "onClick: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            if(((ViewHolder)holder).user.getUser_id()
                                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                mContext.startActivity(intent);
                            }else{
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra(mContext.getString(R.string.calling_activity),mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user),((ViewHolder)holder).user);
                                mContext.startActivity(intent);
                            }
                        }
                    });


                    ((ViewHolder)holder).settings = singleSnapshot.getValue(UserAccountSettings.class);
                    ((ViewHolder)holder).comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity)mContext)
                                    .OnCommentThreadSelected(photos.get(position),
                                            mContext.getString(R.string.home_activity));

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
                .equalTo(photos.get(position).getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).getUsername());
                    ((ViewHolder)holder).user = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mProfileImage;
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

        public ViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            image = (SquareImageView) itemView.findViewById(R.id.post_image);
            heartRed = (ImageView) itemView.findViewById(R.id.image_heart_red);
            heartWhite = (ImageView) itemView.findViewById(R.id.image_heart);
            comment = (ImageView) itemView.findViewById(R.id.speech_bubble);
            likes = (TextView) itemView.findViewById(R.id.image_likes);
            comments = (TextView) itemView.findViewById(R.id.image_comments_Link);
            caption = (TextView) itemView.findViewById(R.id.image_caption);
            timeStamp = (TextView) itemView.findViewById(R.id.image_time_posted);
            mProfileImage = (CircleImageView) itemView.findViewById(R.id.profile_photo);
            address = (TextView) itemView.findViewById(R.id.address);
            map = (ImageView) itemView.findViewById(R.id.ivMap);
            ellipses = (ImageView) itemView.findViewById(R.id.ivEllipses);
        }
    }

    public void openDialog(Photo photo, int position){
        View_Delete_Dialog viewDeleteDialog = new View_Delete_Dialog();
        Bundle args = new Bundle();
        args.putParcelable("PHOTO",photo);
        args.putInt("position",position);
        viewDeleteDialog.setArguments(args);
        viewDeleteDialog.show(((FragmentActivity)mContext).getSupportFragmentManager(),"View_Delete_Dialog");
        viewDeleteDialog.setTargetFragment(fragment,1);
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
            Log.d(TAG, "onDoubleTap: " + ((ViewHolder)mHolder).photo.toString());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(((ViewHolder)mHolder).photo.getPhoto_id())
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
                                    .child(((ViewHolder)mHolder).photo.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(((ViewHolder)mHolder).photo.getUser_id())
                                    .child(((ViewHolder)mHolder).photo.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();


                            mHolder.heart.toggleLike();
                            getLikeString(((ViewHolder)mHolder));
                        }
                        else if(!mHolder.likedByCurrentUser){
                            addNewLike(((ViewHolder)mHolder));
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        addNewLike(((ViewHolder)mHolder));
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
                .child(((ViewHolder)holder).photo.getPhoto_id())
                .child("likes")
                .child(newLikeID)
                .setValue(like);
        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(((ViewHolder)holder).photo.getUser_id())
                .child(((ViewHolder)holder).photo.getPhoto_id())
                .child("likes")
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikeString(holder);
        Log.d(TAG, "addNewLike: " + ((ViewHolder)holder).photo.getUser_id());
        Log.d(TAG, "addNewLike: " + ((ViewHolder)holder).photo.toString());
        Log.d(TAG, "addNewLike: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(!holder.photo.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            String user_id = ((ViewHolder)holder).photo.getUser_id();
            String from_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String type = "like";
            token = holder.settings.getDevice_token();
            likeMessage = currentUsername + " liked your post \"" + ((ViewHolder)holder).photo.getCaption() + "\"";
            firebaseMethods.addNotification(user_id,from_id,type,likeMessage,holder.photo.getPhoto_id());
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
                    .child(((ViewHolder)holder).photo.getPhoto_id())
                    .child("likes");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ((ViewHolder)holder).users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: likes " + singleSnapshot.getValue(Like.class).toString());
                        Log.d(TAG, "onDataChange: count " + count[0]);
                        Log.d(TAG, "onDataChange: " + ((ViewHolder)holder).photo.toString());
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
                                    ((ViewHolder)holder).users.append(singleSnapshot.getValue(User.class).getUsername());
                                    ((ViewHolder)holder).users.append(",");
                                }

                                String[] splitUsers = ((ViewHolder)holder).users.toString().split(",");

                                if (((ViewHolder)holder).users.toString().contains(currentUsername + ",")) {
                                    ((ViewHolder)holder).likedByCurrentUser = true;
                                } else {
                                    ((ViewHolder)holder).likedByCurrentUser = false;

                                }

                                int length = splitUsers.length;
                                if (length == 1) {
                                    likeString = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    likeString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                                } else if (length > 2) {
                                    likeString = "Liked by " + splitUsers[0] + " and " + (splitUsers.length - 1) + " others";
                                }
                                setupLikesString(((ViewHolder)holder),likeString);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        likeString = "";
                        ((ViewHolder)holder).likedByCurrentUser = false;
                        setupLikesString(((ViewHolder)holder),likeString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch(NullPointerException e){
            Log.e(TAG, "getLikeString: NullPointerException" + e.getMessage() );
            likeString = "";
            ((ViewHolder)holder).likedByCurrentUser = false;
        }
    }

    private void setupLikesString(final ViewHolder holder, String likesString){
        Log.d(TAG, "setupLikesString: likes string " + holder.likes);
        if(((ViewHolder)holder).likedByCurrentUser){
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            ((ViewHolder)holder).heartWhite.setVisibility(View.GONE);
            ((ViewHolder)holder).heartRed.setVisibility(View.VISIBLE);
            ((ViewHolder)holder).heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        else{
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            ((ViewHolder)holder).heartWhite.setVisibility(View.VISIBLE);
            ((ViewHolder)holder).heartRed.setVisibility(View.GONE);
            ((ViewHolder)holder).heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return ((ViewHolder)holder).detector.onTouchEvent(event);
                }
            });
        }
        if(likesString.equals("")){
            ((ViewHolder)holder).likes.setVisibility(View.GONE);
        }
        else{
            ((ViewHolder)holder).likes.setVisibility(View.VISIBLE);
            ((ViewHolder)holder).likes.setText(likesString);
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
