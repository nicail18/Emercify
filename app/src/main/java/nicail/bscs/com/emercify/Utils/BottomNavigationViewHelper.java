package nicail.bscs.com.emercify.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import nicail.bscs.com.emercify.Home.HomeActivity;
import nicail.bscs.com.emercify.Likes.LikesActivity;
import nicail.bscs.com.emercify.Profile.ProfileActivity;
import nicail.bscs.com.emercify.R;
import nicail.bscs.com.emercify.Search.SearchActivity;
import nicail.bscs.com.emercify.Share.ShareActivity;

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";


    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        Log.d(TAG, "setupBottomNavigationView: Bottom Setting up NavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, final Activity callingActivty, BottomNavigationViewEx view, final int incoming){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.ic_house:
                        Intent intent1 = new Intent(context, HomeActivity.class);
                        context.startActivity(intent1);
                        callingActivty.overridePendingTransition(R.anim.left_to_right,R.anim.left_to_right);
                        break;
                    case R.id.ic_search:
                        Intent intent2 = new Intent(context, SearchActivity.class);
                        intent2.putExtra("search","search");
                        context.startActivity(intent2);
                        if(incoming == 0){
                            callingActivty.overridePendingTransition(R.anim.right_to_left,R.anim.right_to_left);
                        }
                        else if( incoming > 1){
                            callingActivty.overridePendingTransition(R.anim.left_to_right,R.anim.left_to_right);
                        }
                        break;
                    case R.id.ic_circle:
                        Intent intent3 = new Intent(context, ShareActivity.class);
                        intent3.putExtra("circle", "cicle");
                        context.startActivity(intent3);
                        callingActivty.overridePendingTransition(R.anim.left_to_right,R.anim.left_to_right);
                        break;
                    case R.id.ic_alert:
                        Intent intent4 = new Intent(context, LikesActivity.class);
                        intent4.putExtra("alert","alert");
                        context.startActivity(intent4);
                        if(incoming == 4){
                            callingActivty.overridePendingTransition(R.anim.left_to_right,R.anim.left_to_right);
                        }
                        else if(incoming < 3){
                            callingActivty.overridePendingTransition(R.anim.right_to_left,R.anim.right_to_left);
                        }
                        break;
                    case R.id.ic_android:
                        Intent intent5 = new Intent(context, ProfileActivity.class);
                        intent5.putExtra("android","android");
                        context.startActivity(intent5);
                        callingActivty.overridePendingTransition(R.anim.right_to_left,R.anim.right_to_left);
                        break;
                }

                return false;
            }
        });
    }
}
