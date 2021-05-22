package com.chatmaster.myblufly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Forgot extends AppCompatActivity {
EditText resemail;
Button respass;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        resemail=findViewById(R.id.resemail);
        respass=findViewById(R.id.reset);

        mAuth = FirebaseAuth.getInstance();

        respass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=resemail.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Forgot.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    resemail.setError(email);
                    resemail.requestFocus();
                }else {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Forgot.this, "Reset Link Sent", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Forgot.this, "Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Forgot.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
