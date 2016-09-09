package com.rlc.bookshop;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by alexey on 9/9/16.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
