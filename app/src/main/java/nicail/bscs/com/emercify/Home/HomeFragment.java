package nicail.bscs.com.emercify.Home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.MainfeedListAdapter;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.Photo;

public class    HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ArrayList<Photo> mPaginatedPhotos;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();

        return view;
    }

    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +  singleSnapshot.child("user_id").getValue());
                    mFollowing.add(singleSnapshot.child("user_id").getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                getPhotos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i <mFollowing.size(); i++) {
            final int count = i;
            Query query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild("user_id")
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String,Object>) singleSnapshot.getValue();
                        
                        photo.setCaption(objectMap.get("caption").toString());
                        photo.setTags(objectMap.get("tags").toString());
                        photo.setPhoto_id(objectMap.get("photo_id").toString());
                        photo.setUser_id(objectMap.get("user_id").toString());
                        photo.setDate_created(objectMap.get("date_created").toString());
                        photo.setImage_path(objectMap.get("image_path").toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for(DataSnapshot dSnapshot: singleSnapshot.child("comments").getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);
                        mPhotos.add(photo);
                    }
                    if(count >= mFollowing.size()-1){
                        displayPhotos();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();
        if(mPhotos != null){
            try{
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = mPhotos.size();

                if(iterations > 10){
                    iterations = 10;
                }

                mResults = 10;
                for(int i = 0; i<iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mAdapter = new MainfeedListAdapter(getActivity(),R.layout.layout_mainfeed_listitem,mPaginatedPhotos);
                mListView.setAdapter(mAdapter);
            }catch(NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
            }catch(IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException" + e.getMessage() );
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");
        try{
            if(mPhotos.size() > mResults && mPhotos.size() > 0){
                int iterations;
                if(mPhotos.size() > (mResults+10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than photos");
                    iterations = mPhotos.size() - mResults;
                }

                for(int i = mResults; i<mResults + iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        }catch(NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
        }catch(IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException" + e.getMessage() );
        }
    }
}
