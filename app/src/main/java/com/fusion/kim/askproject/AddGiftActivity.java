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

public class AddGiftActivity extends AppCompatActivity {

    private DatabaseReference mGiftsRef;
    private FirebaseAuth mAuth;

    private EditText mGifNameInput, mPriceInput, mDescInput;
    private Switch mBoughtSwitch;

    private ProgressDialog mAddingPD;


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

        mGifNameInput = findViewById(R.id.input_gift_name);
        mPriceInput = findViewById(R.id.input_gift_price);
        mDescInput = findViewById(R.id.input_gift_desc);
        mBoughtSwitch = findViewById(R.id.switch_bought);

        mAddingPD = new ProgressDialog(this);
        mAddingPD.setTitle("Adding Gift");
        mAddingPD.setMessage("Processing...");
        mAddingPD.setCancelable(false);

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

        String giftName = mGifNameInput.getText().toString().trim();
        String priceString = mPriceInput.getText().toString().trim();
        String desc = mDescInput.getText().toString();

        Boolean boughtState = mBoughtSwitch.isChecked();

        if (TextUtils.isEmpty(giftName)){
            mGifNameInput.setError("Kindly enter gift name");
            return;
        }

        if (TextUtils.isEmpty(priceString)){
            mPriceInput.setError("Kindly enter the price");
            return;
        }

        if (!TextUtils.isEmpty(giftName) && !TextUtils.isEmpty(priceString)){

            mAddingPD.show();

            Map giftMap = new HashMap();
            giftMap.put("giftName", giftName);
            giftMap.put("giftPrice", Double.parseDouble(priceString));
            giftMap.put("description", desc);
            giftMap.put("bought", boughtState);

            mGiftsRef.push().setValue(giftMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        mAddingPD.dismiss();

                        Intent mainIntent = new Intent(AddGiftActivity.this, GiftsListActivity.class);
                        mainIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                        mainIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                        //mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
}
