package com.chatmaster.myblufly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {
EditText remail,rpass,rcpass,username;
Button sign;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        remail=findViewById(R.id.remail);
        rpass=findViewById(R.id.rpass);
        rcpass=findViewById(R.id.rcpass);
        sign=findViewById(R.id.reg);
        username=findViewById(R.id.name);

        mAuth = FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
        
    }

    private void CreateNewAccount() {

        String email=remail.getText().toString();
        String password=rpass.getText().toString();
        String confirm=rcpass.getText().toString();
        String name=username.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(SignUp.this,"enter  email..",Toast.LENGTH_LONG).show();
            remail.setError(email);
        }

        if (TextUtils.isEmpty(password)){
                Toast.makeText(SignUp.this,"enter password..",Toast.LENGTH_LONG).show();
                rpass.setError(password);
        }
        if (password.length()<6){
            rpass.setError("Weak Password");
            rpass.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirm)){
                Toast.makeText(SignUp.this,"confirm password..",Toast.LENGTH_LONG).show();
                rcpass.setError(confirm);
                rcpass.requestFocus();
        }
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Enter your Name..", Toast.LENGTH_SHORT).show();
            username.setError(name);
        }
        else{
            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please Wait..");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();


            mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        FirebaseUser user=mAuth.getCurrentUser();
                                        //get user email and uid from auth

                                        String email=user.getEmail();
                                        String uid=user.getUid();

                                        //When  user is registered store user info in firebase realtime database using hashmap

                                        HashMap<Object,String> hashMap=new HashMap<>();
                                        //put info in hashmap
                                        hashMap.put("email",email);
                                        hashMap.put("uid",uid);
                                        hashMap.put("username","");//will add later
                                        hashMap.put("dp","");// will add later
                                        hashMap.put("about","");//will add later




                                        // firebase database instance
                                        FirebaseDatabase database=FirebaseDatabase.getInstance();

                                        //path to store user data named "USERS"
                                        DatabaseReference reference=database.getReference("Users");

                                        //put data within hashmap in database
                                        reference.child(uid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SignUp.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                    Intent main =new Intent(SignUp.this,Permission.class);
                                                    main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(main);
                                                    finish();
                                                }else {
                                                    Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    } else {
                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            Toast.makeText(SignUp.this, "User Already Registered", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                Toast.makeText(SignUp.this, "User Already Registered", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                        progressDialog.dismiss();
                                    }
                            }
                        });
               }
         }




    public void login(View view) {
        Intent login=new Intent(SignUp.this,Login.class);
        startActivity(login);
    }


}
