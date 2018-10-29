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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddGiftActivity extends AppCompatActivity {

    private DatabaseReference mGiftsRef;
    private FirebaseAuth mAuth;

    private EditText mGifNameInput, mPriceInput, mDescInput;
    private Switch mBoughtSwitch;

    private ProgressDialog mAddingPD;

    private ImageView mImageOneIv, mImageTwoIv, mImageThreeIv;

    private Bitmap mCompressedImageBitmap;
    private Uri mResultUri = null;

    private StorageReference mImagesStorage, mBitmapsStorage;

    private String mBitmapDownloadUrl = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gift);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a Gift");

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

        mAddingPD = new ProgressDialog(this);
        mAddingPD.setTitle("Adding Gift");
        mAddingPD.setMessage("Processing...");
        mAddingPD.setCancelable(false);

        mImageOneIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddGiftActivity.this);

            }
        });


        mImageTwoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mImageThreeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    private void saveGift() {

        final String giftName = mGifNameInput.getText().toString().trim();
        final String priceString = mPriceInput.getText().toString().trim();
        final String desc = mDescInput.getText().toString();

        final Boolean boughtState = mBoughtSwitch.isChecked();

        if (TextUtils.isEmpty(giftName)) {
            mGifNameInput.setError("Kindly enter gift name");
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            mPriceInput.setError("Kindly enter the price");
            return;
        }

        if (!TextUtils.isEmpty(giftName) && !TextUtils.isEmpty(priceString)) {

            mAddingPD.show();

            final String giftKey = mGiftsRef.push().getKey().toString().trim();

            final Map giftMap = new HashMap();
            giftMap.put("giftName", giftName);
            giftMap.put("giftPrice", Double.parseDouble(priceString));
            giftMap.put("description", desc);
            giftMap.put("bought", boughtState);
            giftMap.put("totalAmount", 0);
            giftMap.put("imageUrl", "default");
            giftMap.put("bitmapUrl", "default");

            mGiftsRef.child(giftKey).setValue(giftMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        mAddingPD.dismiss();

                        Intent mainIntent = new Intent(AddGiftActivity.this, GiftsListActivity.class);
                        mainIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                        mainIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                        startActivity(mainIntent);
                        finish();

                    } else {
                        mAddingPD.dismiss();

                        Toast.makeText(AddGiftActivity.this, "Failed to save gift. Try Again", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mResultUri = result.getUri();

                File thumbPath = new File(mResultUri.getPath());

                try {
                    mCompressedImageBitmap = new Compressor(AddGiftActivity.this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbPath);

                    mImageOneIv.setImageBitmap(mCompressedImageBitmap);
                    mImageTwoIv.setImageBitmap(mCompressedImageBitmap);
                    mImageThreeIv.setImageBitmap(mCompressedImageBitmap);
                } catch (IOException e) {

                    e.printStackTrace();

                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

}
