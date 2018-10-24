package com.fusion.kim.askproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailInput, mPasswordInput;
    private TextView mForgotPassTv, mSignUpTv;
    private Button mLoginBtn;

    private ProgressDialog mLoginPD;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEmailInput = findViewById(R.id.input_login_email);
        mPasswordInput = findViewById(R.id.input_login_password);
        mLoginBtn = findViewById(R.id.btn_login);
        mForgotPassTv = findViewById(R.id.tv_forgot_password);
        mSignUpTv = findViewById(R.id.tv_sign_up);


        mLoginPD = new ProgressDialog(this);
        mLoginPD.setTitle("Signing In");
        mLoginPD.setMessage("Please wait while we log you in...");
        mLoginPD.setCancelable(false);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        mSignUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

    }

    private void performLogin() {

        final String email = mEmailInput.getText().toString().trim();
        final String pass = mPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)){

            mEmailInput.setError("Please Enter an Email Address");
            return;

        }

        if (TextUtils.isEmpty(pass)){

            mPasswordInput.setError("Please Enter a Password");
            return;

        }

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)){

            mLoginPD.show();

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        mLoginPD.dismiss();

                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();

                    } else {

                        mLoginPD.dismiss();

                        Toast.makeText(LoginActivity.this, "Failed to login. Please try again." + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    }

                }
            });

        }

    }
}
