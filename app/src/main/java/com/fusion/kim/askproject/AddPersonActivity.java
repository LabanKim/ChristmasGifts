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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddPersonActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mPersonListRef;

    private EditText mPersonNameInput;
    private TextView mDeadlineTv;

    private ProgressDialog mAddingPD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mPersonListRef = FirebaseDatabase.getInstance().getReference().child("PeopleList")
                .child(mAuth.getCurrentUser().getUid());
        mPersonListRef.keepSynced(true);

        mAddingPD = new ProgressDialog(this);
        mAddingPD.setTitle("Adding Person");
        mAddingPD.setMessage("Processing...");
        mAddingPD.setCancelable(false);

        mPersonNameInput = findViewById(R.id.input_person_name);
        mDeadlineTv = findViewById(R.id.tv_deadline);


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



    private void savePerson(){

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

            Map personMap = new HashMap();
            personMap.put("personName", name);
            personMap.put("deadline", deadline);

            mPersonListRef.push().setValue(personMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        mAddingPD.dismiss();

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
}
