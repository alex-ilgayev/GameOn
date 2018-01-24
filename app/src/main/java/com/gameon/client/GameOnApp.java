package com.gameon.client;

import android.app.Activity;
import android.app.Application;

/**
 * Created by Alex on 12/26/2015.
 */
public class GameOnApp extends Application {
    public void onCreate() {
        super.onCreate();
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}
