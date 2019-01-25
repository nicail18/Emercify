package nicail.bscs.com.emercify.Profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Share.ShareActivity;
import nicail.bscs.com.emercify.Utils.Emercify;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.GlideApp;
import nicail.bscs.com.emercify.Utils.UniversalImageLoader;
import nicail.bscs.com.emercify.dialogs.ConfirmPasswordDialog;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;
import nicail.bscs.com.emercify.models.UserSettings;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private CircleImageView mProfilePhoto;
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private UserSettings mUserSettings;
    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_editprofile);

        mProfilePhoto = (CircleImageView) findViewById(R.id.profile_photo);
        mDisplayName = (EditText) findViewById(R.id.display_name);
        mUsername = (EditText) findViewById(R.id.username);
        mWebsite = (EditText) findViewById(R.id.website);
        mDescription = (EditText) findViewById(R.id.description);
        mEmail = (EditText) findViewById(R.id.email);
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        mFirebaseMethods = new FirebaseMethods(this);
        mChangeProfilePhoto = (TextView) findViewById(R.id.changeProfilePhoto);
        mContext = this;
        progressDialog = new ProgressDialog(mContext);

        setupFireBaseAuth();

        //back arrow for navigation back to ProfileActivity
        ImageView backarrow = (ImageView) findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
                finish();
            }
        });

        ImageView checkMark = (ImageView) findViewById(R.id.saveChanges);
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bounce ));
                saveProfileSettings();
            }
        });
    }

    private void saveProfileSettings(){
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());

        progressDialog.setTitle("Saving Profile Settings");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        if(!mUserSettings.getUser().getUsername().equals(username)){
            progressDialog.show();
            checkIfUsernameExists(username);
        }
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            progressDialog.show();
            mFirebaseMethods.updateUserAccountSettings(displayName,null,null,0);
            new InitWeb3j(FirebaseAuth.getInstance().getCurrentUser().getUid(),displayName,"","name")
                    .execute(getString(R.string.infura));
        }
        if(!mUserSettings.getSettings().getWebsite().equals(website)){
            progressDialog.show();
            mFirebaseMethods.updateUserAccountSettings(null,null,website,0);
            progressDialog.dismiss();
            Toast.makeText(mContext, "Saved Website", Toast.LENGTH_SHORT).show();
        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            progressDialog.show();
            mFirebaseMethods.updateUserAccountSettings(null,description,null,0);
            progressDialog.dismiss();
            Toast.makeText(mContext, "Saved Description", Toast.LENGTH_SHORT).show();
        }
        if(mUserSettings.getUser().getPhone_number() != phoneNumber){
            progressDialog.show();
            mFirebaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);
            progressDialog.dismiss();
            Toast.makeText(mContext, "Saved PhoneNumber", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if username already exists");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild("username")
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    mFirebaseMethods.updateUsername(username);
                    new InitWeb3j(FirebaseAuth.getInstance().getCurrentUser().getUid(),"",username,"username")
                            .execute(mContext.getString(R.string.infura));
                    Toast.makeText(mContext, "Saved username", Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(mContext, "That username already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieved from firebase database " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: " + mContext);
        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        mUserSettings = userSettings;

        GlideApp
                .with(getApplicationContext())
                .load(settings.getProfile_photo())
                .placeholder(R.color.grey)
                .centerCrop()
                .into(mProfilePhoto);
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(user.getEmail());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent = new Intent(EditProfileActivity.this, ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
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
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private class InitWeb3j extends AsyncTask<String, String, String> {

        String user_id, name, username, type;

        public InitWeb3j(String user_id, String name, String username, String type) {
            this.user_id = user_id;
            this.name = name;
            this.username = username;
            this.type = type;
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
                if(type == "name"){
                    contract._editName(
                            this.user_id,
                            this.name
                    ).send();
                }
                else{
                    contract._editUsername(
                            this.user_id,
                            this.username
                    ).send();
                }
                return "Saved Successfully";
            } catch (Exception e) {
                return e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mContext, "Successfully Saved", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
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
}
