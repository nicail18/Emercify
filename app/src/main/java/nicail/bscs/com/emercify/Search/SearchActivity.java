package nicail.bscs.com.emercify.Search;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.Profile.ProfileActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.GlideApp;
import nicail.bscs.com.emercify.Utils.UserListAdapter;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM = 1;
    private static final int INTERVAL = 3000;

    private Context mContext = SearchActivity.this;
    private FirebaseMethods firebaseMethods;

    private EditText mSearchParam;
    private ListView mListView;
    private CircleImageView mCircleImageView;

    private List<User> mUserList;
    private UserListAdapter mAdapter;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_search);
        mSearchParam = (EditText) findViewById(R.id.search);
        mListView = (ListView) findViewById(R.id.listView);
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);
        firebaseMethods = new FirebaseMethods(this);
        Log.d(TAG, "onCreate: starting.");

        hideSoftKeyboard();
        setupBottomNavigationView();
        initTextListener();
    }

    private void initTextListener(){
        Log.d(TAG, "initTextListener: initializing");

        mUserList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchParam.getText().toString();
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: search for a match: " + keyword);
        mUserList.clear();
        if(keyword.length() == 0){
        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_users)).orderByChild("username").equalTo(keyword);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).toString());

                        mUserList.add(singleSnapshot.getValue(User.class));
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: updating users list");

        mAdapter = new UserListAdapter(SearchActivity.this,R.layout.layout_user_listitem,mUserList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());
                if(mUserList.get(position).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid() )){
                    Log.d(TAG, "onItemClick: match");
                    Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                    intent.putExtra(getString(R.string.calling_activity),getString(R.string.search_activity));
                    intent.putExtra(getString(R.string.intent_user),mUserList.get(position));

                    startActivity(intent);
                }

            }
        });
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    //Bottom Navigation View Setup
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        int incoming = 0;

        Intent intent = getIntent();
        if(intent.hasExtra("home")){
            incoming = 0;
        }else if(intent.hasExtra("circle")){
            incoming = 2;
        }else  if(intent.hasExtra("alert")){
            incoming = 3;
        }else if(intent.hasExtra("android"))
            incoming = 4;

        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx,incoming);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
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
    public void onResume() {
        super.onResume();
        firebaseMethods.updateOnlineStatus(true);
        startNotificationRunnable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseMethods.updateOnlineStatus(false);
        stopNotificationRunnable();
    }

    @Override
    public void onPause() {
        super.onPause();;
        firebaseMethods.updateOnlineStatus(false);
    }
}
