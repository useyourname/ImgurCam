package com.hulq.ImgurCam;

/**
 * Created by raianhuq on 2014-01-18.
 */

import android.app.Activity;
import android.app.Notification;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;
import android.app.Service;

import java.io.File;

public class ScreenshotService extends Service{

    private FileObserver fileObserver;

    public IBinder onBind(Intent i){
        return null;
    }

    public void onCreate()
    {
        Log.d("Screenshot", "ScreenShotListenerStarted");
        startForeground(1, new Notification());
        this.fileObserver = new FileObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots/")
        {
            public void onEvent(int event, String path)
            {
                Log.d("Screenshot", "new screenshot " + path);
//                Intent localIntent = new Intent(ScreenshotListener.this, Upload.class);
//                localIntent.setAction("android.intent.action.SEND");
//                localIntent.putExtra("nickelme.PuushForAndroid.IsFromApp", true);
//                localIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots/" + paramAnonymousString)));
//                localIntent.addFlags(268435456);
//                ScreenshotService.this.startService(localIntent);
                Uri photoUri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots/" + path));
                new ImgurUploadTask(photoUri, (Activity)ImgurCamApplication.getAppContext()).execute();
            }
        };
        this.fileObserver.startWatching();
    }
}
