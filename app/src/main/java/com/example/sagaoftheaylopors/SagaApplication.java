package com.example.sagaoftheaylopors;

import android.app.Application;

import com.google.firebase.FirebaseApp;

/**
 * Application entry point. Initializes Firebase when google-services.json is present locally.
 */
public class SagaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
    }
}
