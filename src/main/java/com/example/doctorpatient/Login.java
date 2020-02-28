package com.example.doctorpatient;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private EditText edtEmail, EdtPassrof;
    Button login;
    TextView signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //direct go to activity as if hey login

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null) {

            TransferToPassengerMap();
            TransferToDoctorActivity();

        }

        edtEmail = findViewById(R.id.edtEmailSignupid);
        EdtPassrof = findViewById(R.id.edtSignUpPasswordId);
        login = findViewById(R.id.loginId);
        signUp = findViewById(R.id.signupiId);
        setTitle("LogIn");

        login.setOnClickListener(this);
        signUp.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.loginId:

                ParseUser.logInInBackground(edtEmail.getText().toString(),
                        EdtPassrof.getText().toString(), new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {

                                if (user != null && e == null) {

                                    TransferToPassengerMap();
                                    TransferToDoctorActivity();

                                } else
                                    FancyToast.makeText(Login.this, e + "", FancyToast.
                                            LENGTH_LONG, FancyToast.ERROR, true).show();

                            }
                        });
                break;
            case R.id.signupiId:
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }

    void TransferToPassengerMap() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("LoginAS").equals("Patient")) {
                Intent intent = new Intent(Login.this, PatientActivity.class);
                startActivity(intent);
            }
        } else {
        }
    }

    void TransferToDoctorActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("LoginAS").equals("Doctor")) {
                Intent intent = new Intent(Login.this, DoctorActivity.class);
                startActivity(intent);
            }
        } else {
        }


    }


}
