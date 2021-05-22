package com.chatmaster.myblufly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class Profile extends AppCompatActivity {

 EditText name,abouttv;

 CircleImageView pic;
 Button update;
 Uri image_uri;
 String profilePhoto="dp";
ProgressDialog progressDialog;

 FirebaseAuth firebaseAuth;
 FirebaseUser user;
 FirebaseDatabase firebaseDatabase;
 DatabaseReference databaseReference;
 StorageReference storageReference;
 String storagePath="Users_Profile_Image/";

 //permission
    private static final int CAMERA_REQUEST_CODE=1;
    private static final int STORAGE_REQUEST_CODE=2;
    private static final int IMAGE_PICK_GALLERY_CODE=3;
    private static final int IMAGE_PICK_CAMERA_CODE=4;

    String cameraPermission[];
    String storagePermision[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        // firebase
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference=getInstance().getReference();

        // views
        name=findViewById(R.id.stname);
        pic=findViewById(R.id.ppic);
        abouttv=findViewById(R.id.stabt);

        update=findViewById(R.id.update);
        progressDialog=new ProgressDialog(this);

        // arrays
        cameraPermission=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermision=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog.setTitle("Fetching Details");
        progressDialog.show();

        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ModelUsers user=dataSnapshot.getValue(ModelUsers.class);
                name.setText(user.username);
                abouttv.setText(user.about);

                try {
                    Picasso.get().load(user.dp).into(pic);
                }catch (Exception ignored){

                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Query query=databaseReference.orderByChild("email").equalTo(user.getDisplayName());
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String username=""+ds.child("username").getValue();
                    String about=""+ds.child("about").getValue();
                    String dp=""+ds.child("dp").getValue();


                    //set data
                    name.setText(username);
                    abouttv.setText(about);


                    try {
                        Picasso.get().load(dp).into(pic);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ppic).into(pic);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String options[]={"Camera","Gallery"};

                progressDialog.setMessage("Updating Profile Picture");
                progressDialog.show();
                //alert dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(Profile.this);
                //title
                builder.setTitle("Pick Image From");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which==0){

                            if (!CheckCameraPermission()){
                                RequestCameraPermission();
                            }else{
                                PickFromCamera();
                            }

                        }else if (which==1){

                                if (!CheckStoragePermission()){
                                    RequestStoragePermission();
                                }else{
                                    PickFromGallery();
                                }

                        }

                    }
                });

                builder.create().show();
            }
        });
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Updating Username");
                progressDialog.show();
                UpdateUsernameAndAbout("username","username");
            }
        });
        abouttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Updating About");
                progressDialog.show();
                UpdateUsernameAndAbout("about","about");
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Profile.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private void UpdateUsernameAndAbout(final String key, final String val){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Update"+" "+key);
        LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        final EditText editText=new EditText(this);
        editText.setHint("Enter here");
        linearLayout.addView(editText);
        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value=editText.getText().toString().trim();
                if (!value.isEmpty()){
                    progressDialog.dismiss();
                    HashMap<String,Object> result=new HashMap<>();
                    result.put(val,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                   progressDialog.dismiss();
                                    Toast.makeText(Profile.this, "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                  progressDialog.dismiss();
                                    Toast.makeText(Profile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }else {

                    Toast.makeText(Profile.this, "Enter Username", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private boolean CheckStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void RequestStoragePermission(){
        ActivityCompat.requestPermissions((Activity) getApplicationContext(),storagePermision,STORAGE_REQUEST_CODE);
    }

    private boolean CheckCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);


        boolean result1= ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }

    private void RequestCameraPermission(){
        ActivityCompat.requestPermissions((Activity) getApplicationContext(),cameraPermission,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //user selects allow or deny from dialog

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                            PickFromCamera();
                    }else{
                        Toast.makeText(this, "Enable Permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if ( writeStorageAccepted){
                        PickFromGallery();
                    }else{
                        Toast.makeText(this, "Enable Permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void PickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp ProfileImage");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        image_uri=getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void PickFromGallery(){
        Intent galleryIntent =new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();
                UploadProfile(image_uri);
            }
            if (requestCode==IMAGE_PICK_CAMERA_CODE){
                image_uri=data.getData();
                UploadProfile(image_uri);
            }
            pic.setImageURI(image_uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

private void UploadProfile(final Uri uri){
String filePath=storagePath+""+profilePhoto+""+user.getUid();
StorageReference storageReference1=storageReference.child(filePath);
storageReference1.putFile(uri)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri=uriTask.getResult();
                if (uriTask.isSuccessful()){
                    HashMap<String,Object> results =new HashMap<>();
                    results.put(profilePhoto,downloadUri.toString());
                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Profile.this, "Image Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Profile.this, "Error..", Toast.LENGTH_SHORT).show();
                                }
                            });
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(Profile.this, "Some error ", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
}


}
