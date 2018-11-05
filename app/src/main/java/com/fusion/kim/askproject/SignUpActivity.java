package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    //declare member variables

    private Button mSignUpBtn;
    private EditText mEmailInput, mPasswordInput, mConfPassword;

    private ProgressDialog mSignupPD;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //initialize member variables

        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserRef.keepSynced(true);

        mEmailInput = findViewById(R.id.input_register_email);
        mPasswordInput = findViewById(R.id.input_register_password);
        mConfPassword = findViewById(R.id.input_confirm_password);

        mSignUpBtn = findViewById(R.id.btn_register);

        mSignupPD = new ProgressDialog(this);
        mSignupPD.setTitle("Creating User");
        mSignupPD.setMessage("Please wait while we set things up for you...");
        mSignupPD.setCancelable(false);
        mSignupPD.setIndeterminate(true);


        //add clock listener to the signup button
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //retrieve user input
                final String email = mEmailInput.getText().toString().trim();
                final String pass = mPasswordInput.getText().toString().trim();
                final String confPass = mPasswordInput.getText().toString().trim();

                if (TextUtils.isEmpty(email)){

                    mEmailInput.setError("Please Enter an Email Address");
                    return;

                }

                if (TextUtils.isEmpty(pass)){

                    mPasswordInput.setError("Please Enter a Password");
                    return;

                }

                if (TextUtils.isEmpty(confPass)){

                    mConfPassword.setError("Please Confirm your Password");
                    return;

                }

                if (!pass.equals(confPass)){

                    mConfPassword.setError("Passwords do not match!");
                    return;

                }

                if (!TextUtils.isEmpty(email)  && !TextUtils.isEmpty(pass) &&
                        !TextUtils.isEmpty(confPass) && pass.equals(confPass)){

                    mSignupPD.show();

                    //send request to creat a user using email and password
                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                //creating the user was successful

                                //create a hashmap to store user details
                                Map userMap = new HashMap();
                                userMap.put("emailAddress", email);
                                userMap.put("password", pass);

                                //upload the hashmap to firebase
                                mUserRef.child(task.getResult().getUser().getUid()).
                                        setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            //upload was successful

                                            mSignupPD.dismiss();

                                            //send the user to the main activity
                                            Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();

                                        }

                                    }
                                });

                            } else {
                                //upload failed

                                mSignupPD.dismiss();

                                Toast.makeText(SignUpActivity.this, "Failed to create account. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                            }

                        }
                    });

                }

            }
        });
    }


}
