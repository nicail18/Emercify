package nicail.bscs.com.emercify.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.w3c.dom.Text;

import nicail.bscs.com.emercify.R;

public class Agreedisagree_Dialog extends Dialog{
    private static final String TAG = "Agreedisagree_Dialog";

    public Agreedisagree_Dialog(@NonNull Context context) {
        super(context);
    }

    public interface OnAgreeClickListener{
        void onAgreeClickListener();
    }

    public OnAgreeClickListener onAgreeClickListener;

    public void setSetAgreeClickListener(OnAgreeClickListener listener){
        onAgreeClickListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_agreedisagree);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_1);
        CheckBox checkbox1 = (CheckBox) findViewById(R.id.checkbox1);
        RelativeLayout box1 = (RelativeLayout)findViewById(R.id.agreement_box1);
        RelativeLayout box2 = (RelativeLayout) findViewById(R.id.agreediagree_button);
        TextView agreebutton = (TextView) findViewById(R.id.agreeButton);
        TextView disagreebutton = (TextView) findViewById(R.id.disagreeButton);
        TextView textagreement = (TextView) findViewById(R.id.textagreement);
        checkbox1.setAlpha(.5f);
        agreebutton.setAlpha(.5f);
        disagreebutton.setAlpha(.5f);
        textagreement.setAlpha(.5f);
        checkbox1.setEnabled(false);
        agreebutton.setEnabled(false);
        disagreebutton.setEnabled(false);
        textagreement.setEnabled(false);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //int scrollY = scrollView.getScrollY(); // For ScrollView
                // DO SOMETHING WITH THE SCROLL COORDINATES11
                if (scrollView.getChildAt(0).getBottom()
                        <= (scrollView.getHeight() + scrollView.getScrollY())) {
                    //scroll view is at bottom
                    checkbox1.setAlpha(1f);
                    agreebutton.setAlpha(1f);
                    disagreebutton.setAlpha(1f);
                    textagreement.setAlpha(1f);
                    checkbox1.setEnabled(true);
                    agreebutton.setEnabled(true);
                    disagreebutton.setEnabled(true);
                    textagreement.setEnabled(true);
                    if(checkbox1.isChecked()){
                        agreebutton.setEnabled(true);
                        agreebutton.setAlpha(1f);
                    } else {
                        agreebutton.setEnabled(false);
                        agreebutton.setAlpha(.5f);
                    }
                } else {
                    //scroll view is not at bottom
                   /*checkbox1.setAlpha(1f);
                    agreebutton.setAlpha(1f);
                    disagreebutton.setAlpha(1f);
                    textagreement.setAlpha(1f);
                    checkbox1.setEnabled(true);
                    agreebutton.setEnabled(true);
                    disagreebutton.setEnabled(true);
                    textagreement.setEnabled(true);*/
                }
            }
        });
        checkbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkbox1.isChecked()){
                    agreebutton.setEnabled(true);
                    agreebutton.setAlpha(1f);
                }else{
                    agreebutton.setEnabled(false);
                    agreebutton.setAlpha(.5f);
                }
            }
        });

        agreebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAgreeClickListener.onAgreeClickListener();
                dismiss();
            }
        });

        disagreebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                dismiss();
            }
        });
    }

    //    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.dialog_agreedisagree,container,false);
//        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scroll_1);
//        CheckBox checkbox1 = (CheckBox) view.findViewById(R.id.checkbox1);
//        RelativeLayout box1 = (RelativeLayout)view.findViewById(R.id.agreement_box1);
//        RelativeLayout box2 = (RelativeLayout) view.findViewById(R.id.agreediagree_button);
//        Button agreebutton = (Button) view.findViewById(R.id.agreeButton);
//        Button disagreebutton = (Button) view.findViewById(R.id.disagreeButton);
//        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                //int scrollY = scrollView.getScrollY(); // For ScrollView
//                // DO SOMETHING WITH THE SCROLL COORDINATES11
//                if (scrollView.getChildAt(0).getBottom()
//                        <= (scrollView.getHeight() + scrollView.getScrollY())) {
//                    //scroll view is at bottom
//                    checkbox1.setEnabled(true);
//                } else {
//                    //scroll view is not at bottom
//                    checkbox1.setEnabled(false);
//                }
//
//            }
//        });
//        return view;
//    }
}
