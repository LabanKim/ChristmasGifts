package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddGiftActivity extends AppCompatActivity {

    // create global member variables

    private DatabaseReference mGiftsRef;
    private FirebaseAuth mAuth;

    private EditText mGifNameInput, mPriceInput, mDescInput;
    private Switch mBoughtSwitch;

    private ProgressDialog mAddingPD;

    private ImageView mImageOneIv, mImageTwoIv, mImageThreeIv;

    private Bitmap mCompressedImageBitmapOne, mCompressedImageBitmapTwo, mCompressedImageBitmapThree;
    private Uri mImageOneResultUri = null, mImageTwoResultUri = null,
            mImageThreeResultUri = null, defaultUri;

    private StorageReference mImagesStorage, mBitmapsStorage;


    private int type = 0;

    private Map giftMap;

    private String deadline = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gift);

        // Set the title of the action bar of the activity
        getSupportActionBar().setTitle("Add a Gift");

        //initialize the variables accordingly

        mAuth = FirebaseAuth.getInstance();
        mGiftsRef = FirebaseDatabase.getInstance().getReference().child("GiftsList")
                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID"));
        mGiftsRef.keepSynced(true);

        mImagesStorage = FirebaseStorage.getInstance().getReference().child("GiftImages").child("Images");
        mBitmapsStorage = FirebaseStorage.getInstance().getReference().child("GiftImages").child("Bitmaps");

        mGifNameInput = findViewById(R.id.input_gift_name);
        mPriceInput = findViewById(R.id.input_gift_price);
        mDescInput = findViewById(R.id.input_gift_desc);
        mBoughtSwitch = findViewById(R.id.switch_bought);

        mImageOneIv = findViewById(R.id.image_one);
        mImageTwoIv = findViewById(R.id.image_two);
        mImageThreeIv = findViewById(R.id.image_three);


        //Default image to be uploaded in case the user does not select an image
        defaultUri = Uri.parse("android.resource://com.fusion.kim.askproject/" + R.drawable.placeholder_image_logo);


        mAddingPD = new ProgressDialog(this);
        mAddingPD.setTitle("Adding Gift");
        mAddingPD.setMessage("Processing...");
        mAddingPD.setCancelable(false);


        /*Load person data for purposes of re-writing later when adding a gift so as to avoid accidental
        deletion of data*/
        FirebaseDatabase.getInstance().getReference().child("PeopleList")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getIntent().getStringExtra("personID")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                deadline = dataSnapshot.child("deadline").getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //add onClickListener to the first image holder to open gallery and select an image
        mImageOneIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //type = 1 means that the request to open the gallery came from clicking on the first image
                type = 1;

                //configure and start the activity to open gallery
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddGiftActivity.this);

            }
        });

        //add onClickListener to the second image holder to open gallery and select an image
        mImageTwoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //type = 2 means that the request to open the gallery came from clicking on the second image
                type = 2;

                //configure and start the activity to open gallery
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddGiftActivity.this);

            }
        });

        //add onClickListener to the third image holder to open gallery and select an image
        mImageThreeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //type = 3 means that the request to open the gallery came from clicking on the third image
                type = 3;

                //configure and start the activity to open gallery
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddGiftActivity.this);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_gift_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_gift) {

            saveGift();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //method to handle uploading data about a gift and images
    private void saveGift() {

        //retrieve user input from the input fields
        final String giftName = mGifNameInput.getText().toString().trim();
        final String priceString = mPriceInput.getText().toString().trim();
        final String desc = mDescInput.getText().toString();

        final Boolean boughtState = mBoughtSwitch.isChecked();

        //validate that the user has provided input and if not throw an error
        if (TextUtils.isEmpty(giftName)) {
            mGifNameInput.setError("Kindly enter gift name");
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            mPriceInput.setError("Kindly enter the price");
            return;
        }

        //set the default image to the first image placeholder if the user has not selected any image
        if (mImageOneResultUri == null){

            mImageOneResultUri = defaultUri;

        }

         //set the default image to the second image placeholder if the user has not selected any image
        if (mImageTwoResultUri == null){

            mImageTwoResultUri = defaultUri;

        }

         //set the default image to the third image placeholder if the user has not selected any image
        if (mImageThreeResultUri == null){

            mImageThreeResultUri = defaultUri;

        }

        if (!TextUtils.isEmpty(giftName) && !TextUtils.isEmpty(priceString)) {

            //start uploading gift data
            mAddingPD.show();

            final String giftKey = mGiftsRef.push().getKey().toString().trim();

            //add gift data to a hash map for bulk uploading to firebase
            giftMap = new HashMap();
            giftMap.put("giftName", giftName);
            giftMap.put("giftPrice", Double.parseDouble(priceString));
            giftMap.put("description", desc);
            giftMap.put("bought", boughtState);

            //upload the first image to firebase
            mImagesStorage.child(giftKey).child("image1").putFile(mImageOneResultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    //if upload is successful, start uploading second image
                    if (task.isSuccessful()){

                        //retrieve the path where the image is stored in firebase
                        final String imageOneDownloadUrl = task.getResult().getDownloadUrl().toString();
                        giftMap.put("imageOne", imageOneDownloadUrl);


                        //start uploading second image
                        mImagesStorage.child(giftKey).child("image2").putFile(mImageTwoResultUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                                        //if upload is successful, start uploading third image
                                        if (task.isSuccessful()){

                                            //retrieve the path where the image is stored in firebase
                                            final String imageTwoDownloadUrl = task.getResult().getDownloadUrl().toString();
                                            giftMap.put("imageTwo", imageTwoDownloadUrl);

                                            //start uploading tird image
                                            mImagesStorage.child(giftKey).child("image3").putFile(mImageThreeResultUri)
                                                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                            //if upload is successful, start uploading second image
                                                            if (task.isSuccessful()){

                                                                //retrieve the path where the image is stored in firebase
                                                                final String imageThreeDownloadUrl = task.getResult().getDownloadUrl().toString();
                                                                giftMap.put("imageThree", imageThreeDownloadUrl);

                                                                //start uploading gift data
                                                                mGiftsRef.child(giftKey).setValue(giftMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        //if upload of the gift data was successful
                                                                        if (task.isSuccessful()){

                                                                            //check if the gift not bought
                                                                            if (!boughtState){

                                                                                mAddingPD.dismiss();

                                                                                //all the uploads are finished, send the user to the list of gifts activity

                                                                                Intent mainIntent = new Intent(AddGiftActivity.this, GiftsListActivity.class);
                                                                                mainIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                                                                                mainIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                                                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                startActivity(mainIntent);
                                                                                finish();

                                                                            } else {

                                                                                //if the gift is selected as bought then update the bought status in the person data

                                                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("PeopleList")
                                                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                                        .child(getIntent().getStringExtra("personID"));

                                                                                //create a hashmap to hold data about the person who we are adding the gift to
                                                                                Map map = new HashMap();
                                                                                map.put("bought", true);
                                                                                map.put("deadline", deadline);
                                                                                map.put("personName", getIntent().getStringExtra("personName"));


                                                                                //upload the data to the person
                                                                                ref.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        //if the upload was successful
                                                                                        if (task.isSuccessful()){

                                                                                            mAddingPD.dismiss();

                                                                                            //all the uploads are finished, send the user to the list of gifts activity
                                                                                            Intent mainIntent = new Intent(AddGiftActivity.this, GiftsListActivity.class);
                                                                                            mainIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                                                                                            mainIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                                                                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                            startActivity(mainIntent);
                                                                                            finish();

                                                                                        } else {

                                                                                            Toast.makeText(AddGiftActivity.this, "Failed to save changes", Toast.LENGTH_LONG).show();

                                                                                        }

                                                                                    }
                                                                                });

                                                                            }


                                                                        } else {
                                                                            mAddingPD.dismiss();

                                                                            Toast.makeText(AddGiftActivity.this, "Failed to save gift. Try Again", Toast.LENGTH_LONG).show();
                                                                        }

                                                                    }
                                                                });


                                                            } else {

                                                                mAddingPD.dismiss();

                                                                Toast.makeText(AddGiftActivity.this, "Failed to Upload Third Image", Toast.LENGTH_LONG).show();

                                                            }

                                                        }
                                                    });

                                        } else {

                                            mAddingPD.dismiss();

                                            Toast.makeText(AddGiftActivity.this, "Failed to Upload Second Image", Toast.LENGTH_LONG).show();

                                        }

                                    }
                                });

                    } else {

                        mAddingPD.dismiss();

                        Toast.makeText(AddGiftActivity.this, "Failed to Upload First Image", Toast.LENGTH_LONG).show();


                    }

                }
            });

        }

    }


    //Retrieve the results of the gallery activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //if the result code is correct proceed to retrieve the results
            if (resultCode == RESULT_OK) {

                //if the request that opened the gallery was from first image
                if (type == 1){

                    mImageOneResultUri = result.getUri();

                    File thumbPath = new File(mImageOneResultUri.getPath());

                    //compress the image
                    try {
                        mCompressedImageBitmapOne = new Compressor(AddGiftActivity.this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(75)
                                .compressToBitmap(thumbPath);

                        mImageOneIv.setImageBitmap(mCompressedImageBitmapOne);
                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                } else if (type == 2){
                    //if the request that opened the gallery was from second image
                    mImageTwoResultUri = result.getUri();

                    File thumbPath = new File(mImageTwoResultUri.getPath());

                    //compress the image
                    try {
                        mCompressedImageBitmapTwo = new Compressor(AddGiftActivity.this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(75)
                                .compressToBitmap(thumbPath);

                        mImageTwoIv.setImageBitmap(mCompressedImageBitmapTwo);
                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                } else if (type == 3){

                    //if the request that opened the gallery was from third image
                    mImageThreeResultUri = result.getUri();

                    File thumbPath = new File(mImageThreeResultUri.getPath());

                    //compress the image
                    try {
                        mCompressedImageBitmapThree = new Compressor(AddGiftActivity.this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(75)
                                .compressToBitmap(thumbPath);

                        mImageThreeIv.setImageBitmap(mCompressedImageBitmapThree);
                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

}
