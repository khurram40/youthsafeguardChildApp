package com.example.child;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public EditText tv_ChildName, tv_MobileNumber;
    public String ChildName, MobileNumber,Number;
    public AlertDialog.Builder builder;
    public EditText et_phone;
    public String phoneNumber;
    public String verificationId;
    public AlertDialog alertDialog;
    public boolean check;
    ProgressDialog progressVerify;
    private CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setContentView(R.layout.activity_get_permissions);


        } else {

            setContentView(R.layout.activity_main);
        }

    }


    public void btn_SignUp_click(View view) {

        InitializeUi();
        phoneNumber = tv_MobileNumber.getText().toString().trim();
        phoneNumber = ccp.getFullNumberWithPlus();
        Number=tv_MobileNumber.getText().toString().trim();
        ChildName = tv_ChildName.getText().toString();
        if (ChildName.isEmpty() && phoneNumber.isEmpty()) {
            Toast.makeText(this, "Enter Name and Mobile Number", Toast.LENGTH_LONG).show();
        } else if (ChildName.isEmpty()) {
            tv_ChildName.setError("Enter Name");
            tv_ChildName.requestFocus();
        } else if (ChildName.length() > 15) {
            tv_ChildName.setError("Name length must be less then 15");
            tv_ChildName.requestFocus();
                } else if (phoneNumber.isEmpty()) {
            tv_MobileNumber.setError("Enter Number");
            tv_MobileNumber.requestFocus();
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please Wait!"); // Setting Message
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            progressDialog.show(); // Display Progress Dialog
            progressDialog.setCancelable(false);


            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.hasChild("ParentData/"+Number)) {

                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("CallReport/" +Number);
                        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.hasChild(ChildName)) {
                                    progressDialog.dismiss();
                                    tv_ChildName.setError("Already Added");
                                    tv_ChildName.requestFocus();
                                } else {

                                    progressDialog.dismiss();
                                    sendVerificationCode(phoneNumber);

                                }
                             }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Error! Please Try Again Later", Toast.LENGTH_LONG).show();
                            }
                        });


                    } else {
                        progressDialog.dismiss();
                        tv_MobileNumber.setError("No App Registered with this Number");
                        tv_MobileNumber.requestFocus();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error! Please Try Again Later", Toast.LENGTH_LONG).show();
                }
            });

        }
        // LinkToParent();
    }


    private void sendVerificationCode(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCall
        );
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCall = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onCodeSent(String vId, PhoneAuthProvider.ForceResendingToken token) {
            verificationId = vId;
            callDialog();
            Toast.makeText(getApplicationContext(), "Code Sent", Toast.LENGTH_SHORT).show();


        }
    };


    public void callDialog() {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.activity_otp, null);
        final Button btn_cancle = (Button) promptsView.findViewById(R.id.btn_cancle);
        final Button btn_verify = (Button) promptsView.findViewById(R.id.btn_verify);
        final EditText et_otp = (EditText) promptsView.findViewById(R.id.et_otp);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptsView);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);


        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();

            }
        });


        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String otp = et_otp.getText().toString();

                if (!otp.isEmpty()) {

                    verifyOtp(verificationId, otp);
                } else {
                    et_otp.setError("Enter Code!");
                }


            }
        });


    }


    public void verifyOtp(String v, String otp) {
        progressVerify = new ProgressDialog(MainActivity.this);
        progressVerify.setMessage("Please Wait!"); // Setting Message
        progressVerify.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressVerify.show(); // Display Progress Dialog
        progressVerify.setCancelable(false);


        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(v, otp);

        signInWithPhoneAuthCredential(credential);
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            progressVerify.dismiss();
                            alertDialog.dismiss();
                            DatabaseReference root = FirebaseDatabase.getInstance().getReference("CallReport/"+Number);
                            root.child(ChildName).setValue("");
                            DatabaseReference root1 = FirebaseDatabase.getInstance().getReference("MessageReport/"+Number);
                            root1.child(ChildName).setValue("");

                            User.setChildName(ChildName);
                            User.setParentsPhoneNumber(Number);

                            finish();

                            Intent intent = new Intent(MainActivity.this, getPermissions.class);
                            startActivity(intent);
                        } else {
                            progressVerify.dismiss();
                            alertDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Invalid Code ,Try Again", Toast.LENGTH_LONG).show();

                        }
                    }

                });
    }


    private void InitializeUi() {
        mAuth = FirebaseAuth.getInstance();
        tv_ChildName = (EditText) findViewById(R.id.tv_ChildName);
        tv_MobileNumber = (EditText) findViewById(R.id.tv_MobileNumber);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(tv_MobileNumber);

    }



    public void exitAlertDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Child Redistered Successfully")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Alert");
        alert.show();


    }

}
