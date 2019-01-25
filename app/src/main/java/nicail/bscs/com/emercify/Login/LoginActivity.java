package nicail.bscs.com.emercify.Login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.Emercify;
import nicail.bscs.com.emercify.Utils.FirebaseMethods;
import nicail.bscs.com.emercify.dialogs.Agreedisagree_Dialog;

public class LoginActivity extends AppCompatActivity{

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods mFirebaseMethods;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private CallbackManager callbackManager;
    private LoginButton loginButton;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;
    private ProgressDialog progressDialog;
    private LoginResult mLoginResult;
    private boolean isGoogle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.splash_fade_in, R.anim.splash_fade_out);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPleaseWait = (TextView) findViewById(R.id.pleaseWait);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        signInButton = (SignInButton) findViewById(R.id.googe_signin);
        loginButton = (LoginButton) findViewById(R.id.facebook_login);
        mContext = LoginActivity.this;
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Logging In To Emercify");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Log.d(TAG, "onCreate: started");

        loginButton.setReadPermissions("email");

        mProgressBar.setVisibility(View.GONE);
        mPleaseWait.setVisibility(View.GONE);
        setupFireBaseAuth();
        init();

    }


    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null");
        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

    private void openDialog(){
        Agreedisagree_Dialog agreedisagree_dialog = new Agreedisagree_Dialog(LoginActivity.this);
        agreedisagree_dialog.setSetAgreeClickListener(new Agreedisagree_Dialog.OnAgreeClickListener() {
            @Override
            public void onAgreeClickListener() {
                if(isGoogle){
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, 101);
                }
                else{
                    handleFacebookAccessToken(mLoginResult.getAccessToken());
                }
            }
        });
        agreedisagree_dialog.setCancelable(false);
        agreedisagree_dialog.show();

    }

    //Firebase Section
    private void init(){

        //initialize the button for logging in
        //Button btnLogin = (Button) findViewById(R.id.btn_login);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGoogle = true;
                openDialog();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        mLoginResult = loginResult;
                        isGoogle = false;
                        openDialog();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }
                });
            }
        });

//        btnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: attempting to login");
//                String email = mEmail.getText().toString();
//                String password = mPassword.getText().toString();
//                if(isStringNull(email) && isStringNull(password)){
//                    Toast.makeText(mContext,"You must fill out all the fields", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    mProgressBar.setVisibility(View.VISIBLE);
//                    mPleaseWait.setVisibility(View.VISIBLE);
//
//                    mAuth.signInWithEmailAndPassword(email, password)
//                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                                    if (!task.isSuccessful()) {
//                                        // If sign in fails, display a message to the user.
//                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
//                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                                Toast.LENGTH_SHORT).show();
//                                        mProgressBar.setVisibility(View.GONE);
//                                        mPleaseWait.setVisibility(View.GONE);
//                                    }
//                                    else{
//                                        Log.d(TAG, "signInWithEmail: successful login");
//                                        try {
//                                            if(user.isEmailVerified()){
//                                                Log.d(TAG, "onComplete: success email is verified");
//                                                Log.d(TAG, "onComplete: " + FirebaseInstanceId.getInstance().getToken());
//                                                String user_id = mAuth.getCurrentUser().getUid();
//                                                mFirebaseMethods.updateDevice_token(
//                                                        FirebaseInstanceId.getInstance().getToken());
//                                                mFirebaseMethods.updateOnlineStatus(true);
//                                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                                                startActivity(intent);
//                                            }
//                                            else{
//                                                Toast.makeText(mContext, "Email is not verified \n Check your email inbox", Toast.LENGTH_SHORT).show();
//                                                mProgressBar.setVisibility(View.GONE);
//                                                mPleaseWait.setVisibility(View.GONE);
//                                                mFirebaseMethods.sendVerificationEmail();
//                                                mAuth.signOut();
//                                            }
//                                        }catch(NullPointerException e){
//                                            Log.e(TAG, "onComplete: NullPointerException" + e.getMessage() );
//                                        }
//                                    }
//                                }
//                            });
//                }
//            }
//        });

        //TextView linkSignUp = (TextView) findViewById(R.id.link_signup);
//        linkSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: navigating to register screen");
//                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
//                startActivity(intent);
//            }
//        });

        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void handleFacebookAccessToken(AccessToken accessToken){
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String email = authResult.getUser().getEmail();
                String username = authResult.getUser().getDisplayName();
                mFirebaseMethods.addNewUser(email,username,"","","" );

                mFirebaseMethods.updateDevice_token(
                        FirebaseInstanceId.getInstance().getToken());
                mFirebaseMethods.updateOnlineStatus(true);
                progressDialog.show();
                new InitWeb3j(FirebaseAuth.getInstance().getCurrentUser().getUid(),username,username,email).execute(getString(R.string.infura));
            }
        });
    }

    private void setupFireBaseAuth(){
        Log.d(TAG, "setupFireBaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                    mFirebaseMethods = new FirebaseMethods(LoginActivity.this);
                }
                else{
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }

        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct);


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = acct.getEmail();
                            String username = acct.getDisplayName();
                            Log.d(TAG, "onComplete: " + email);
                            Log.d(TAG, "onComplete: " + username);
                            mFirebaseMethods.addNewUser(email,username,"","","" );

                            String user_id = mAuth.getCurrentUser().getUid();
                            mFirebaseMethods.updateDevice_token(
                                    FirebaseInstanceId.getInstance().getToken());
                            mFirebaseMethods.updateOnlineStatus(true);
                            progressDialog.show();
                            new InitWeb3j(FirebaseAuth.getInstance().getCurrentUser().getUid(),username,username,email).execute(getString(R.string.infura));;
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(mContext, "Login Failed", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private class InitWeb3j extends AsyncTask<String, String, String> {

        String user_id, name, username, email;

        public InitWeb3j(String user_id, String name, String username, String email) {
            this.user_id = user_id;
            this.name = name;
            this.username = username;
            this.email = email;
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
                contract._setUser(
                        this.user_id,
                        this.name,
                        this.username,
                        this.email
                ).send();
                return "LogIn Successfully";
            } catch (Exception e) {
                return e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mContext, "Login Successfully", Toast.LENGTH_LONG).show();
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
