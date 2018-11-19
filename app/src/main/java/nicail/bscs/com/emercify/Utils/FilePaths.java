package nicail.bscs.com.emercify.Utils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

public class FilePaths {

    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/camera";

    private static final String TAG = "FilePaths";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";

    public ArrayList<String> getFilePaths(Activity context)
    {


        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri u1 = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN ;
        Cursor c = null;
        Cursor c1 = null;
        SortedSet<String> dirList = new TreeSet<String>();
        ArrayList<String> resultIAV = new ArrayList<String>();
        ArrayList<File> filepaths = new ArrayList<File>();

        String[] directories = null;
        if (u != null)
        {
            c = context.managedQuery(u, projection, null, null, orderBy);
            c1 = context.managedQuery(u1,projection,null,null,orderBy);
        }

        if ((c1 != null) && (c1.moveToFirst()))
        {
            do
            {
                String tempDir = c1.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try{
                    dirList.add(tempDir);
                }
                catch(Exception e)
                {

                }
            }
            while (c.moveToNext());

        }

        if ((c != null) && (c.moveToFirst()))
        {
            do
            {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try{
                    dirList.add(tempDir);
                }
                catch(Exception e)
                {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for(int i=0;i<dirList.size();i++)
        {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if(imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if(imagePath.isDirectory())
                    {
                        imageList = imagePath.listFiles();

                    }
                    if ( imagePath.getName().contains(".jpg")|| imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg")|| imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                            )
                    {



                        String path= imagePath.getAbsolutePath();
                        File file = new File(path);
                        Date lastModDate = new Date(file.lastModified());
                        Log.d(TAG, "getFilePaths: file last modified is " + lastModDate.toString());
                        resultIAV.add(path);
                        filepaths.add(file);

                    }
                }
                //  }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Collections.sort(filepaths);


        return resultIAV;


    }

}
