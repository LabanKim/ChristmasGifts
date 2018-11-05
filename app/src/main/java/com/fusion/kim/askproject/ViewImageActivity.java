package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    //declare member variables

    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        //Set the title of the action bar
        getSupportActionBar().setTitle("Images");

        //initialize the member variables
        mImage = findViewById(R.id.iv_image_view);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //load the image passed to from the previous activity
        Picasso.get().load(getIntent().getStringExtra("image")).into(mImage);
        progressDialog.dismiss();
    }
}
