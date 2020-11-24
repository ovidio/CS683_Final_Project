package com.noyouaint.cs683_ovidio_reyna_weight_tracking_app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

public class FirebaseInitActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
    }
}
