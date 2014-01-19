package com.hulq.ImgurCam;


import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.os.FileObserver;
import android.content.Context;
import android.util.Log;

/**
 * Created by raianhuq on 2014-01-14.
 */
public class ImgurCamApplication extends Application {

    private FileObserver observer;
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        ImgurCamApplication.context = getApplicationContext();
        startService(new Intent(this, ScreenshotService.class));
    }//end of onCreate()

    public void setObserver(FileObserver observer){
        this.observer = observer;
        observer.startWatching();
    }

    public static Context getAppContext() {
        return ImgurCamApplication.context;
    }
}//end of ImgurCamApplication
