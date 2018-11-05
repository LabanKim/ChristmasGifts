package com.fusion.kim.askproject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditPersonActivity extends AppCompatActivity {

    //declare member variables
    private FirebaseAuth mAuth;
    private DatabaseReference mPersonListRef;

    private EditText mPersonNameInput;
    private TextView mDeadlineTv;
    private ImageView mPickDateIv;

    private ProgressDialog mUpdatingPD;

    private Calendar mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person);

        //set up the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //set the title of the activity as the name of the person we are editing
        getSupportActionBar().setTitle(getIntent().getStringExtra("personName"));

        //initialize the member variables
        mAuth = FirebaseAuth.getInstance();

        mPersonListRef = FirebaseDatabase.getInstance().getReference().child("PeopleList")
                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID"));
        mPersonListRef.keepSynced(true);

        mUpdatingPD = new ProgressDialog(this);
        mUpdatingPD.setTitle("Updating Person");
        mUpdatingPD.setMessage("Processing...");
        mUpdatingPD.setCancelable(false);

        mPersonNameInput = findViewById(R.id.input_edit_person_name);
        mPersonNameInput.setText(getIntent().getStringExtra("personName"));

        mDeadlineTv = findViewById(R.id.tv_edit_deadline);
        mPickDateIv = findViewById(R.id.iv_edit_pick_date);

        //set the tex of the deadline textfield as the date retrieved from the intent that opened this activity
        mDeadlineTv.setText(getIntent().getStringExtra("deadline"));


        //add click listener to show date picker to the textview
        mDeadlineTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePicker();

            }
        });

        //add click listener to show date picker to the calendar icon
        mPickDateIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePicker();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_person, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_save_person) {

            updatePerson();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    //method to upload and update the person data
    private void updatePerson(){

        //retrieve user input
        String name = mPersonNameInput.getText().toString().trim();
        String deadline = mDeadlineTv.getText().toString().trim();

        if (TextUtils.isEmpty(name)){

            mPersonNameInput.setError("Kindly provide a name.");
            return;
        }

        if (TextUtils.isEmpty(deadline)){

            mPersonNameInput.setError("Kindly provide a deadline");
            return;

        }

        if (!TextUtils.isEmpty(name)  && !TextUtils.isEmpty(deadline) ){

            mUpdatingPD.show();

            //create a hashmap to store person data
            Map personMap = new HashMap();
            personMap.put("personName", name);
            personMap.put("deadline", deadline);

            //upload hashmap to the firebase reference of the person to update the data
            mPersonListRef.setValue(personMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        //if the upate was successful
                        mUpdatingPD.dismiss();

                        //send the user to the main activity
                        Intent mainIntent = new Intent(EditPersonActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        finish();

                        Toast.makeText(EditPersonActivity.this, "Updated Successfully", Toast.LENGTH_LONG).show();

                    } else {

                        mUpdatingPD.dismiss();

                        Toast.makeText(EditPersonActivity.this, "Failed to save changes. Please try again", Toast.LENGTH_LONG).show();

                    }

                }
            });

        }

    }


    //method to pop up date picker
    public void showDatePicker() {
        final Calendar currentDate = Calendar.getInstance();
        mDate = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mDate.set(year, monthOfYear, dayOfMonth);

                String day = "";
                String month = "";

                //format the date
                int monthNew = monthOfYear + 1;

                if (dayOfMonth < 10){

                    day = "0"+dayOfMonth;

                } else {

                    day = ""+dayOfMonth;

                }

                if (monthNew < 10){

                    month = "0"+monthNew;

                } else {

                    month = ""+monthNew;

                }

                //set the date to the textview
                mDeadlineTv.setText(year + "-" + month + "-" + day);


            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }


}
