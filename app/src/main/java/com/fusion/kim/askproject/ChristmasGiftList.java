package com.fusion.kim.askproject;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;


//The application class to set offline persistence of data in firebase

public class ChristmasGiftList extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        //set offline persistence of data so that the user can use the app without internet connectivity
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //Synchronize the offline data with the online data
        FirebaseDatabase.getInstance().getReference().keepSynced(true);

    }
}
