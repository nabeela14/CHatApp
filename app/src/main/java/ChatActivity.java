package com.chatmaster.myblufly;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    ImageButton send, attach;
    EditText msg;
    RecyclerView recyclerView;
    Toolbar toolbar;
    CircleImageView dppic;
    TextView name;
    Context context;


    String msg_receiverID;
    String msg_receiverName;
    String myuid;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage mStorage;

    String userImage;
    List<ModelChat> chatList;
    ChatAdapter chatAdapter;



    private static final int IMAGE_PICK_GALLERY_CODE = 3;
    String storagePermission[];
    private static final int STORAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mStorage=FirebaseStorage.getInstance();
        Initialized();


        Query userquery = databaseReference.orderByChild("uid").equalTo(msg_receiverID);
        userquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String username = "" + ds.child("username").getValue();
                    userImage = "" + ds.child("dp").getValue();
                    //set data
                    name.setText(username);
                    try {
                        Picasso.get().load(userImage).placeholder(R.drawable.ppic).into(dppic);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ppic).into(dppic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        readMessage();

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pickFile();
            }
        });
    }

    private void pickFile() {
        String[] mimeTypes={
                "image/jpeg",
        };

        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            intent.setType("*/*");
            intent.putExtra(intent.EXTRA_MIME_TYPES,mimeTypes);
        }else{
            StringBuilder mimTypesStr=new StringBuilder();
            for (String mimType:mimeTypes){
                mimTypesStr.append(mimType).append("|");
            }
            intent.setType(mimTypesStr.substring(0,mimTypesStr.length()-1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"),IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==IMAGE_PICK_GALLERY_CODE && resultCode==RESULT_OK){
            Uri uri=data.getData();



            showDialog(uri);
        }
    }

    private void showDialog(final Uri uri) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Confirm File Upload");
        builder.setMessage("Do you wish to send");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    uploadFile(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("No",null);
        builder.create().show();
    }

    private void uploadFile(final Uri uri) throws IOException {
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading File");
        progressDialog.show();

      StorageReference ref=mStorage.getReference().child("Documents").child(myuid + "");
      ref.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
              if (task.isSuccessful()){
                  Toast.makeText(context, "File uploaded", Toast.LENGTH_SHORT).show();
              }else {
                  Log.e("failed",task.getException().toString());
                  Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
              }
              progressDialog.dismiss();
          }
      }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
              double progress=(100.0*taskSnapshot.getBytesTransferred() / taskSnapshot
              .getTotalByteCount());
              progressDialog.setMessage("Uploading.."+""+progress+"%");
          }
      });
        progressDialog.setMessage("Displaying..");
        progressDialog.show();
        Bitmap bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data=baos.toByteArray();
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri=uriTask.getResult().toString();
                        if (uriTask.isSuccessful()){
                            DatabaseReference datareference=FirebaseDatabase.getInstance().getReference();
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("sender", myuid);
                            hashMap.put("receiver", msg_receiverID);
                            hashMap.put("message",downloadUri);
                            hashMap.put("type","image");
                            datareference.child("Chats").push().setValue(hashMap);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Initialized() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        dppic = findViewById(R.id.dppic);
        name = findViewById(R.id.name);
        attach = findViewById(R.id.attach);

        send = findViewById(R.id.send);
        msg = findViewById(R.id.msg);
        recyclerView = findViewById(R.id.chat_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        msg_receiverID = intent.getStringExtra("useruid");
        msg_receiverName = intent.getStringExtra("ret_name");


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");


        context = this;
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


    }

   /* private void RequestStoragePermission() {
        ActivityCompat.requestPermissions((Activity) getApplicationContext(), storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean CheckStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }*/


    private void readMessage() {
        chatList=new ArrayList<>();
        DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Chats");
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                Log.e( "found " , dataSnapshot.toString());
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    Log.e( "child " , ds.toString());
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myuid)&& chat.getSender().equals(msg_receiverID)||
                            chat.getReceiver().equals(msg_receiverID)&& chat.getSender().equals(myuid)){

                        chatList.add(chat);
                    }

                    chatAdapter=new ChatAdapter(ChatActivity.this,chatList,userImage);
                    chatAdapter.notifyDataSetChanged();

                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myuid = user.getUid();//logged in user
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void sendmsg(View view) {
        String message = msg.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "null message", Toast.LENGTH_SHORT).show();
        } else {
            sendMessages(message);
        }

    }

    private void sendMessages(String message) {
        DatabaseReference datareference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myuid);
        hashMap.put("receiver", msg_receiverID);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("type","text");


        datareference.child("Chats").push().setValue(hashMap);



        msg.setText("");
    }

    @Override
    protected void onStart() {
        checkUser();
        super.onStart();
    }

}