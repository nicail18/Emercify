package nicail.bscs.com.emercify.Profile;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
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
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.Likes.LikesActivity;
import nicail.bscs.com.emercify.Login.LoginActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Share.ShareActivity;
import nicail.bscs.com.emercify.Utils.Emercify;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.Utils.UniversalImageLoader;
import nicail.bscs.com.emercify.dialogs.ConfirmPasswordDialog;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;
import nicail.bscs.com.emercify.models.UserSettings;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: " + password);

        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(),password);
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: user re-authenticated");

                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if(task.isSuccessful()){
                                        try{
                                            if(task.getResult().getProviders().size() == 1){
                                                Log.d(TAG, "onComplete: that email is already in use");
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Log.d(TAG, "onComplete: That email is available");

                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Log.d(TAG, "onComplete: User email address updated");
                                                                    Toast.makeText(getActivity(), "Email Updated", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        }catch(NullPointerException e){
                                            Log.e(TAG, "onComplete: NullPointerException" + e.getMessage());
                                        }
                                    }
                                }
                            });

                        }
                        else{
                            Log.d(TAG, "onComplete: re-authentication failed.");
                        }
                    }
                });
    }

    private static final String TAG = "EditProfileFragment";

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile,container,false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mFirebaseMethods = new FirebaseMethods(getActivity());
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        mContext = getActivity();
        progressDialog = new ProgressDialog(mContext);
        //setProfileImage();
        setupFireBaseAuth();

        //back arrow for navigation back to ProfileActivity
        ImageView backarrow = (ImageView) view.findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
                getActivity().finish();
            }
        });

        ImageView checkMark = (ImageView) view.findViewById(R.id.saveChanges);
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bounce ));
                saveProfileSettings();
            }
        });

        return view;
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
        progressDialog.show();

        if(!mUserSettings.getUser().getUsername().equals(username)){
            checkIfUsernameExists(username);
        }
        if(!mUserSettings.getUser().getEmail().equals(email)){
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(),"ConfirmPasswordDialog");
            dialog.setTargetFragment(EditProfileFragment.this,1);
        }
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            mFirebaseMethods.updateUserAccountSettings(displayName,null,null,0);
            new InitWeb3j(FirebaseAuth.getInstance().getCurrentUser().getUid(),displayName,"","name")
                    .execute(getString(R.string.infura));
        }
        if(!mUserSettings.getSettings().getWebsite().equals(website)){
            mFirebaseMethods.updateUserAccountSettings(null,null,website,0);
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Saved Website", Toast.LENGTH_SHORT).show();
        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            mFirebaseMethods.updateUserAccountSettings(null,description,null,0);
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Saved Description", Toast.LENGTH_SHORT).show();
        }
        if(mUserSettings.getUser().getPhone_number() != phoneNumber){
            mFirebaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Saved PhoneNumber", Toast.LENGTH_SHORT).show();
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
                            .execute(getActivity().getString(R.string.infura));
                    Toast.makeText(getActivity(), "Saved username", Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already exists", Toast.LENGTH_SHORT).show();
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

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        mUserSettings = userSettings;

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto, null,"");
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
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
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
