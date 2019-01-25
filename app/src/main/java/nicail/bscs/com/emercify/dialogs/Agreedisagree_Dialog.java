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
//        TextView terms1 = findViewById(R.id.terms1);
//        terms1.setText(" By downloading or using the app, these terms will automatically apply to you – you should make sure therefore that you read them carefully before using the app. You’re not allowed to copy, or modify the app, any part of the app, or our trademarks in any way. You’re not allowed to attempt to extract the source code of the app, and you also shouldn’t try to translate the app into other languages, or make derivative versions. The app itself, and all the trade marks, copyright, database rights and other intellectual property rights related to it, still belong to Computer Science Students.\n" +
//                "\n" +
//                "    Computer Science Students is committed to ensuring that the app is as useful and efficient as possible. For that reason, we reserve the right to make changes to the app or to charge for its services, at any time and for any reason. We will never charge you for the app or its services without making it very clear to you exactly what you’re paying for.\n" +
//                "\n" +
//                "    The Emercify app stores and processes personal data that you have provided to us, in order to provide my Service. It’s your responsibility to keep your phone and access to the app secure. We therefore recommend that you do not jailbreak or root your phone, which is the process of removing software restrictions and limitations imposed by the official operating system of your device. It could make your phone vulnerable to malware/viruses/malicious programs, compromise your phone’s security features and it could mean that the Emercify app won’t work properly or at all.\n" +
//                "\n" +
//                "    You should be aware that there are certain things that Computer Science Students will not take responsibility for. Certain functions of the app will require the app to have an active internet connection. The connection can be Wi-Fi, or provided by your mobile network provider, but Computer Science Students cannot take responsibility for the app not working at full functionality if you don’t have access to Wi-Fi, and you don’t have any of your data allowance left.\n" +
//                "\n" +
//                "    If you’re using the app outside of an area with Wi-Fi, you should remember that your terms of the agreement with your mobile network provider will still apply. As a result, you may be charged by your mobile provider for the cost of data for the duration of the connection while accessing the app, or other third party charges. In using the app, you’re accepting responsibility for any such charges, including roaming data charges if you use the app outside of your home territory (i.e. region or country) without turning off data roaming. If you are not the bill payer for the device on which you’re using the app, please be aware that we assume that you have received permission from the bill payer for using the app.\n" +
//                "\n" +
//                "    Along the same lines, Computer Science Students cannot always take responsibility for the way you use the app i.e. You need to make sure that your device stays charged – if it runs out of battery and you can’t turn it on to avail the Service, Computer Science Students cannot accept responsibility.\n" +
//                "\n" +
//                "    With respect to Computer Science Students’s responsibility for your use of the app, when you’re using the app, it’s important to bear in mind that although we endeavour to ensure that it is updated and correct at all times, we do rely on third parties to provide information to us so that we can make it available to you. Computer Science Students accepts no liability for any loss, direct or indirect, you experience as a result of relying wholly on this functionality of the app.\n" +
//                "\n" +
//                "    At some point, we may wish to update the app. The app is currently available on Android – the requirements for system(and for any additional systems we decide to extend the availability of the app to) may change, and you’ll need to download the updates if you want to keep using the app. Computer Science Students does not promise that it will always update the app so that it is relevant to you and/or works with the Android version that you have installed on your device. However, you promise to always accept updates to the application when offered to you, We may also wish to stop providing the app, and may terminate use of it at any time without giving notice of termination to you. Unless we tell you otherwise, upon any termination, (a) the rights and licenses granted to you in these terms will end; (b) you must stop using the app, and (if needed) delete it from your device.\n" +
//                "\n" +
//                "Changes to This Terms and Conditions\n" +
//                "\n" +
//                "    I may update our Terms and Conditions from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Terms and Conditions on this page. These changes are effective immediately after they are posted on this page.\n" +
//                "\n" +
//                "Contact Us\n" +
//                "\n" +
//                "    If you have any questions or suggestions about my Terms and Conditions, do not hesitate to contact me.\n");
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
