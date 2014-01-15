package com.hulq.ImgurCam;

import android.app.Application;
import android.os.FileObserver;

/**
 * Created by raianhuq on 2014-01-14.
 */
public class ImgurCamApplication extends Application {

    private FileObserver observer;

    @Override
    public void onCreate(){
        super.onCreate();
    }//end of onCreate()

    public void setObserver(FileObserver observer){
        this.observer = observer;
    }
}//end of ImgurCamApplication
