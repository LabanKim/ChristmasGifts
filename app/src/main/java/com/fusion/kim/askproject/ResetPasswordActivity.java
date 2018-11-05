package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    //declare member variables

    private FirebaseAuth mAuth;
    private EditText mEmailTv;
    private Button mResetBtn;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //set the back button and the title of the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reset Passord");

        //initialize the member variables
        mAuth = FirebaseAuth.getInstance();

        mEmailTv = findViewById(R.id.input_reset_email);
        mResetBtn = findViewById(R.id.btn_reset_pass);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Sending...");
        mProgress.setCancelable(false);


        //set click listener for the reset button
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //retrieve the email from input field
                String email = mEmailTv.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {

                    mEmailTv.setError("Provide your registered email address");

                    return;
                }

                mProgress.show();

                //send request to reset the password
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //request sent successfully and the user should check their inbox
                                    Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    //request failed
                                    Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }

                                mProgress.dismiss();
                            }
                        });
            }
        });
    }


}
