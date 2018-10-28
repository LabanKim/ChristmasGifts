package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ViewGiftActivity extends AppCompatActivity {

    private DatabaseReference mGiftsRef;
    private FirebaseAuth mAuth;

    private EditText mGifNameInput, mPriceInput, mDescInput;
    private Switch mBoughtSwitch;

    private ProgressDialog mUpdatingPD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gift);

        Bundle data = getIntent().getExtras();
        String giftName = data.getString("giftName");
        String description = data.getString("description");
        double giftPrice = data.getDouble("giftPrice");
        boolean bought = data.getBoolean("bought");
        String giftKey = data.getString("giftKey");

        getSupportActionBar().setTitle(giftName);

        mAuth = FirebaseAuth.getInstance();
        mGiftsRef = FirebaseDatabase.getInstance().getReference().child("GiftsList")
                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID")).child(giftKey);
        mGiftsRef.keepSynced(true);

        mGifNameInput = findViewById(R.id.input_view_gift_name);
        mPriceInput = findViewById(R.id.input_view_gift_price);
        mDescInput = findViewById(R.id.input_view_gift_desc);
        mBoughtSwitch = findViewById(R.id.switch_view_bought);

        mGifNameInput.setText(giftName);
        mPriceInput.setText(String.valueOf(giftPrice));
        mDescInput.setText(description);

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
        String priceString = mPriceInput.getText().toString().trim();
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

            Map giftMap = new HashMap();
            giftMap.put("giftName", giftName);
            giftMap.put("giftPrice", Double.parseDouble(priceString));
            giftMap.put("description", desc);
            giftMap.put("bought", boughtState);

            mGiftsRef.setValue(giftMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        FirebaseDatabase.getInstance().getReference().child("PeopleList")
                                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID"))
                                .child("bought").setValue(boughtState).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){

                                    mUpdatingPD.dismiss();

                                    Intent mainIntent = new Intent(ViewGiftActivity.this, GiftsListActivity.class);
                                    mainIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                                    mainIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                                    startActivity(mainIntent);
                                    finish();

                                    Toast.makeText(ViewGiftActivity.this, "Details Updated", Toast.LENGTH_LONG).show();

                                } else {

                                    Toast.makeText(ViewGiftActivity.this, "Failed to update gift. Try Again", Toast.LENGTH_LONG).show();

                                }

                            }
                        });

                    } else {
                        mUpdatingPD.dismiss();

                        Toast.makeText(ViewGiftActivity.this, "Failed to update gift. Try Again", Toast.LENGTH_LONG).show();
                    }

                }
            });

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
