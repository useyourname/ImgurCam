package com.hulq.ImgurCam;

/**
 * Created by raianhuq on 2014-01-18.
 */

import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.content.Intent;
import android.app.Service;

import java.io.File;

public class ScreenshotService extends Service{

    private FileObserver fileObserver;

    public IBinder onBind(Intent i){
        return null;
    }

    public void onCreate()
    {
        this.fileObserver = new FileObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots/")
        {
            public void onEvent(int event, String path)
            {
                if ((event == FileObserver.CLOSE_WRITE)){
                    Uri photoUri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots/" + path));
                    new ImgurUploadTask(photoUri, ImgurCamApplication.getAppContext()).execute();
                }
            }
        };
        this.fileObserver.startWatching();
    }//end of onCreate()

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }
}//end of ScreenshotService

