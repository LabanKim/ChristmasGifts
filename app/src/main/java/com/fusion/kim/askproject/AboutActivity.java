package com.fusion.kim.askproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// Activity to display info about the app and the developer
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("About");

    }
}
