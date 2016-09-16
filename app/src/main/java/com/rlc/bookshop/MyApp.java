package com.rlc.bookshop;

import android.app.Application;
import android.content.res.Resources;

import com.google.firebase.database.FirebaseDatabase;


public class MyApp extends Application {
    public static Resources mResources;
    public static String PACKAGE_NAME;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mResources = getResources();
        PACKAGE_NAME=getPackageName();
    }
}
