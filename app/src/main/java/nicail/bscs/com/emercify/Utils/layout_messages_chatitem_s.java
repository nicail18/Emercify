package nicail.bscs.com.emercify.Utils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import nicail.bscs.com.emercify.R;

public class layout_messages_chatitem_s extends AppCompatActivity {

    ListView mListView;
    int[] images = {R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3,
            R.drawable.image4,
            R.drawable.image5,
            R.drawable.image6,
            R.drawable.image7,
            R.drawable.image8,
            R.drawable.image9,
            R.drawable.image10};

    String[] Names = {"Curry",
            "James",
            "Jordan",
            "Bryant",
            "O'Neal",
            "Durant",
            "Harden",
            "Antetokounmpo",
            "Westbrook",
            "Rose"
    };

    String[] type = {
            "receiver",
            "receiver",
            "sender",
            "sender",
            "receiver",
            "sender",
            "receiver",
            "sender",
            "sender",
            "receiver",
    };

    String[] Send = {"tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
            "tanginamotanginamotangina",
    };

    String[] time = {"10:30",
            "10:30",
            "10:30",
            "10:30",
            "10:30",
            "10:30",
            "10:30",
            "10:30",
            "10:30",
            "10:30",
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_messages_chatitem_s);

        mListView = (ListView) findViewById(R.id.chat_listview);
        CustomAdapter customAdapter = new CustomAdapter();
        mListView.setAdapter(customAdapter);

    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = getLayoutInflater().inflate(R.layout.layout_messages_sender, null);
            if(type[position].equals("receiver")){
                view = getLayoutInflater().inflate(R.layout.layout_messages_receiver, null);
            }
            else{
                view = getLayoutInflater().inflate(R.layout.layout_messages_sender, null);
            }
            ImageView mImageView =  (ImageView) view.findViewById(R.id.chat_image);
            TextView mTextView = (TextView) view.findViewById(R.id.chat_name);
            TextView mTextView1 = (TextView) view.findViewById(R.id.chat_user);
            TextView mTextView2 = (TextView) view.findViewById(R.id.chat_time);
            mImageView.setImageResource(images[position]);
            mTextView.setText(Names[position]);
            mTextView1.setText(Send[position]);
            mTextView2.setText(time[position]);
            return view;
        }
    }
}
