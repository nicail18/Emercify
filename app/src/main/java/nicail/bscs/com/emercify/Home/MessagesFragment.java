package nicail.bscs.com.emercify.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.GlideApp;
import nicail.bscs.com.emercify.Utils.MessageListAdapter;
import nicail.bscs.com.emercify.Utils.layout_messages_chatitem_s;
import nicail.bscs.com.emercify.models.Message;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";
    private ArrayList<Message> mMessage;
    private MessageListAdapter mAdapter;
    private ListView mListView;



    String[] Names = {"Curry",
            "James",
            "Jordan",
            "Bryant",
            "O'Neal",
            "Durant",
            "Harden",
            "Antetokounmpo",
            "Westbrook",
            "Rose"};


            String[] Message = {"Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah",
                    "Sir, This is blah"};

            String[] time = {"10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",
                    "10:30 p.m.",};


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages,container,false);
        mListView = (ListView) view.findViewById(R.id.listmessages);
        mMessage = new ArrayList<>();



        CustomAdapter customAdapter = new CustomAdapter();
        mListView.setAdapter(customAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent myIntent = new Intent(view.getContext(), layout_messages_chatitem_s.class);
                    startActivityForResult(myIntent, 0);

            }
        });

        getMessages();

        return view;
    }



    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ResourceType")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.layout_messages_listitem, null);
            ImageView mImageView =  (ImageView) view.findViewById(R.id.profile_photo);
            TextView mTextView = (TextView) view.findViewById(R.id.username);
            TextView mTextView1 = (TextView) view.findViewById(R.id.tvMessage);
            TextView mTextView2 = (TextView) view.findViewById(R.id.timestamp);

            mTextView.setText(Names[position]);
            mTextView1.setText(Message[position]);
            mTextView2.setText(time[position]);

//            GlideApp
//                    .with(getContext())
//                    .load(R.id.profile_photo)
//                    .placeholder(R.mipmap.ic_emercify_launcher)
//                    .error(R.drawable.ic_error)
//                    .centerCrop()
//                    .into(mImageView);

            return view;
        }
    }



    private void getMessages(){
        Log.d(TAG, "getMessages: searching for messages");

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_coversations));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    String key = singleSnapshot.getKey();
                    int found = key.indexOf(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Log.d(TAG, "onDataChange: " + singleSnapshot.getKey());
                    Log.d(TAG, "onDataChange: " + found);
                    if(found  != -1){
                        Log.d(TAG, "onDataChange: " + singleSnapshot.getChildren());
                    }else{

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

