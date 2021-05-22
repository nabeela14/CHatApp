package com.chatmaster.myblufly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    EditText uemail,pass;
    TextView forgot;
    Button log;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        uemail=findViewById(R.id.email);
        pass=findViewById(R.id.pass);
        forgot=findViewById(R.id.forgot);
        log=findViewById(R.id.log);
        progressDialog=new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }


        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLogIn();
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendToForgotPassword();
            }
        });

    }

    private void SendToForgotPassword() {
        Intent intent=new Intent(Login.this,Forgot.class);
        startActivity(intent);
    }

    private void UserLogIn() {

        String email=uemail.getText().toString();
        String password=pass.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(Login.this,"enter  email..",Toast.LENGTH_LONG).show();
            uemail.setError(email);
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            uemail.setError("Invalid");
            uemail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)){
            Toast.makeText(Login.this,"enter password..",Toast.LENGTH_LONG).show();
            pass.setError(password);
        }
        else {
            progressDialog.setTitle("Logging In");
            progressDialog.setMessage("Please Wait..");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();


                mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            SendUserToMain();
                            Toast.makeText(Login.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                        else {
                            String message=task.getException().toString();
                            Toast.makeText(Login.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

        }
    }

    private void SendUserToMain() {
        Intent intent=new Intent(this,Permission.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }


    public void Signup(View view) {
        SendUserTo();
    }

    private void SendUserTo() {
        Intent intent = new Intent(Login.this,SignUp.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }


}
