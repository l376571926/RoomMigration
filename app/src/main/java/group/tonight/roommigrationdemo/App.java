package group.tonight.roommigrationdemo;

import android.app.Application;

import com.facebook.stetho.Stetho;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        CustomActivityOnCrash.install(this);
    }
}