package nicail.bscs.com.emercify.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;
import nicail.bscs.com.emercify.Home.HomeFragment;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Utils.MainfeedRecyclerAdapter;
import nicail.bscs.com.emercify.Utils.RecyclerViewDivider;
import nicail.bscs.com.emercify.Utils.RespondersRecyclerViewAdapter;
import nicail.bscs.com.emercify.models.Photo;
import nicail.bscs.com.emercify.models.Responder;

public class RespondentsDialog extends DialogFragment {

    private static final String TAG = "RespondentsDialog";
    
    public RespondentsDialog() {
        super();
        setArguments(new Bundle());
    }

    RecyclerView recyclerView;
    ArrayList<Responder> responders;
    ArrayList<Responder> paginatedResponder;
    RespondersRecyclerViewAdapter respondersRecyclerViewAdapter;
    LinearLayoutManager manager;
    TextView noresponders;
    Button closeBtn;
    int results;
    private Boolean isScrolling = false;
    private boolean first = true;
    private int mResults,currentItems,totalItems,scrollOutItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_respondents, container, false);
        getDialog().setCancelable(false);

        recyclerView = (RecyclerView) view.findViewById(R.id.listViewrespondents);
        noresponders = (TextView) view.findViewById(R.id.noresponders);
        closeBtn = (Button) view.findViewById(R.id.closebtn);
        responders = getRespondersFromBundle();
        if(responders.size() != 0){
            dispayResponders();
        }
        else{
            noresponders.setVisibility(View.VISIBLE);
        }

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    private void dispayResponders(){
        paginatedResponder = new ArrayList<>();
        if(responders != null){
            try{
                int iterations = responders.size();
                if(iterations > 10){
                    iterations = 10;
                }
                mResults = 10;
                for(int i = 0; i<iterations; i++){
                    paginatedResponder.add(responders.get(i));
                }

                respondersRecyclerViewAdapter = new RespondersRecyclerViewAdapter(paginatedResponder);
                manager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(respondersRecyclerViewAdapter);
//                Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.line_divider);
//                if(first){
//                    recyclerView.addItemDecoration(new RecyclerViewDivider(
//                            dividerDrawable
//                    ));
//                    first = false;
//                }
//                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                    @Override
//                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                        super.onScrollStateChanged(recyclerView, newState);
//                        if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
//                            isScrolling = true;
//                        }
//                    }
//
//                    @Override
//                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                        super.onScrolled(recyclerView, dx, dy);
//                        currentItems = manager.getChildCount();
//                        totalItems = manager.getItemCount();
//                        scrollOutItems = manager.findFirstVisibleItemPosition();
//
//                        if(isScrolling && (currentItems + scrollOutItems == totalItems)){
//                            isScrolling = false;
//                            displayMoreResponders();
//                        }
//                    }
//                });
            }catch(NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
            }catch(IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException" + e.getMessage() );
            }
        }
    }

    private void displayMoreResponders(){
        try{
            if(responders.size() > mResults && responders.size() > 0){
                int iterations;
                if(responders.size() > (mResults+10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than photos");
                    iterations = responders.size() - mResults;
                }

                for(int i = mResults; i<mResults + iterations; i++){
                    paginatedResponder.add(responders.get(i));
                }
                mResults = mResults + iterations;
                respondersRecyclerViewAdapter.notifyDataSetChanged();

            }
        }catch(NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage() );
        }catch(IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException" + e.getMessage() );
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private ArrayList<Responder> getRespondersFromBundle(){
        Bundle bundle = this.getArguments();
        return bundle.getParcelableArrayList("RESPONDERS");
    }
}
