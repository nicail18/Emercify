package nicail.bscs.com.emercify.Utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.Likes.LikesActivity;
import nicail.bscs.com.emercify.Likes.MapActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.dialogs.RespondentsDialog;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.Like;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.Responder;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public interface OnCommentThreadSelectedListener{
        void onCommentThreadSelecetedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    public ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }

    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel,mCaption,mUsername,mTimeStamp, mLikes, mComments, mAddress, fakeCount, legitCount;
    private ImageView mBackArrow, respondents, mHeartRed, mHeartWhite, mProfileImage, mComment, emergencyIcon;

    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername = "", photoUrl = "";
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikeString = "";
    private User mCurrentUser;
    private String token, likeMessage;
    private RelativeLayout rellayout2;
    private ProgressBar viewpost1;
    private Context mContext;
    private Button respondButton,fakeButton,legitButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private ProgressDialog progressDialog;

    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;

    private ArrayList<Responder> responders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post,container,false);
        this.inflater = inflater;
        this.container = container;
        this.savedInstanceState = savedInstanceState;
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimeStamp = (TextView) view.findViewById(R.id.image_time_posted);
        respondents = (ImageView) view.findViewById(R.id.respondents);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);
        mLikes = (TextView) view.findViewById(R.id.image_likes);
        mComment = (ImageView) view.findViewById(R.id.speech_bubble);
        mComments = (TextView) view.findViewById(R.id.image_comments_Link);
        mAddress = (TextView) view.findViewById(R.id.address);
        rellayout2 = (RelativeLayout) view.findViewById(R.id.rellayout2);
        respondButton = (Button) view.findViewById(R.id.respondButton);
        fakeButton = (Button) view.findViewById(R.id.fakeButton);
        legitButton = (Button) view.findViewById(R.id.legitButton);
        viewpost1 = (ProgressBar) view.findViewById(R.id.progress_Barviewpost);
        emergencyIcon = (ImageView) view.findViewById(R.id.emergency_icon);
        fakeCount = (TextView) view.findViewById(R.id.fakecount);
        legitCount = (TextView) view.findViewById(R.id.legitcount);
        mContext = getActivity();
        responders = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());

        mFirebaseMethods = new FirebaseMethods(getActivity());

        mHeart = new Heart(mHeartWhite,mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        new Task().execute();
        setupFireBaseAuth();
        setupBottomNavigationView();

        return view;
    }

    class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            viewpost1.setVisibility(View.VISIBLE);
            rellayout2.setVisibility(View.GONE);
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean result) {
            viewpost1.setVisibility(View.GONE);
            rellayout2.setVisibility(View.VISIBLE);
            super.onPostExecute(result);
        }
        @Override
        protected Boolean doInBackground(String... params) {

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void init(){
        try{
            respondButton.setVisibility(View.GONE);
            fakeButton.setVisibility(View.GONE);
            legitButton.setVisibility(View.GONE);
            mPhoto = getPhotoFromBundle();
            GlideApp
                    .with(mContext.getApplicationContext())
                    .load(getPhotoFromBundle().getImage_path())
                    .placeholder(R.color.grey)
                    .centerCrop()
                    .into(mPostImage);
            mActivityNumber = getActivityNumFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();
            if(getEmergencyFromBundle() != null){
                emergencyIcon.setVisibility(View.VISIBLE);
                fakeCount.setVisibility(View.VISIBLE);
                legitCount.setVisibility(View.VISIBLE);
                respondents.setVisibility(View.VISIBLE);
                new InitWeb3j(null,mPhoto.getPhoto_id(),null,false,true).execute(getActivity().getString(R.string.infura));
                Query query = myRef
                        .child(getActivity().getString(R.string.dbname_photos))
                        .child(mPhoto.getPhoto_id())
                        .child("responder");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean respondedByUser = false;
                        boolean legitFound = false;
                        String responder_id = "";
                        String status = "";
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Log.d(TAG, "onDataChange: " + ds.child("user_id").getValue().toString());
                            Log.d(TAG, "onDataChange: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            if(ds.getValue(Responder.class).getUser_id()
                                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                respondedByUser = true;
                                responder_id = ds.child("responder_id").getValue().toString();
                                if(ds.child("status").getValue() != null){
                                    status = ds.child("status").getValue().toString();
                                }
                                Log.d(TAG, "onDataChange: " + true);
                                if(ds.child("legit").getValue() != null){
                                    Log.d(TAG, "onDataChange: legit");
                                    legitFound = true;
                                    break;
                                }
                                else{
                                    break;
                                }
                            }
                        }
                        if(!dataSnapshot.exists() || !respondedByUser){
                            respondButton.setVisibility(View.VISIBLE);
                            respondButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Are you sure you want to respond to the photo's location?")
                                            .setCancelable(false)
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(getActivity(), MapActivity.class);
                                                    intent.putExtra(getString(R.string.calling_activity),"Likes Activity");
                                                    intent.putExtra("INTENT PHOTO",getPhotoFromBundle());
                                                    Log.d(TAG, "onDataChange: " + intent);
                                                    String message = mCurrentUser.getUsername() +
                                                            " is responding to your emergency post";
                                                    String token = mUserAccountSettings.getDevice_token();
                                                    new Notify(token,message).execute();
                                                    startActivity(intent);
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });
                        }
                        else{
                            if(!legitFound){
                                fakeButton.setVisibility(View.VISIBLE);
                                legitButton.setVisibility(View.VISIBLE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                String finalResponder_id = responder_id;
                                String finalStatus = status;
                                legitButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        builder.setMessage("Do you still want to mark this post as verified?");
                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mFirebaseMethods.updateResponder(mPhoto, finalResponder_id,
                                                        true, finalStatus);
                                                new InitWeb3j(mPhoto.getUser_id(),mPhoto.getPhoto_id(),"verify",true,false)
                                                        .execute(getActivity().getString(R.string.infura));
                                                dialog.dismiss();
                                                progressDialog.setTitle("Loading");
                                                progressDialog.setMessage("Please Wait...");
                                                progressDialog.setCancelable(false);
                                                progressDialog.show();
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                });
                                fakeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        builder.setMessage("Once you marked this post as fake. You will receive the location of the user. " +
                                                "\nDo you still want to mark this post as fake?");
                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mFirebaseMethods.updateResponder(mPhoto, finalResponder_id,
                                                        false,finalStatus);
                                                new InitWeb3j(mPhoto.getUser_id(),mPhoto.getPhoto_id(),"fake",false, false)
                                                        .execute(getActivity().getString(R.string.infura));
                                                dialog.dismiss();
                                                progressDialog.setTitle("Loading");
                                                progressDialog.setMessage("Please Wait...");
                                                progressDialog.setCancelable(false);
                                                progressDialog.show();
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                respondents.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialog();
                    }
                });
            }
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild("photo_id")
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        newPhoto.setLatitude((double) objectMap.get("latitude"));
                        newPhoto.setLongitude((double) objectMap.get("longitude"));
                        newPhoto.setCaption(objectMap.get("caption").toString());
                        newPhoto.setTags(objectMap.get("tags").toString());
                        newPhoto.setPhoto_id(objectMap.get("photo_id").toString());
                        newPhoto.setUser_id(objectMap.get("user_id").toString());
                        newPhoto.setDate_created(objectMap.get("date_created").toString());
                        newPhoto.setImage_path(objectMap.get("image_path").toString());
                        newPhoto.setAddress(objectMap.get("address").toString());
                        GlideApp
                                .with(mContext.getApplicationContext())
                                .load(newPhoto.getImage_path())
                                .placeholder(R.color.grey)
                                .centerCrop()
                                .into(mPostImage);

                        List<Comment> commentList = new ArrayList<Comment>();
                        for(DataSnapshot dSnapshot: singleSnapshot.child("comments").getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            commentList.add(comment);
                        }

                        for(DataSnapshot ds: singleSnapshot.child("responder").getChildren()){
                             if(ds.exists()) {
                                 Responder responder = new Responder();
                                 responder.setResponder_id(ds.getValue(Responder.class).getResponder_id());
                                 responder.setUser_id(ds.getValue(Responder.class).getUser_id());
                                 if (ds.getValue(Responder.class).getStatus() != null) {
                                     responder.setStatus(ds.getValue(Responder.class).getStatus());
                                     responder.setLegit(ds.getValue(Responder.class).isLegit());
                                 }
                                 responders.add(responder);
                             }
                        }

                        Log.d(TAG, "onDataChange: Responders " + responders.toString());

                        newPhoto.setComments(commentList);
                        mPhoto = newPhoto;

                        getCurrentUser();
                        getPhotoDetails();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch(NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException" + e.getMessage());
        }
    }

    private void openDialog(){
        RespondentsDialog respondentsDialog = new RespondentsDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("RESPONDERS",responders);
        respondentsDialog.setArguments(args);
        respondentsDialog.show(((FragmentActivity)mContext).getSupportFragmentManager(),"RespondentsDialog");
        respondentsDialog.setTargetFragment(this,1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isAdded()){
            init();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: " + context.getApplicationContext());
        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();

        }catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
    }

    private void getLikeString(){
        Log.d(TAG, "getLikeString: getting likes string ");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child("likes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild("user_id")
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like: " + singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");

                            if(mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser = true;
                            }
                            else{
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;
                            if(length == 1){
                                mLikeString = "Liked by " + splitUsers[0];
                            }
                            else if(length == 2){
                                mLikeString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                            }
                            else if(length > 2){
                                mLikeString = "Liked by " + splitUsers[0] + " and " + (splitUsers.length-1) + " others";
                            }
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    mLikeString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    mCurrentUser = singleSnapshot.getValue(User.class);
                }
                getLikeString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child("likes");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        String keyID = singleSnapshot.getKey();
                        if(mLikedByCurrentUser && singleSnapshot.getValue(Like.class)
                                .getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();
                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(mPhoto.getUser_id())
                                    .child(mPhoto.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();


                            mHeart.toggleLike();
                            getLikeString();
                        }
                        else if(!mLikedByCurrentUser){
                            addNewLike();
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child("likes")
                .child(newLikeID)
                .setValue(like);
        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child("likes")
                .child(newLikeID)
                .setValue(like);

        mHeart.toggleLike();
        getLikeString();

        if(!mPhoto.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            String user_id = mPhoto.getUser_id();
            String from_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String type = "like";
            token = mUserAccountSettings.getDevice_token();
            likeMessage = mUserAccountSettings.getUsername() + " liked your post: \"" + mPhoto.getCaption() + "\"";
            mFirebaseMethods.addNotification(user_id,from_id,type,likeMessage, mPhoto.getPhoto_id());
            new Notify(token,likeMessage).execute();
        }
    }

    private void getPhotoDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    GlideApp
                            .with(mContext.getApplicationContext())
                            .load(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo())
                            .placeholder(R.color.grey)
                            .centerCrop()
                            .into(mProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private String getCallingActivityFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getString(getString(R.string.home_activity));
        }
        else{
            return null;
        }
    }
    private String getEmergencyFromBundle(){
        Bundle bundle = this.getArguments();
        if(bundle != null){
            if(bundle.getString("emergency") != null){
                return bundle.getString("emergency");
            }
            else{
                return null;
            }
        }
        else{
            return  null;
        }
    }

    private void setupWidgets(){
        if(mLikeString == ""){
            mLikes.setVisibility(View.GONE);
        }
        else{
            mLikes.setVisibility(View.VISIBLE);
        }
        String timestampDiff = getTimeStampDifference();
        if(!timestampDiff.equals("0")){
            mTimeStamp.setText(timestampDiff);
        }
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikeString);
        mCaption.setText(mPhoto.getCaption());
        mAddress.setText(mPhoto.getAddress());
        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putParcelable("PHOTO",mPhoto);
                Log.d(TAG, "onClick: " + b.getParcelable("PHOTO"));
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtras(b);
                mContext.startActivity(intent);
            }
        });
        if(mPhoto.getComments().size() > 0){
            mComments.setVisibility(View.VISIBLE);
            mComments.setText("View all " + mPhoto.getComments().size() + " comments");
        }
        else{
            mComments.setVisibility(View.GONE);
        }

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCommentThreadSelectedListener.onCommentThreadSelecetedListener(mPhoto);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getCallingActivityFromBundle().equals("Likes Activity")){
                    getActivity().getSupportFragmentManager().popBackStack();
                    getActivity().finish();
                }
                else if(getCallingActivityFromBundle().equals("Home Activity")){
                    getActivity().getSupportFragmentManager().popBackStack();
                    ((HomeActivity)getActivity()).showLayout();
                }
                else{
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCommentThreadSelectedListener.onCommentThreadSelecetedListener(mPhoto);
            }
        });



        if(mLikedByCurrentUser){
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
        else{
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
    }

    private String getTimeStampDifference(){
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+8"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = mPhoto.getDate_created();
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
                            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 / 60 / 60 / 24))) + " days ago";
                    }
                    else{
                        Log.d(TAG, "getTimeStampDifference: " + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60));
                        difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 / 60 / 60 ))) + " hours ago";
                    }
                }
                else{
                    Log.d(TAG, "getTimeStampDifference: " + Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60));
                    difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 / 60 ))) + " minutes ago";
                }
            }
            else{
                Log.d(TAG, "getTimeStampDifference: " + Math.round((today.getTime() - timestamp.getTime()) / 1000));
                difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime())/ 1000 ))) + " seconds ago";
            }
            Log.d(TAG, "getTimeStampDifference: " + difference);
        }catch(ParseException e){
            Log.e(TAG, "getTimeStampDifference: ParseException" + e.getMessage());
            difference = "0";
        }
        return difference;
    }

    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable("PHOTO");
        }
        else{
            return null;
        }
    }

    private int getActivityNumFromBundle(){
        Log.d(TAG, "getActivityNumFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        }
        else{
            return 0;
        }
    }

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(),getActivity(),bottomNavigationView,mActivityNumber);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

    //Firebase Section
    private void setupFireBaseAuth(){
        Log.d(TAG, "setupFireBaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                }
                else{
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    private class InitWeb3j extends AsyncTask<String, String, String> {

        String user_id, post_id,type;
        boolean isFake;
        boolean isGet;
        private int fake,real;

        public InitWeb3j(String user_id, String post_id, String type, boolean isFake, boolean isGet) {
            this.user_id = user_id;
            this.post_id = post_id;
            this.type = type;
            this.isFake = isFake;
            this.isGet = isGet;
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            try {
                Web3j web3 = Web3jFactory.build(new HttpService(url));
                Credentials credentials = Credentials.create(getString(R.string.private_key));
                Emercify contract = Emercify.load(
                        getString(R.string.contract_address),
                        web3, credentials,
                        ManagedTransaction.GAS_PRICE,
                        Contract.GAS_LIMIT
                );
                if(isGet){
                    Tuple3<String, BigInteger, BigInteger> output = contract.getEmergencyPostReports(this.post_id).send();
                    real = output.getValue2().intValue();
                    fake = output.getValue3().intValue();
                }
                else{
                    contract.reportPost(
                            this.user_id,
                            this.post_id,
                            this.isFake
                    ).send();
                }
                return "success";
            } catch (Exception e) {
                return e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isGet){
                fakeCount.setText(String.valueOf(fake));
                legitCount.setText(String.valueOf(real));
            }
            else{
                Toast.makeText(mContext, "Successfully Marked", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                if(type.equals("fake")){
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(getString(R.string.calling_activity),"Likes Activity");
                    intent.putExtra("REPORT PHOTO",getPhotoFromBundle());
                    startActivity(intent);
                }
                else{
                    init();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getActivity().getString(R.string.dbname_users))
                .orderByChild("user_id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    photoUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
