package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class ViewImageActivity extends AppCompatActivity {

    //declare member variables

    private ImageView mImageIv;
    private ProgressDialog progressDialog;
    private LinearLayout mEditImageLayout;

    private Bitmap mCompressedImage;
    private Uri mImageResultUri = null;

    private StorageReference mImagesStorage, mBitmapsStorage;

    private DatabaseReference mGiftsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        //Set the title of the action bar
        getSupportActionBar().setTitle("Images");

        mAuth = FirebaseAuth.getInstance();

        mGiftsRef = FirebaseDatabase.getInstance().getReference().child("GiftsList")
                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID"));
        mGiftsRef.keepSynced(true);

        mImagesStorage = FirebaseStorage.getInstance().getReference().child("GiftImages").child("Images");
        mBitmapsStorage = FirebaseStorage.getInstance().getReference().child("GiftImages").child("Bitmaps");

        //initialize the member variables
        mImageIv = findViewById(R.id.iv_image_view);
        mEditImageLayout = findViewById(R.id.layout_edit_image);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //open gallery to select or take a photo using camera
        mEditImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //configure and start the activity to open gallery
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ViewImageActivity.this);

            }
        });

        //load the image passed to from the previous activity


        RequestOptions requestOption = new RequestOptions()
                .placeholder(R.drawable.placeholder_image_logo).centerInside();
        Glide.with(this).load(getIntent().getStringExtra("image"))
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOption)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressDialog.dismiss();
                        mImageIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image_logo));

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressDialog.dismiss();
                        return false;
                    }
                })
                .into(mImageIv);

    }



    //Retrieve the results of the gallery activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //if the result code is correct proceed to retrieve the results
            if (resultCode == RESULT_OK) {

                mImageResultUri = result.getUri();

                File thumbPath = new File(mImageResultUri.getPath());

                //compress the image
                try {
                    mCompressedImage = new Compressor(ViewImageActivity.this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbPath);

                    mImageIv.setImageBitmap(mCompressedImage);
                } catch (IOException e) {

                    e.printStackTrace();

                }

                uploadImage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    private void uploadImage(){

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        //start uploading the image
        mImagesStorage.child(getIntent().getStringExtra("giftKey")).child(getIntent().getStringExtra("imageName")).putFile(mImageResultUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){

                            final String imageDownloadUrl = task.getResult().getDownloadUrl().toString();

                            String imageNode = null;
                            final String imageName = getIntent().getStringExtra("imageName");

                            if (imageName.equals("image1")){

                                imageNode = "imageOne";

                            } else if (imageName.equals("image2")){

                                imageNode = "imageTwo";

                            } else if (imageName.equals("image3")){

                                imageNode = "imageThree";

                            }


                            mGiftsRef.child(getIntent().getStringExtra("giftKey"))
                                    .child(imageNode).setValue(imageDownloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        progressDialog.dismiss();

                                        Toast.makeText(ViewImageActivity.this, "Image Updated Successfully", Toast.LENGTH_LONG).show();

                                        Bundle data = getIntent().getExtras();

                                        final double giftPrice = data.getDouble("giftPrice");
                                        final boolean bought = data.getBoolean("bought");

                                        if (imageName.equals("image1")){

                                            Intent backIntent = new Intent(ViewImageActivity.this, ViewGiftActivity.class);
                                            backIntent.putExtra("imageOne", imageDownloadUrl);
                                            backIntent.putExtra("imageTwo", getIntent().getStringExtra("imageTwo"));
                                            backIntent.putExtra("imageThree", getIntent().getStringExtra("imageThree"));
                                            backIntent.putExtra("personID", data.getString("personID"));
                                            backIntent.putExtra("giftKey", data.getString("giftKey"));
                                            backIntent.putExtra("giftName", data.getString("giftName"));
                                            backIntent.putExtra("description", data.getString("description"));
                                            backIntent.putExtra("giftPrice", giftPrice);
                                            backIntent.putExtra("bought", bought);
                                            startActivity(backIntent);
                                            finish();

                                        } else if (imageName.equals("image2")){

                                            Intent backIntent = new Intent(ViewImageActivity.this, ViewGiftActivity.class);
                                            backIntent.putExtra("imageOne", getIntent().getStringExtra("imageOne"));
                                            backIntent.putExtra("imageTwo", imageDownloadUrl);
                                            backIntent.putExtra("imageThree", getIntent().getStringExtra("imageThree"));
                                            backIntent.putExtra("personID", data.getString("personID"));
                                            backIntent.putExtra("giftKey", data.getString("giftKey"));
                                            backIntent.putExtra("giftName", data.getString("giftName"));
                                            backIntent.putExtra("description", data.getString("description"));
                                            backIntent.putExtra("giftPrice", giftPrice);
                                            backIntent.putExtra("bought", bought);
                                            startActivity(backIntent);
                                            finish();

                                        } else if (imageName.equals("image3")){

                                            Intent backIntent = new Intent(ViewImageActivity.this, ViewGiftActivity.class);
                                            backIntent.putExtra("imageOne", getIntent().getStringExtra("imageOne"));
                                            backIntent.putExtra("imageTwo", getIntent().getStringExtra("imageTwo"));
                                            backIntent.putExtra("imageThree", imageDownloadUrl);
                                            backIntent.putExtra("personID", data.getString("personID"));
                                            backIntent.putExtra("giftKey", data.getString("giftKey"));
                                            backIntent.putExtra("giftName", data.getString("giftName"));
                                            backIntent.putExtra("description", data.getString("description"));
                                            backIntent.putExtra("giftPrice", giftPrice);
                                            backIntent.putExtra("bought", bought);
                                            startActivity(backIntent);
                                            finish();

                                        }

                                    } else {

                                        progressDialog.dismiss();

                                        Toast.makeText(ViewImageActivity.this, "Failed to upload Image. Try again.", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });


                        } else {



                            Toast.makeText(ViewImageActivity.this, "Failed to update the image. Try Again.", Toast.LENGTH_LONG).show();

                        }
                    }
                });

    }


}
