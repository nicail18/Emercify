package nicail.bscs.com.emercify.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.List;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.GlideApp;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.ViewCommentsFragment;
import nicail.bscs.com.emercify.Utils.ViewPostFragment;
import nicail.bscs.com.emercify.Utils.ViewProfileFragment;
import nicail.bscs.com.emercify.models.Photo;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener{

    private static final String TAG = "ProfileActivity";

    private static final int INTERVAL = 3000;

    private ViewProfileFragment viewProfileFragment;
    private ProfileFragment profileFragment;
    private String callingActivity;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public void onCommentThreadSelecetedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelecetedListener: selected comment thread");
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable("PHOTO",photo);
        args.putString(getString(R.string.home_activity),callingActivity);
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected an image gridview: " + photo.toString());
        Intent intent = getIntent();
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        if(intent.hasExtra(getString(R.string.intent_emergency))){
            args.putString("emergency","emergency");
        }
        args.putParcelable("PHOTO",photo);
        args.putInt(getString(R.string.activity_number),activityNumber);
        if(callingActivity != null){
            args.putString(getString(R.string.home_activity),callingActivity);
        }
        else{
            args.putString(getString(R.string.home_activity),"Profile Activity");
        }
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    private static final int ACTIVITY_NUM = 4;
    private Context mContext = ProfileActivity.this;
    private ProgressBar mProgressBar;
    private ImageView profilePhoto;
    private static final int NUM_GRID_COLUMNS = 3;
    private FirebaseMethods firebaseMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting.");
        firebaseMethods = new FirebaseMethods(this);

        init();
    }

    private void init(){
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "init: searching for user object attached as intent extra");
            if(intent.hasExtra(getString(R.string.intent_user))){
                viewProfileFragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intent_user),intent.getParcelableExtra(getString(R.string.intent_user)));
                viewProfileFragment.setArguments(args);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container,viewProfileFragment);
                transaction.addToBackStack(getString(R.string.view_profile_fragment));
                transaction.commit();
            }
            else if(intent.hasExtra(getString(R.string.intent_like))){
                Photo photo = new Photo();
                photo = intent.getParcelableExtra(getString(R.string.intent_like));
                callingActivity = "Likes Activity";
                Log.d(TAG, "init: " + photo);
                onGridImageSelected(photo,3);
            }
            else if(intent.hasExtra(getString(R.string.intent_comment))){
                Photo photo = new Photo();
                photo = intent.getParcelableExtra(getString(R.string.intent_comment));
                callingActivity = "Likes Activity";
                Log.d(TAG, "init: " + photo);
                onCommentThreadSelecetedListener(photo);
            }
            else if(intent.hasExtra(getString(R.string.intent_emergency))){
                Photo photo = new Photo();
                photo = intent.getParcelableExtra(getString(R.string.intent_emergency));
                callingActivity = "Likes Activity";
                onGridImageSelected(photo,3);
            }
            else{
                Toast.makeText(mContext, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.d(TAG, "init: dont have extra");
            profileFragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container,profileFragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }

    }

    private void startNotificationRunnable(){
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                retrieveNotifs();
                handler.postDelayed(runnable,INTERVAL);
            }
        },INTERVAL);
    }

    private void stopNotificationRunnable(){
        handler.removeCallbacks(runnable);
    }

    private void retrieveNotifs(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_notification))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    boolean check = (boolean) ds.child("badge_seen").getValue();
                    if(!check){
                        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
                        BottomNavigationViewHelper.showBadge(
                                mContext,
                                bottomNavigationViewEx,
                                R.id.ic_alert,
                                "1");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        int num = getSupportFragmentManager().getBackStackEntryCount();
        if(num == 1){
            finish();
        }
        else{
            getFragmentManager().popBackStack();
        }
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        startNotificationRunnable();
        firebaseMethods.updateOnlineStatus(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseMethods.updateOnlineStatus(false);
        stopNotificationRunnable();
    }

    @Override
    public void onPause() {
        super.onPause();
        firebaseMethods.updateOnlineStatus(false);
    }
}
