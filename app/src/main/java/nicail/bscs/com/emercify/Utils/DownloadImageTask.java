package nicail.bscs.com.emercify.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private String image_path;

    public DownloadImageTask(String image_path) {
        this.image_path = image_path;
    }

    public DownloadImageTask() {

    }

    @Override
    public Bitmap doInBackground(String... urls) {
        final String url = urls[0];
        Bitmap bitmap = null;

        try {
            final InputStream inputStream = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (final MalformedURLException malformedUrlException) {
            // Handle error
        } catch (final IOException ioException) {
            // Handle error
        }
        return bitmap;
    }

    @Override
    public void onPostExecute(Bitmap bitmap) {
    }
}


