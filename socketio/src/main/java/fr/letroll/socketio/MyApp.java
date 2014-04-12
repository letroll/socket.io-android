package fr.letroll.socketio;

import android.app.Application;
import android.content.Context;

/**
 * Created by letroll on 22/02/2014.
 */
public class MyApp extends Application{

    public static Context INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }
}
