package nicail.bscs.com.emercify.Likes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.math.BigDecimal;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
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
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.infura.InfuraHttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.Profile.ProfileActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.BlockChain;
import nicail.bscs.com.emercify.Utils.BottomNavigationViewHelper;
import nicail.bscs.com.emercify.Utils.CheckInternet;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.HelloWorld;
import nicail.bscs.com.emercify.Utils.MainfeedRecyclerAdapter;
import nicail.bscs.com.emercify.Utils.NotifRecyclerAdapter;
import nicail.bscs.com.emercify.Utils.Notify;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.Notifications;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserSettings;
import rx.schedulers.Schedulers;

public class LikesActivity extends AppCompatActivity implements
        NotifRecyclerAdapter.NotifRecyclerAdapterClickListener {

    private static final String TAG = "LikesActivity";

    private static final int ACTIVITY_NUM = 3;

    private Context mContext = LikesActivity.this;
    private ImageView ivMap;
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
    private ProgressBar progressBar,pbnotif;
    private TextView empty;
    private RelativeLayout rellayoutnotif;
    private RecyclerView notiflistview;
    private TextView nointernet, nonotification;
    private ImageView nonotifimage,nowifiimage,bcTest;
    private RecyclerView notifsRecyclerView;
    private ProgressDialog progressDialog;
    private static ArrayList<BlockChain> blockchain = new ArrayList<BlockChain>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifs);
        ivMap = (ImageView) findViewById(R.id.ivMap);
        bcTest = (ImageView) findViewById(R.id.bcTest);
        pbnotif = (ProgressBar) findViewById(R.id.progress_Barnotif);
        notifications = new ArrayList<>();
        notifsRecyclerView = (RecyclerView) findViewById(R.id.notif_listview);
        empty = findViewById(R.id.empty);
        nointernet = findViewById(R.id.no_internet);
        nonotification = findViewById(R.id.no_notification);
        pbnotif.setVisibility(View.VISIBLE);
        mFirebaseMethods = new FirebaseMethods(this);
        rellayoutnotif = (RelativeLayout) findViewById(R.id.rellayoutnotif);
        nonotifimage = (ImageView) findViewById(R.id.nonotif_image);
        nowifiimage = (ImageView) findViewById(R.id.nowifi_image);
        Log.d(TAG, "onCreate: starting.");

        new Task().execute();
        setupFireBaseAuth();
        setupBottomNavigationView();
        ivMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LikesActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        bcTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output = "";
                web3j();
