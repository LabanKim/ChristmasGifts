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

public class AddPersonActivity extends AppCompatActivity {

    //declare member variables

    private FirebaseAuth mAuth;
    private DatabaseReference mPersonListRef;

    private EditText mPersonNameInput;
    private TextView mDeadlineTv;
    private ImageView mPickDateIv;

    private ProgressDialog mAddingPD;

    private Calendar mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get the intent that opened this activity
        Intent intent = getIntent();
        //retrieve the name of the person picked from contacts
        String contactName = intent.getStringExtra("contactName");

        mPersonNameInput = findViewById(R.id.input_person_name);

        //check if the intent has a contact name
        if (intent.hasExtra("contactName")){

            getSupportActionBar().setTitle(contactName);
            mPersonNameInput.setText(contactName);

        }


        //initialize memeber variables
        mAuth = FirebaseAuth.getInstance();
        mPersonListRef = FirebaseDatabase.getInstance().getReference().child("PeopleList")
                .child(mAuth.getCurrentUser().getUid());
        mPersonListRef.keepSynced(true);

        mAddingPD = new ProgressDialog(this);
        mAddingPD.setTitle("Adding Person");
        mAddingPD.setMessage("Processing...");
        mAddingPD.setCancelable(false);

        mDeadlineTv = findViewById(R.id.tv_deadline);
        mPickDateIv = findViewById(R.id.iv_pick_date);


        //add onClickListener to show date picker
        mDeadlineTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePicker();


            }
        });

        //add onClickListener to show date picker
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

            savePerson();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //method to upload person data to firebase
    private void savePerson(){

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

            mAddingPD.show();

            //create a hasmap to store data about the person
            Map personMap = new HashMap();
            personMap.put("personName", name);
            personMap.put("deadline", deadline);
            personMap.put("bought", false);
            personMap.put("totalAmount", 0);

            //upload the hashmap to firebase which will write the data stored in it as children in the database
            mPersonListRef.push().setValue(personMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    //if the upload was successful
                    if (task.isSuccessful()){

                        mAddingPD.dismiss();

                        //send the user to the main activity
                        Intent mainIntent = new Intent(AddPersonActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        finish();

                    } else {

                        mAddingPD.dismiss();

                        Toast.makeText(AddPersonActivity.this, "Failed to add person. Please try again", Toast.LENGTH_LONG).show();

                    }

                }
            });

        }

    }

    //method to popup datepicker
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

                //set the picked date to the textview
                mDeadlineTv.setText(year + "-" + month + "-" + day);


            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

}
