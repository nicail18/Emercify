package nicail.bscs.com.emercify.Profile;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.GlideApp;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.GridImageAdapter;
import nicail.bscs.com.emercify.Utils.UniversalImageLoader;
import nicail.bscs.com.emercify.Utils.ViewWeightAnimationWrapper;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.Like;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.UserAccountSettings;
import nicail.bscs.com.emercify.models.UserSettings;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int NUM_GRID_COLUMNS = 3;
    private static final int ACTIVITY_NUM = 4;
    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;

    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private ScrollView scrollView;

    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private RelativeLayout topLayout,rellayout2;
    private TextView nonetprof;
    private ImageView nonetimageprof;
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container, false);
        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.profileName);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mFollowers = (TextView) view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);
        topLayout = (RelativeLayout) view.findViewById(R.id.rellayout6);
        toolbar =  (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        rellayout2 = (RelativeLayout) view.findViewById(R.id.rellayout2);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(mContext);
        scrollView = (ScrollView) view.findViewById(R.id.scrolllayout);
        nonetprof = (TextView) view.findViewById(R.id.no_netprof);
        nonetimageprof = (ImageView) view.findViewById(R.id.no_netimageprof);
        mDisplayName.setVisibility(View.GONE);
        mUsername.setVisibility(View.GONE);
        mWebsite.setVisibility(View.GONE);
        mDescription.setVisibility(View.GONE);
        mProfilePhoto.setVisibility(View.GONE);
        mFollowers.setVisibility(View.GONE);
        mFollowing.setVisibility(View.GONE);
        mPosts.setVisibility(View.GONE);

        class Task extends AsyncTask<String, Integer, Boolean> {
            @Override
            protected void onPreExecute() {
                mProgressBar.setVisibility(View.VISIBLE);
                rellayout2.setVisibility(View.GONE);
                nonetprof.setVisibility(View.GONE);
                nonetimageprof.setVisibility(View.GONE);
                //nointernet.setVisibility(View.GONE);
                //nonotification.setVisibility(View.GONE);
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(Boolean result) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                    mProgressBar.setVisibility(View.GONE);
                    rellayout2.setVisibility(View.VISIBLE);
                }else{
                    mProgressBar.setVisibility(View.GONE);
                    nonetprof.setVisibility(View.VISIBLE);
                    nonetimageprof.setVisibility(View.VISIBLE);
                }
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
        new Task().execute();

        Log.d(TAG, "onCreateView: started");



        gridView.setOnTouchListener(new GridView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        setupBottomNavigationView();
        setupToolBar();

        setupFireBaseAuth();
        setupGridView();
        getFollowingCount();
        getFollowersCount();
        getPostsCount();

        TextView editProfile = (TextView) view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to edit profile fragment");

                Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });



        return view;


    }

    private void getFollowersCount(){
        mFollowersCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found follower: " + singleSnapshot.getValue());
                    mFollowersCount++;
                }
                mFollowers.setVisibility(View.VISIBLE);
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){
        mFollowingCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following: " + singleSnapshot.getValue());
                    mFollowingCount++;
                }
                mFollowing.setVisibility(View.VISIBLE);
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount(){
        mPostsCount = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found post: " + singleSnapshot.getValue());
                    mPostsCount++;
                }
                mPosts.setVisibility(View.VISIBLE);
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieved from firebase database " + userSettings.toString());

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        mPosts.setVisibility(View.VISIBLE);
        mPosts.setText(String.valueOf(mPostsCount));
        mDisplayName.setVisibility(View.VISIBLE);
        mUsername.setVisibility(View.VISIBLE);
        mWebsite.setVisibility(View.VISIBLE);
        mDescription.setVisibility(View.VISIBLE);
        mProfilePhoto.setVisibility(View.VISIBLE);
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());

        mProgressBar.setVisibility(View.GONE);
//        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto, null,"");
        if(settings.getProfile_photo().equals("")) {
            GlideApp
                    .with(mContext)
                    .load(R.drawable.ic_profile)
                    .placeholder(R.color.grey)
                    .centerCrop()
                    .into(mProfilePhoto);
        }else
            GlideApp
                    .with(mContext)
                    .load(settings.getProfile_photo())
                    .placeholder(R.color.grey)
                    .centerCrop()
                    .into(mProfilePhoto);

    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage() );
        }
        super.onAttach(context);
    }

    private void setupGridView(){
        Log.d(TAG, "setupGridView: setting up image grid");
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild("photo_id");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String,Object>) singleSnapshot.getValue();

                    try {
                        photo.setCaption(objectMap.get("caption").toString());
                        photo.setTags(objectMap.get("tags").toString());
                        photo.setPhoto_id(objectMap.get("photo_id").toString());
                        photo.setUser_id(objectMap.get("user_id").toString());
                        photo.setDate_created(objectMap.get("date_created").toString());
                        photo.setImage_path(objectMap.get("image_path").toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot.child("comments").getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }



                        photo.setComments(comments);

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot.child("likes").getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        photo.setLikes(likesList);
                        photos.add(photo);
                    }catch (NullPointerException e){
                        Log.e(TAG, "onDataChange: NullPointerException " + e.getMessage() );
                    }
                }
                Collections.reverse(photos);
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);
                ArrayList<String> imgUrls = new ArrayList<String>();
                for(int i = 0; i < photos.size(); i++){
                    imgUrls.add(photos.get(i).getImage_path());
                }

                GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,"",imgUrls);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position),ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void setupToolBar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings");
                Intent intent  = new Intent(mContext,AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);

        int incoming = 0;

        Intent intent = getActivity().getIntent();
        if(intent.hasExtra("home")){
            incoming = 0;
        }else if(intent.hasExtra("search")){
            incoming = 1;
        }else  if(intent.hasExtra("circle")){
            incoming = 2;
        }else if(intent.hasExtra("alert"))
            incoming = 3;

        BottomNavigationViewHelper.enableNavigation(mContext,getActivity(),bottomNavigationView,incoming);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(gridView);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                40,
                90);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(topLayout);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                60,
                10);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(gridView);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                90,
                40);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(topLayout);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                10,
                60);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

}
