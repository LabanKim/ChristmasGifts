package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
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

    private DatabaseReference mGiftsRef;
    private FirebaseAuth mAuth;

    private EditText mGifNameInput, mPriceInput, mDescInput;
    private Switch mBoughtSwitch;

    private ProgressDialog mUpdatingPD;
    private ProgressBar mLoadingDetailsPB;

    private ImageView mImageOneIv, mImageTwoIv, mImageThreeIv;
    private ProgressDialog mProgress;

    private String imageOne = null, imageTwo = null, imageThree = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gift);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading...");
        mProgress.setCancelable(false);

        mProgress.show();

        Bundle data = getIntent().getExtras();
        String giftName = data.getString("giftName");
        String description = data.getString("description");
        double giftPrice = data.getDouble("giftPrice");
        boolean bought = data.getBoolean("bought");
        String giftKey = data.getString("giftKey");
        String personID = data.getString("personID");


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

        imageOne = getIntent().getStringExtra("imageOne");
        imageTwo = getIntent().getStringExtra("imageTwo");
        imageThree = getIntent().getStringExtra("imageThree");

        mLoadingDetailsPB.setVisibility(View.GONE);

        mImageOneIv = findViewById(R.id.image_one_gift);
        mImageTwoIv = findViewById(R.id.image_two_gift);
        mImageThreeIv = findViewById(R.id.image_three_gift);

        mGifNameInput.setText(giftName);
        mPriceInput.setText(String.valueOf(giftPrice));
        mDescInput.setText(description);


        FirebaseDatabase.getInstance().getReference().child("GiftsList").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(personID).child(giftKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Picasso.get().load(getIntent().getStringExtra("imageOne"))
                    .into(mImageOneIv, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                            mProgress.dismiss();

                            Toast.makeText(ViewGiftActivity.this, "Failed to load image "+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                Picasso.get().load(getIntent().getStringExtra("imageTwo"))
                        .into(mImageTwoIv, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                mProgress.dismiss();

                                Toast.makeText(ViewGiftActivity.this, "Failed to load image "+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                Picasso.get().load(getIntent().getStringExtra("imageThree"))
                        .into(mImageThreeIv, new Callback() {
                            @Override
                            public void onSuccess() {

                                mProgress.dismiss();

                            }

                            @Override
                            public void onError(Exception e) {

                                mProgress.dismiss();

                                Toast.makeText(ViewGiftActivity.this, "Failed to load image "+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mImageOneIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(ViewGiftActivity.this, ViewImageActivity.class);
                viewIntent.putExtra("image", getIntent().getStringExtra("imageOne"));
                startActivity(viewIntent);
            }
        });

        mImageTwoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(ViewGiftActivity.this, ViewImageActivity.class);
                viewIntent.putExtra("image", getIntent().getStringExtra("imageTwo"));
                startActivity(viewIntent);
            }
        });

        mImageThreeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(ViewGiftActivity.this, ViewImageActivity.class);
                viewIntent.putExtra("image", getIntent().getStringExtra("imageThree"));
                startActivity(viewIntent);
            }
        });

        if (bought){


            mBoughtSwitch.setChecked(true);

        } else {
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

    private void updateGift() {

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

            final Map giftMap = new HashMap();
            giftMap.put("giftName", giftName);
            giftMap.put("giftPrice", Double.parseDouble(priceString));
            giftMap.put("description", desc);
            giftMap.put("bought", boughtState);
            giftMap.put("imageOne", imageOne);
            giftMap.put("imageTwo", imageTwo);
            giftMap.put("imageThree", imageThree);

            if (boughtState){

                FirebaseDatabase.getInstance().getReference().child("PeopleList").child(mAuth.getCurrentUser().getUid())
                        .child(getIntent().getStringExtra("personID")).child("bought")
                        .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            mGiftsRef.setValue(giftMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        mUpdatingPD.dismiss();

                                        Toast.makeText(ViewGiftActivity.this, "Details Updated", Toast.LENGTH_LONG).show();

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

                mGiftsRef.setValue(giftMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            mUpdatingPD.dismiss();

                            Toast.makeText(ViewGiftActivity.this, "Details Updated", Toast.LENGTH_LONG).show();

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


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent backIntent = new Intent(ViewGiftActivity.this, GiftsListActivity.class);
        backIntent.putExtra("personID", getIntent().getStringExtra("personID"));
        backIntent.putExtra("personName", getIntent().getStringExtra("personName"));
        startActivity(backIntent);
        finish();

    }



}
