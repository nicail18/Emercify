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

import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.MessageListAdapter;
import nicail.bscs.com.emercify.models.Message;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";

    private ArrayList<Message> mMessage;
    private MessageListAdapter mAdapter;
    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages,container,false);
        mListView = (ListView) view.findViewById(R.id.listmessages);
        mMessage = new ArrayList<>();

        getMessages();

        return view;
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