//                blockchain.add(new BlockChain("Hi im the first block", "0"));
//                Log.d(TAG, "onClick: Trying to Mine block 1... " + "\n\n");
//                blockchain.get(0).mineBlock(2);
//
//                blockchain.add(new BlockChain("Yo im the second block",blockchain.get(blockchain.size()-1).hash));
//                Log.d(TAG, "onClick: Trying to Mine block 2... " + "\n\n");
//                blockchain.get(1).mineBlock(2);
//
//                blockchain.add(new BlockChain("Hey im the third block",blockchain.get(blockchain.size()-1).hash));
//                Log.d(TAG, "onClick: Trying to Mine block 3..." + "\n\n");
//                blockchain.get(2).mineBlock(2);
//                Log.d(TAG, "onClick: \nBlockchain is Valid: " + isChainValid() + "\n\n");
//
//                String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//                Log.d(TAG, "onClick: \nThe block chain: " + "\n\n");
//                Log.d(TAG, "onClick: " + blockchainJson);
            }
        });

        getNotifications();

    }

    private void web3j(){
        String infura = "https://ropsten.infura.io/v3/1f7164b0fe774aed805a297c99736218";
        InitWeb3j task = new InitWeb3j();
        progressDialog = new ProgressDialog(LikesActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        task.execute(infura);
    }

    private class InitWeb3j extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String walletFileName = "";
            try {
                Web3j web3 = Web3jFactory.build(new HttpService(url));
                Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
//                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
//                File[] list = file.listFiles();
//                String walletPath = "";
//                for(File path: list){
//                    if(path.getName().contains(".json") && path.getName().contains("UTC")){
//                        walletPath = path.getAbsolutePath();
//                        File walletFile = new File(walletPath);
//                        walletFileName = walletFile.getName();
//                    }
//                }
                Credentials credentials = Credentials.create("0x83FC7F52521183D064A23B5D29423AC9A9AB26F768FBA183FE858BA5A763FDD3");
//                Credentials credentials =
//                        WalletUtils.loadCredentials(
//                                "password",
//                                walletPath);
//                TransactionReceipt transferReceipt = Transfer.sendFunds(
//                        web3, credentials,
//                        "0xDc1Fb60F7E1eF6ef052A014EFbDF82debd803D06",  // you can put any address here
//                        java.math.BigDecimal.valueOf(1.0), Convert.Unit.WEI)  // 1 wei = 10^-18 Ether
//                        .send();
                HelloWorld contract = HelloWorld.deploy(
                        web3, credentials,
                        ManagedTransaction.GAS_PRICE,
                        Contract.GAS_LIMIT
                ).send();
                String contractAddress = contract.getContractAddress();
                String output = contract.helloWorld().send();
//                String walletFileName = "";
//                try {
//                    walletFileName = WalletUtils.generateLightNewWalletFile(
//                            "password",
//                            new File(Environment.getExternalStorageDirectory().getAbsolutePath())
//                    );
//                }catch(Exception e){
//                    Log.d(TAG, "doInBackground: Exception: " + e.getMessage());
//                    return e.getMessage();
//                }
                return contractAddress + "\n" + output;
            } catch (IOException e) {
                Log.d(TAG, "doInBackground: web3j IOException " + e.getMessage() );
                return "IOException" + e.getMessage();
            } catch (Exception e){
                Log.d(TAG, "doInBackground: web3j Exception: " + e.getMessage());
                return "Excepti on" + e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            final AlertDialog.Builder builder = new AlertDialog.Builder(LikesActivity.this);
            builder.setMessage(s)
                    .setCancelable(false)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public static Boolean isChainValid() {
        BlockChain currentBlock;
        BlockChain previousBlock;
        String hashTarget = new String(new char[2]).replace('\0', '0');

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                Log.d(TAG, "isChainValid: Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                Log.d(TAG, "isChainValid: Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, 2).equals(hashTarget)) {
                Log.d(TAG, "isChainValid: This block hasn't been mined");
                return false;
            }
        }
        return true;
    }

    class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            pbnotif.setVisibility(View.VISIBLE);
            rellayoutnotif.setVisibility(View.GONE);
            nointernet.setVisibility(View.GONE);
            nonotification.setVisibility(View.GONE);
            nonotifimage.setVisibility(View.GONE);
            nowifiimage.setVisibility(View.GONE);
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (CheckInternet.isNetwork(LikesActivity.this)) {
                //internet is connected do something
                pbnotif.setVisibility(View.GONE);
                displaynotif();
                //rellayoutnotif.setVisibility(View.VISIBLE);
                //nonotification.setVisibility(View.GONE);

            }else{
                //do something, net is not connected
                pbnotif.setVisibility(View.GONE);
                nointernet.setVisibility(View.VISIBLE);
                nowifiimage.setVisibility(View.VISIBLE);
                nonotification.setVisibility(View.GONE);
                nonotifimage.setVisibility(View.GONE);
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

    public void displaynotif(){
        if(notifRecyclerAdapter.getItemCount()!=0){
            notifsRecyclerView.setAdapter(notifRecyclerAdapter);
            rellayoutnotif.setVisibility(View.VISIBLE);
        }else{
            //Toast.makeText(LikesActivity.this, "No Notifications Available",Toast.LENGTH_SHORT).show();
            nonotification.setVisibility(View.VISIBLE);
            nonotifimage.setVisibility(View.VISIBLE);
        }
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
                    notification.setStatus_seen((boolean) objectMap.get("status_seen"));
                    notification.setActivity_id(objectMap.get("activity_id").toString());

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

                notifRecyclerAdapter = new NotifRecyclerAdapter(paginatedNotif,this);
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
        else{
            empty.setVisibility(View.VISIBLE);
            notifsRecyclerView.setVisibility(View.GONE);
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

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx, incoming);
        BottomNavigationViewHelper.removeBadge(bottomNavigationViewEx,R.id.ic_alert);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
    //Firebase Section
    private void setupFireBaseAuth(){
        Log.d(TAG, "setupFireBaseAuth: setting up firebase auth");
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

    @Override
    public void onUserClicked(int position) {
        Log.d(TAG, "onUserClicked: selected a notif " + notifications.get(position).toString());

        String activity_id = notifications.get(position).getActivity_id();
        final String type = notifications.get(position).getType();
        boolean status_seen = notifications.get(position).isStatus_seen();
        String notification_id = notifications.get(position).getNotification_id();
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFirebaseMethods.updateNotification(user_id,status_seen,notification_id);

        if(type.equals("follow")){
            if(activity_id
                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                Intent intent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(intent);
            }
            else{
                Query query = myRef
                        .child(getString(R.string.dbname_users))
                        .orderByChild("user_id")
                        .equalTo(activity_id);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = new User();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            user = ds.getValue(User.class);
                            Log.d(TAG, "onDataChange: " + user);

                        }
                        Intent intent = new Intent(LikesActivity.this, ProfileActivity.class);
                        intent.putExtra(getString(R.string.calling_activity),"Likes Activity");
                        intent.putExtra(getString(R.string.intent_user),user);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        else if(type.equals("like") || type.equals("comment")) {
            Photo photo = new Photo();
            Query query = myRef
                    .child(getString(R.string.dbname_photos))
                    .orderByChild("photo_id")
                    .equalTo(activity_id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String,Object>) ds.getValue();
                        Log.d(TAG, "onDataChange: " + objectMap.get("latitude"));
                        photo.setAddress(objectMap.get("address").toString());
                        photo.setLatitude((double) objectMap.get("latitude"));
                        photo.setLongitude((double) objectMap.get("longitude"));
                        photo.setCaption(objectMap.get("caption").toString());
                        photo.setTags(objectMap.get("tags").toString());
                        photo.setPhoto_id(objectMap.get("photo_id").toString());
                        photo.setUser_id(objectMap.get("user_id").toString());
                        photo.setDate_created(objectMap.get("date_created").toString());
                        photo.setImage_path(objectMap.get("image_path").toString());

                        Log.d(TAG, "onDataChange: " + photo.toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for(DataSnapshot dSnapshot: ds.child("comments").getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);

                        Log.d(TAG, "onDataChange: " + photo.toString());

                        if (type.equals("like")) {
                            Intent intent = new Intent(LikesActivity.this, ProfileActivity.class);
                            intent.putExtra(getString(R.string.calling_activity),"Likes Activity");
                            intent.putExtra(getString(R.string.intent_like),photo);
                            startActivity(intent);
                        }
                        else if (type.equals("comment")) {
                            Intent intent = new Intent(LikesActivity.this, ProfileActivity.class);
                            intent.putExtra(getString(R.string.calling_activity),"Likes Activity");
                            intent.putExtra(getString(R.string.intent_comment),photo);
                            Log.d(TAG, "onDataChange: " + intent);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else if(type.equals("emergency") || type.equals("report")){
            Query query = myRef
                    .child(getString(R.string.dbname_photos))
                    .orderByChild("photo_id")
                    .equalTo(activity_id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String,Object>) ds.getValue();
                        Log.d(TAG, "onDataChange: " + objectMap.get("latitude"));
                        photo.setAddress(objectMap.get("address").toString());
                        photo.setLatitude((double) objectMap.get("latitude"));
                        photo.setLongitude((double) objectMap.get("longitude"));
                        photo.setCaption(objectMap.get("caption").toString());
                        photo.setTags(objectMap.get("tags").toString());
                        photo.setPhoto_id(objectMap.get("photo_id").toString());
                        photo.setUser_id(objectMap.get("user_id").toString());
                        photo.setDate_created(objectMap.get("date_created").toString());
                        photo.setImage_path(objectMap.get("image_path").toString());

                        Intent intent = new Intent(LikesActivity.this, MapActivity.class);
                        intent.putExtra(getString(R.string.calling_activity),"Likes Activity");
                        intent.putExtra("INTENT PHOTO",photo);
                        Log.d(TAG, "onDataChange: " + intent);
                        startActivity(intent);

                        Log.d(TAG, "onDataChange: " + photo.toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseMethods.updateOnlineStatus(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFirebaseMethods.updateOnlineStatus(false);
    }
}
