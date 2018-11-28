package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ViewGiftActivity extends AppCompatActivity {

    //declare member variables

    private DatabaseReference mGiftsRef;
    private FirebaseAuth mAuth;

    private EditText mGifNameInput, mPriceInput, mDescInput;
    private Switch mBoughtSwitch;

    private ProgressDialog mUpdatingPD;
    private ProgressBar mLoadingDetailsPB, mLoadImageOnePb, mLoadImageTwoPb, mLoadImageThreePb;

    private ImageView mImageOneIv, mImageTwoIv, mImageThreeIv, mReloadOne, mReloadTwo, mReloadThree;
    private ProgressDialog mProgress;

    private String imageOne = null, imageTwo = null, imageThree = null;

    //if images are loaded successfully
    private boolean mOneLoaded = false, mTwoLoaded = false, mThreeLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gift);

        //initialize member variables

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading...");
        mProgress.setCancelable(false);

        //get data sent from the previous activity
        Bundle data = getIntent().getExtras();
        final String giftName = data.getString("giftName");
        final String description = data.getString("description");
        final double giftPrice = data.getDouble("giftPrice");
        final boolean bought = data.getBoolean("bought");
        final String giftKey = data.getString("giftKey");

        //set the title of the actionbar as the gift name
        getSupportActionBar().setTitle(giftName);

        mAuth = FirebaseAuth.getInstance();
        mGiftsRef = FirebaseDatabase.getInstance().getReference().child("GiftsList")
                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID")).child(giftKey);
        mGiftsRef.keepSynced(true);

        mGifNameInput = findViewById(R.id.input_view_gift_name);
        mPriceInput = findViewById(R.id.input_view_gift_price);
        mDescInput = findViewById(R.id.input_view_gift_desc);
        mBoughtSwitch = findViewById(R.id.switch_view_bought);
        mLoadingDetailsPB = findViewById(R.id.pb_loading_details);
        mReloadOne = findViewById(R.id.iv_refresh_one);
        mReloadTwo = findViewById(R.id.iv_refresh_two);
        mReloadThree = findViewById(R.id.iv_refresh_three);

        imageOne = getIntent().getStringExtra("imageOne");
        imageTwo = getIntent().getStringExtra("imageTwo");
        imageThree = getIntent().getStringExtra("imageThree");

        mLoadingDetailsPB.setVisibility(View.GONE);
        mLoadImageOnePb = findViewById(R.id.pb_load_one);
        mLoadImageTwoPb = findViewById(R.id.pb_load_two);
        mLoadImageThreePb = findViewById(R.id.pb_load_three);

        mImageOneIv = findViewById(R.id.image_one_gift);
        mImageTwoIv = findViewById(R.id.image_two_gift);
        mImageThreeIv = findViewById(R.id.image_three_gift);

        mGifNameInput.setText(giftName);
        mPriceInput.setText(String.valueOf(giftPrice));
        mDescInput.setText(description);

        //load images
        //loadImages();

        mLoadImageOnePb.setVisibility(View.VISIBLE);

        RequestOptions requestOption = new RequestOptions()
                .placeholder(R.drawable.placeholder_image_logo).centerCrop();
        Glide.with(this).load(getIntent().getStringExtra("imageOne"))
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOption)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mLoadImageOnePb.setVisibility(View.GONE);
                        mImageOneIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image_logo));
                        mReloadOne.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mLoadImageOnePb.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImageOneIv);

        mLoadImageTwoPb.setVisibility(View.VISIBLE);

        Glide.with(this).load(getIntent().getStringExtra("imageTwo"))
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOption)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mLoadImageTwoPb.setVisibility(View.GONE);
                        mImageTwoIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image_logo));
                        mReloadTwo.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mLoadImageTwoPb.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImageTwoIv);

        mLoadImageThreePb.setVisibility(View.VISIBLE);

        Glide.with(this).load(getIntent().getStringExtra("imageThree"))
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOption)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mLoadImageThreePb.setVisibility(View.GONE);
                        mImageThreeIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image_logo));
                        mReloadThree.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mLoadImageThreePb.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImageThreeIv);

        //reload images if the fail to load
        mReloadOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadImageOne();
            }
        });

        mReloadTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadImageTwo();
            }
        });

        mReloadThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadImageThree();
            }
        });

        //add click listener to the first image
        mImageOneIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    //send the user to view the image
                    Intent viewIntent = new Intent(ViewGiftActivity.this, ViewImageActivity.class);
                    viewIntent.putExtra("image", getIntent().getStringExtra("imageOne"));
                    viewIntent.putExtra("imageTwo", getIntent().getStringExtra("imageTwo"));
                    viewIntent.putExtra("imageThree", getIntent().getStringExtra("imageThree"));
                    viewIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                    viewIntent.putExtra("giftKey", giftKey);
                    viewIntent.putExtra("giftName", giftName);
                    viewIntent.putExtra("description", description);
                    viewIntent.putExtra("giftPrice", giftPrice);
                    viewIntent.putExtra("bought", bought);
                    viewIntent.putExtra("imageName", "image1");
                    startActivity(viewIntent);

                } catch (Exception e){
                    System.err.println("Failed: "+ e.getLocalizedMessage());
                }

            }
        });

        //add click listener to the second image
        mImageTwoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent viewIntent = new Intent(ViewGiftActivity.this, ViewImageActivity.class);
                    viewIntent.putExtra("image", getIntent().getStringExtra("imageTwo"));
                    viewIntent.putExtra("imageOne", getIntent().getStringExtra("imageOne"));
                    viewIntent.putExtra("imageThree", getIntent().getStringExtra("imageThree"));
                    viewIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                    viewIntent.putExtra("giftKey", giftKey);
                    viewIntent.putExtra("giftName", giftName);
                    viewIntent.putExtra("description", description);
                    viewIntent.putExtra("giftPrice", giftPrice);
                    viewIntent.putExtra("bought", bought);
                    viewIntent.putExtra("imageName", "image2");
                    startActivity(viewIntent);
                } catch (Exception e){
                    System.err.println("Failed: "+ e.getLocalizedMessage());
                }

            }
        });

        //add click listener to the third image
        mImageThreeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{

                    Intent viewIntent = new Intent(ViewGiftActivity.this, ViewImageActivity.class);
                    viewIntent.putExtra("image", getIntent().getStringExtra("imageThree"));
                    viewIntent.putExtra("imageOne", getIntent().getStringExtra("imageOne"));
                    viewIntent.putExtra("imageTwo", getIntent().getStringExtra("imageTwo"));
                    viewIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                    viewIntent.putExtra("giftKey", giftKey);
                    viewIntent.putExtra("giftName", giftName);
                    viewIntent.putExtra("description", description);
                    viewIntent.putExtra("giftPrice", giftPrice);
                    viewIntent.putExtra("bought", bought);
                    viewIntent.putExtra("imageName", "image3");
                    startActivity(viewIntent);

                } catch (Exception e){
                    System.err.println("Failed: "+ e.getLocalizedMessage());
                }

            }
        });

        //check if the gift is bought or not
        if (bought){

            //if the gift is bought set the bought switch to true
            mBoughtSwitch.setChecked(true);

        } else {
            //if the gift is not bought set the bought switch to false
            mBoughtSwitch.setChecked(false);
        }

        mUpdatingPD = new ProgressDialog(this);
        mUpdatingPD.setTitle("Updating Gift");
        mUpdatingPD.setMessage("Processing...");
        mUpdatingPD.setCancelable(false);

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

            updateGift();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadImageOne(){

        mLoadImageOnePb.setVisibility(View.VISIBLE);
        mReloadOne.setVisibility(View.GONE);

        RequestOptions requestOption = new RequestOptions()
                .placeholder(R.drawable.placeholder_image_logo).centerCrop();
        Glide.with(this).load(getIntent().getStringExtra("imageOne"))
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOption)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mLoadImageOnePb.setVisibility(View.GONE);
                        mImageOneIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image_logo));
                        mReloadOne.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mLoadImageOnePb.setVisibility(View.GONE);
                        mReloadOne.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImageOneIv);

    }

    private void reloadImageTwo(){

        mLoadImageTwoPb.setVisibility(View.VISIBLE);
        mReloadTwo.setVisibility(View.GONE);

        RequestOptions requestOption = new RequestOptions()
                .placeholder(R.drawable.placeholder_image_logo).centerCrop();

        Glide.with(this).load(getIntent().getStringExtra("imageTwo"))
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOption)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mLoadImageTwoPb.setVisibility(View.GONE);
                        mImageTwoIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image_logo));
                        mReloadTwo.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mLoadImageTwoPb.setVisibility(View.GONE);
                        mReloadTwo.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImageTwoIv);

    }

    private void reloadImageThree(){

        mLoadImageThreePb.setVisibility(View.VISIBLE);
        mReloadThree.setVisibility(View.GONE);

        RequestOptions requestOption = new RequestOptions()
                .placeholder(R.drawable.placeholder_image_logo).centerCrop();

        Glide.with(this).load(getIntent().getStringExtra("imageThree"))
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOption)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mLoadImageThreePb.setVisibility(View.GONE);
                        mImageThreeIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_image_logo));
                        mReloadThree.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mLoadImageThreePb.setVisibility(View.GONE);
                        mReloadThree.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImageThreeIv);

    }

    //method to update data about the gift
    private void updateGift() {

        //retrieve user input
        String giftName = mGifNameInput.getText().toString().trim();
        final String priceString = mPriceInput.getText().toString().trim();
        String desc = mDescInput.getText().toString();

        final Boolean boughtState = mBoughtSwitch.isChecked();

        if (TextUtils.isEmpty(giftName)){
            mGifNameInput.setError("Kindly enter gift name");
            return;
        }

        if (TextUtils.isEmpty(priceString)){
            mPriceInput.setError("Kindly enter the price");
            return;
        }


        if (!TextUtils.isEmpty(giftName) && !TextUtils.isEmpty(priceString)){

            mUpdatingPD.show();

            //create a hashmap to store data about the gift
            final Map giftMap = new HashMap();
            giftMap.put("giftName", giftName);
            giftMap.put("giftPrice", Double.parseDouble(priceString));
            giftMap.put("description", desc);
            giftMap.put("bought", boughtState);
            giftMap.put("imageOne", imageOne);
            giftMap.put("imageTwo", imageTwo);
            giftMap.put("imageThree", imageThree);

            if (boughtState){

                //if the gift is bought then change the state ofv the person as bought

                FirebaseDatabase.getInstance().getReference().child("PeopleList").child(mAuth.getCurrentUser().getUid())
                        .child(getIntent().getStringExtra("personID")).child("bought")
                        .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            //if the value change was successful then upload the hashmap

                            mGiftsRef.setValue(giftMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        //if the upload was successful
                                        mUpdatingPD.dismiss();

                                        //display a success message to the user
                                        Toast.makeText(ViewGiftActivity.this, "Details Updated", Toast.LENGTH_LONG).show();

                                        //send the user to the gift list activity
                                        Intent mainIntent = new Intent(ViewGiftActivity.this, GiftsListActivity.class);
                                        mainIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                                        mainIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                                        startActivity(mainIntent);
                                        finish();


                                    } else {

                                        Toast.makeText(ViewGiftActivity.this, "Failed Update Amount", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });

                        } else {

                            Toast.makeText(ViewGiftActivity.this, "Failed to update data", Toast.LENGTH_LONG).show();

                        }

                    }
                });

            } else {

                //if the gift is not bought then upload the data directly to firebase
                mGiftsRef.setValue(giftMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            mUpdatingPD.dismiss();

                            Toast.makeText(ViewGiftActivity.this, "Details Updated", Toast.LENGTH_LONG).show();

                            //send the user to the gifts list activity
                            Intent mainIntent = new Intent(ViewGiftActivity.this, GiftsListActivity.class);
                            mainIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                            mainIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                            startActivity(mainIntent);
                            finish();


                        } else {

                            Toast.makeText(ViewGiftActivity.this, "Failed Update Amount", Toast.LENGTH_LONG).show();

                        }

                    }
                });

            }


        }

    }


    //override the back method to send data back to the previous activity through the intent
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent backIntent = new Intent(ViewGiftActivity.this, GiftsListActivity.class);
        backIntent.putExtra("personID", getIntent().getStringExtra("personID"));
        backIntent.putExtra("personName", getIntent().getStringExtra("personName"));
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(backIntent);
        finish();

    }
}
