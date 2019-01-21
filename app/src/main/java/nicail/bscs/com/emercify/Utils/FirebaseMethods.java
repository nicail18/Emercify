package nicail.bscs.com.emercify.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.Profile.AccountSettingsActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.models.Notifications;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.User;
import nicail.bscs.com.emercify.models.UserAccountSettings;
import nicail.bscs.com.emercify.models.UserSettings;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    private String userID;
    private ArrayList<UserAccountSettings> userLists;

    private double mPhotoUploadProgress = 0;
    private Context mContext;
    private ProgressDialog progressDialog;

    public FirebaseMethods(Context context){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;
        userLists = new ArrayList<>();
        getAllUsers();
        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    private void getAllUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: checkDistance" + ds.getValue(UserAccountSettings.class).toString());
                    userLists.add(ds.getValue(UserAccountSettings.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadNewPhoto(String photoType, final String caption,
                               int count, String imgUrl, Bitmap bm,
                               final String address, final double latitude,
                               final double longitude, final String type){
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo");

        progressDialog = new ProgressDialog(mContext, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Uploading Photo");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        FilePaths filePaths = new FilePaths();
        if(photoType.equals("new_photo")){
            Log.d(TAG, "uploadNewPhoto: uploading new photo");
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+ user_id+"/photo"+ (count + 1));
            if(bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm,100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String firebaseUrl = uri.toString();
                            Log.d(TAG, "onSuccess: Successfully Uploaded ");
                            Toast.makeText(mContext, "Successfully Uploaded ", Toast.LENGTH_SHORT).show();

                            addPhotoToDatabase(caption,firebaseUrl,address,latitude,longitude,type);
                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    String firebaseUrl = "http://pm1.narvii.com/6645/a59a0d2a7b9677ed7ba09b1a503eaa3f00a94592_00.jpg";
                    addPhotoToDatabase(caption,firebaseUrl,address,latitude,longitude,null);
                    Toast.makeText(mContext, "Photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f",progress) + " %",  Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }
        else if(photoType.equals("profile_photo")){
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");


            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+ user_id+"/profile_photo");
            if(bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm,100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String firebaseUrl = uri.toString();
                            Log.d(TAG, "onSuccess: Successfully Uploaded ");

                            setProfilePhoto(firebaseUrl);
                            progressDialog.dismiss();
                        }
                    });
                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapater
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    Toast.makeText(mContext, "Photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f",progress) + " %",  Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }
    }

    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profile_photo")
                .setValue(url);
    }

    private String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+8"));
        return sdf.format(new Date());
    }

    public void updateDevice_token(String token){
        Log.d(TAG, "updateDevice_token: updating token to: " + token);
        myRef.child("users").child(userID).child("device_token").setValue(token);
        myRef.child("user_account_settings").child(userID).child("device_token").setValue(token);
    }

    public void updateOnlineStatus(boolean status){
        Log.d(TAG, "updateOnlineStatus: updating online status");
        myRef.child("users").child(userID).child("online_status").setValue(status);
        myRef.child("user_account_settings").child(userID).child("online_status").setValue(status);
    }

    private void addPhotoToDatabase(String caption, String url,
                                    String address, double latitude,
                                    double longitude,String type){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");
        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);
        photo.setAddress(address);
        photo.setLatitude(latitude);
        photo.setLongitude(longitude);
        if(type != null){
            photo.setType(type);
            int x;
            for(x = 0; x < userLists.size(); x++) {
                if (!userLists.get(x).getUser_id()
                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    float[] distance = new float[1];
                    Location.distanceBetween(userLists.get(x).getLatitude(), userLists.get(x).getLongitude()
                            , latitude, longitude, distance);
                    Log.d(TAG, "checkDistance: user ID " + userLists.get(x).getUsername());
                    Log.d(TAG, "checkDistance: distance " + distance[0]);
                    if (distance[0] < 1000) {
                        String message = "There is an Emergency!";
                        if (type.equals("emergency")) {
                            addNotification(
                                    userLists.get(x).getUser_id(),
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    "emergency",
                                    message,
                                    newPhotoKey);
                            new Notify(userLists.get(x).getDevice_token(), message).execute();
                        } else if (type.equals("report")) {
                        }
                    }
                }
            }
        }

        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(newPhotoKey).setValue(photo);

        if(type.equals("emergency")){
            new InitWeb3j(newPhotoKey,FirebaseAuth.getInstance().getCurrentUser().getUid(),caption)
                    .execute(mContext.getString(R.string.infura));
        }
        else{
            Toast.makeText(mContext, "Successfully Uploaded ", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            Intent intent = new Intent(mContext, HomeActivity.class);
            mContext.startActivity(intent);
        }



    }


    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(userID).getChildren()){
            count++;
        }

        return count;
    }

    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to: " + username);
        myRef.child("users").child(userID).child("username").setValue(username);
        myRef.child("user_account_settings").child(userID).child("username").setValue(username);
    }

    public void updateEmail(String email){
        Log.d(TAG, "updateUsername: updating email to: " + email);
        myRef.child("users").child(userID).child("email").setValue(email);
    }

    public void updateUserAccountSettings(String display_name, String description, String website, long phone_number){
        Log.d(TAG, "updateUserAccountSettings: updating user account settings to");
        if(display_name != null){
            myRef.child("user_account_settings").child(userID).child("display_name").setValue(display_name);
        }
        if(description != null){
            myRef.child("user_account_settings").child(userID).child("description").setValue(description);
        }
        if(website != null){
            myRef.child("user_account_settings").child(userID).child("website").setValue(website);
        }
        if(phone_number != 0){
            myRef.child("users").child(userID).child("phone_number").setValue(phone_number);
        }
    }

   /* public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIfUsernameExists: checking if "+ username +" already exists");

        User user = new User();
        for(DataSnapshot ds: dataSnapshot.child("users").getChildren()){
            Log.d(TAG, "checkIfUsernameExists: datasnapshot: " + ds);

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIfUsernameExists: username: " + user.getUsername());

            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + user.getUsername());
                return true;
            }

        }

        return false;
    }*/

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     * @param name
     */
    public void registerNewEmail(final String email, String password, final String name){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete: " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else if(task.isSuccessful()){
                            //send verification email
                            sendVerificationEmail();

                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed: " + userID);
                        }
                    }
                });
    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }
                            else{
                                Toast.makeText(mContext, "Couldn't send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void updateLocation(double latitude, double longitude ){
        Log.d(TAG, "updateLocation: updating location");
        myRef.child("users").child(userID).child("latitude").setValue(latitude);
        myRef.child("users").child(userID).child("longitude").setValue(longitude);
        myRef.child("user_account_settings").child(userID).child("latitude").setValue(latitude);
        myRef.child("user_account_settings").child(userID).child("longitude").setValue(longitude);
    }

    /**
     * Add information to the users node
     * ADd information to the user_account_setttings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo){
        User user = new User(userID, 1,
                StringManipulation.condenseUsername(username),
                email,FirebaseInstanceId.getInstance().getToken(),
                true,0,0);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID,
                FirebaseInstanceId.getInstance().getToken(),
                true,
                0,
                0);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for(DataSnapshot ds: dataSnapshot.getChildren()){
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))){
                Log.d(TAG, "getUserAccountSettings: dataSnapshot: " + ds);

                try{
                    settings.setDisplay_name(
                            ds.child(userID)
                            .getValue(UserAccountSettings.class)
                            .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );
                    settings.setDevice_token(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDevice_token()
                    );
                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                }catch(NullPointerException e){
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }
            }
            if(ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: dataSnapshot: " + ds);

                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );
                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );
                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number()
                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
                user.setDevice_token(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
                Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + user.toString());
            }
        }
        return new UserSettings(user,settings);
    }

    public void updateNotification(String user_id,boolean status_seen,String notification_id){
        if(!status_seen){
            myRef.child(mContext.getString(R.string.dbname_user_notification))
                    .child(user_id).child(notification_id).child("status_seen").setValue(true);
            myRef.child(mContext.getString(R.string.dbname_notification))
                    .child(notification_id).child("status_seen").setValue(true);
        }
    }

    public void updateBadgeSeen(String user_id,boolean status_seen,String notification_id){
        myRef.child(mContext.getString(R.string.dbname_user_notification))
                .child(user_id).child(notification_id).child("badge_seen").setValue(true);
        myRef.child(mContext.getString(R.string.dbname_notification))
                .child(notification_id).child("badge_seen").setValue(true);
    }

    public void deletePhoto(String user_id, String photo_id){
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(photo_id).removeValue();
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(user_id).child(photo_id).removeValue();
    }

    public void addNotification(String user_id, String from_id, String type, String message,String activity_id){
        String notificationKey = myRef.child(mContext.getString(R.string.dbname_notification)).push().getKey();
        Notifications notification = new Notifications();
        notification.setUser_id(user_id);
        notification.setFrom_id(from_id);
        notification.setType(type);
        notification.setTimestamp(getTimeStamp());
        notification.setMessage(message);
        notification.setNotification_id(notificationKey);
        notification.setStatus_seen(false);
        notification.setActivity_id(activity_id);
        notification.setBadge_seen(false);

        myRef.child(mContext.getString(R.string.dbname_notification))
                .child(notificationKey)
                .setValue(notification);

        myRef.child(mContext.getString(R.string.dbname_user_notification))
                .child(user_id)
                .child(notificationKey)
                .setValue(notification);
    }

    private class InitWeb3j extends AsyncTask<String, String, String> {

        String photo_id, user_id, caption;

        public InitWeb3j(String photo_id, String user_id, String caption) {
            this.photo_id = photo_id;
            this.user_id = user_id;
            this.caption = caption;
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            try {
                Web3j web3 = Web3jFactory.build(new HttpService(url));
                Credentials credentials = Credentials.create(mContext.getString(R.string.private_key));
                Emercify contract = Emercify.load(
                        mContext.getString(R.string.contract_address),
                        web3, credentials,
                        ManagedTransaction.GAS_PRICE,
                        Contract.GAS_LIMIT
                );
                contract._setEmergencyPost(
                        photo_id,
                        user_id,
                        caption
                ).send();
                return "Successfully Uploaded";
            } catch (Exception e) {
                return e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mContext, "Successfully Uploaded ", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            Intent intent = new Intent(mContext, HomeActivity.class);
            mContext.startActivity(intent);
        }
    }
}
