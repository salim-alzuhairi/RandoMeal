package com.example.randomeal;


import android.annotation.SuppressLint;
import android.app.Application;
import com.google.android.gms.ads.MobileAds;
import papaya.in.admobopenads.AppOpenManager;

public class open_app extends Application {

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        new AppOpenManager(this, "ca-app-pub-6801840571633600/8425732157");
//        ca-app-pub-3940256099942544/3419835294
    }
}
