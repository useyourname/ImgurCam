package com.hulq.ImgurCam;

import android.app.Application;
import android.content.Intent;
import android.content.Context;

/**
 * Created by raianhuq on 2014-01-14.
 */
public class ImgurCamApplication extends Application {
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        ImgurCamApplication.context = getApplicationContext();
        startService(new Intent(this, ScreenshotService.class));
    }//end of onCreate()

    public static Context getAppContext() {
        return ImgurCamApplication.context;
    }
}//end of ImgurCamApplication
