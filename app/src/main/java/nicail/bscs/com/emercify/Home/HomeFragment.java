package nicail.bscs.com.emercify.Home;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.MainfeedRecyclerAdapter;
import nicail.bscs.com.emercify.Utils.RecyclerViewDivider;
import nicail.bscs.com.emercify.dialogs.View_Delete_Dialog;
import nicail.bscs.com.emercify.models.Comment;
import nicail.bscs.com.emercify.models.Photo;

public class HomeFragment extends Fragment implements
        View_Delete_Dialog.OnViewClickListener,
        View_Delete_Dialog.OnDeleteClickListener,
        View_Delete_Dialog.OnDownloadClickListener,
        SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "HomeFragment";

    @Override
    public void onDownloadClickListener(Photo photo) {
        file_download(photo.getImage_path());
    }

    public void file_download(String url)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date now = new Date();
        String date = simpleDateFormat.format(now);

        File direct = new File(Environment.getExternalStorageDirectory()
                + "/Emercify");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Emercify Photo")
                .setDestinationInExternalPublicDir("/Emercify", "EMERCIFY_IMG_"+date+".jpg");

        mgr.enqueue(request);

        Toast.makeText(getActivity(), "Photo Saved", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onViewClickListener(Photo photo) {
        Log.d(TAG, "onViewClickListener: " + photo.toString());
        ((HomeActivity)getContext()).OnViewClickListener(photo);
        ((HomeActivity)getContext()).hideLayout();
    }

    @Override
    public void onDeleteClickListener(Photo photo, int position) {
        Log.d(TAG, "onDeleteClickListener: " + photo.toString());
        ((HomeActivity)getContext()).OnDeleteClickListener(photo,position);

    }

    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ArrayList<Photo> mPaginatedPhotos;
    private ListView mListView;
    private RecyclerView recyclerView;
    private MainfeedRecyclerAdapter recyclerAdapter;
    private LinearLayoutManager manager;

    private int mResults,currentItems,totalItems,scrollOutItems;
    private Boolean isScrolling = false;
    private boolean first = true;
    private ProgressBar pb;
    private RelativeLayout mViewPager;
    private TextView nonet;
    private TextView noposts;
    private ImageView nonetimage;
    private ImageView nopostimage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView emercifyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        //mListView = (ListView) view.findViewById(R.id.listView);
        emercifyText = (ImageView) getActivity().findViewById(R.id.emercify_text);
        recyclerView = (RecyclerView) view.findViewById(R.id.listViewhome);
        pb = (ProgressBar) view.findViewById(R.id.progress_Bar1);
        mViewPager = (RelativeLayout) view.findViewById(R.id.rellayout2);
        nonet = (TextView) view.findViewById(R.id.no_net);
        noposts = (TextView) view.findViewById(R.id.no_postavail);
        nonetimage = (ImageView) view.findViewById(R.id.no_netimage);
        nopostimage = (ImageView) view.findViewById(R.id.nopost_image);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        
        emercifyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: emercifyText");
                RecyclerView.SmoothScroller smoothScroller = new
                        LinearSmoothScroller(getActivity()) {
                            @Override protected int getVerticalSnapPreference() {
                                return LinearSmoothScroller.SNAP_TO_START;
                            }
                        };
                smoothScroller.setTargetPosition(0);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
                        .getLayoutManager();
                if(layoutManager.findFirstCompletelyVisibleItemPosition()==0 ||
                        layoutManager.findFirstVisibleItemPosition()==0){
                    swipeRefreshLayout.setRefreshing(true);
                    getFollowing();
                }
                else{
                    layoutManager.startSmoothScroll(smoothScroller);
                }

            }
        });
        
        class Task extends AsyncTask<String, Integer, Boolean> {
            @Override
            protected void onPreExecute() {
                pb.setVisibility(View.VISIBLE);
                nonet.setVisibility(View.GONE);
                noposts.setVisibility(View.GONE);
                nonetimage.setVisibility(View.GONE);
                nopostimage.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(Boolean result) {
                ConnectivityManager connMgr = (ConnectivityManager) getActivity()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                    getFollowing();
                } else {
                    pb.setVisibility(View.GONE);
                    nonet.setVisibility(View.VISIBLE);
                    nonetimage.setVisibility(View.VISIBLE);
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
        new Task().execute();
        return view;

    }

    public int getItemCount() {
        if (mPhotos.size() == 0) {
            noposts.setVisibility(View.VISIBLE);
            nopostimage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return 0;
        }else
        noposts.setVisibility(View.GONE);
        nopostimage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        return  recyclerView.getChildCount();
    }


    public void updateMainFeed(int position){
        mPaginatedPhotos.remove(position);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void getFollowing(){
        mFollowing = new ArrayList<>();
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
        mPhotos = new ArrayList<>();
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
                        photo.setType(objectMap.get("type").toString());

                        Log.d(TAG, "onDataChange: " + photo.toString());

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

                recyclerAdapter = new MainfeedRecyclerAdapter(mPaginatedPhotos,HomeFragment.this);
                recyclerView.setAdapter(recyclerAdapter);
                manager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(manager);
                swipeRefreshLayout.setRefreshing(false);
                Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.line_divider);
                if(first){
                    recyclerView.addItemDecoration(new RecyclerViewDivider(
                            dividerDrawable
                    ));
                    first = false;
                }
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            displayMorePhotos();
                        }
                    }
                });
                pb.setVisibility(View.GONE);
                getItemCount();
            }catch(NullPointerException e){
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
            }catch(IndexOutOfBoundsException e){
                swipeRefreshLayout.setRefreshing(false);
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
                recyclerAdapter.notifyDataSetChanged();

            }
        }catch(NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
        }catch(IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException" + e.getMessage() );
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        getFollowing();
    }
}
