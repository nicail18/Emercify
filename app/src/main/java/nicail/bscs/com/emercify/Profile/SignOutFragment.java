package nicail.bscs.com.emercify.Profile;


import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import nicail.bscs.com.emercify.Login.LoginActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;

public class SignOutFragment extends Fragment {

    private static final String TAG = "SignOutFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;


    private GoogleSignInClient googleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signout,container,false);



        firebaseMethods = new FirebaseMethods(getActivity());


        FacebookSdk.sdkInitialize(getActivity());
        setupFireBaseAuth();
        Log.d(TAG, "onCreateView: " + mAuth.getCurrentUser().getUid());

        Button btnConfirmSignOut = (Button) view.findViewById(R.id.btnConfirmSignout);
        btnConfirmSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting sign out");
                firebaseMethods.updateOnlineStatus(false);
                signOut();
                mAuth.signOut();
                getActivity().finish();
            }
        });
        return view;
    }

    private void signOut(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getActivity(),gso);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null && !accessToken.isExpired()){
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/",
                    null, HttpMethod.DELETE, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    LoginManager.getInstance().logOut();
                }
            }).executeAsync();
        }
        else{
            googleSignInClient.signOut().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: something went wrong");
                }
            });
        }

    }

    private void setupFireBaseAuth(){
        Log.d(TAG, "setupFireBaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
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

                    Log.d(TAG, "onAuthStateChanged: navigating back to login screen");
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
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
