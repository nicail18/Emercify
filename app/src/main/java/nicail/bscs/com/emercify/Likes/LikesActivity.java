package nicail.bscs.com.emercify.Likes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.NotifRecyclerAdapter;
import nicail.bscs.com.emercify.Utils.Notify;
import nicail.bscs.com.emercify.models.Notifications;
import nicail.bscs.com.emercify.models.UserSettings;

public class LikesActivity extends AppCompatActivity {

    private static final String TAG = "LikesActivity";

    private static final int ACTIVITY_NUM = 3;

    private Context mContext = LikesActivity.this;
    private ImageView ivMap,test;
    private String token;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private UserSettings userSettings;
    private String userID;
    private ArrayList<Notifications> notifications;
    private ArrayList<Notifications> paginatedNotif;
    private NotifRecyclerAdapter notifRecyclerAdapter;
    private LinearLayoutManager manager;
    private int mResults,currentItems,totalItems,scrollOutItems;
    private Boolean isScrolling = false;
    private ProgressBar progressBar;

    RecyclerView notifsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifs);
        ivMap = (ImageView) findViewById(R.id.ivMap);
        test = (ImageView) findViewById(R.id.testNotif);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        notifications = new ArrayList<>();
        notifsRecyclerView = findViewById(R.id.notif_listview);
        Log.d(TAG, "onCreate: starting.");


        setupFireBaseAuth();
        setupBottomNavigationView();
        ivMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LikesActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                token = userSettings.getSettings().getDevice_token();
                Log.d(TAG, "onClick: " + token);
                new Notify(token,"Hello").execute();
            }
        });

        getNotifications();
    }

    public void getNotifications(){
        Log.d(TAG, "getNotifications: getting notifications");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final int[] count = {0};
        Query query = reference
                .child(getString(R.string.dbname_user_notification))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: " + ds.child("user_id").getValue());
                    Notifications notification = new Notifications();
                    Map<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();
                    notification.setMessage(objectMap.get("message").toString());
                    notification.setTimestamp(objectMap.get("timestamp").toString());
                    notification.setFrom_id(objectMap.get("from_id").toString());
                    notification.setUser_id(objectMap.get("user_id").toString());
                    notification.setType(objectMap.get("type").toString());
                    notification.setNotification_id(objectMap.get("notification_id").toString());

                    notifications.add(notification);
                    count[0]++;
                }
                if(count[0] > notifications.size()-1){
                    displayNotifs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayNotifs(){
        paginatedNotif = new ArrayList<>();
        if(notifications != null){
            try{
                Collections.sort(notifications, new Comparator<Notifications>() {
                    @Override
                    public int compare(Notifications o1, Notifications o2) {
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });

                int iterations = notifications.size();

                if(iterations > 10){
                    iterations = 10;
                }
                mResults = 10;
                for(int i = 0; i<iterations; i++){
                    paginatedNotif.add(notifications.get(i));
                }

                notifRecyclerAdapter = new NotifRecyclerAdapter(paginatedNotif);
                notifsRecyclerView.setAdapter(notifRecyclerAdapter);
                manager = new LinearLayoutManager(this);
                notifsRecyclerView.setLayoutManager(manager);
                notifsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                            isScrolling = true;
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        currentItems = manager.getChildCount();
                        totalItems = manager.getItemCount();
                        scrollOutItems = manager.findFirstVisibleItemPosition();

                        if(isScrolling && (currentItems + scrollOutItems == totalItems)){
                            isScrolling = false;
                            displayMoreNotifs();
                        }
                    }
                });

            }catch(NullPointerException e){
                Log.e(TAG, "displayNotifs: " + e.getMessage() );
            }catch(IndexOutOfBoundsException e){
                Log.e(TAG, "displayNotifs: " + e.getMessage() );
            }
        }
    }

    public void displayMoreNotifs(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");
        try{
            if(notifications.size() > mResults && notifications.size() > 0){
                int iterations;
                if(notifications.size() > (mResults+10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than photos");
                    iterations = notifications.size() - mResults;
                }

                for(int i = mResults; i<mResults + iterations; i++){
                    paginatedNotif.add(notifications.get(i));
                }
                mResults = mResults + iterations;
                notifRecyclerAdapter.notifyDataSetChanged();
            }
        }catch(NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
        }catch(IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException" + e.getMessage() );
        }
    }

    //Bottom Navigation View Setup
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);

        int incoming = 0;

        Intent intent = getIntent();
        if(intent.hasExtra("home")){
            incoming = 0;
        }else if(intent.hasExtra("search")){
            incoming = 1;
        }else  if(intent.hasExtra("circle")){
            incoming = 2;
        }else if(intent.hasExtra("android"))
            incoming = 4;

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx, incoming);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
    //Firebase Section
    private void setupFireBaseAuth(){
        Log.d(TAG, "setupFireBaseAuth: setting up firebase auth");
        mFirebaseMethods = new FirebaseMethods(this);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();;

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
                Log.d(TAG, "onDataChange: " + dataSnapshot.getChildren());
                userSettings = mFirebaseMethods.getUserSettings(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
