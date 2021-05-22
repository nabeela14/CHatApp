package com.chatmaster.myblufly;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

import static com.chatmaster.myblufly.AppController.gson;
import static com.chatmaster.myblufly.AppController.mChatService;
import static com.chatmaster.myblufly.AppController.mConnectedDeviceAddress;
import static com.chatmaster.myblufly.AppController.mConnectedDeviceName;


public class BTChatActivity extends AppCompatActivity implements BTListener{

    private static final int REQUEST_ENABLE_BT = 10223;
    private static final int IMAGE_PICK_GALLERY_CODE = 13;
    TextView tv_name;
    TextView tv_address;
    EditText et_mssg;
    public String EXTRA_DEVICE_ADDRESS = "device_address";
    private ArrayAdapter<String> mConversationArrayAdapter;

    private StringBuffer mOutStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;

    Context context;
    // Layout Views
    private ListView mConversationView;
    private PickResult result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btchat);

        context=this;

        tv_address = findViewById(R.id.daddress);
        tv_name = findViewById(R.id.dname);
        mConversationView = findViewById(R.id.list);
        et_mssg = findViewById(R.id.msg);

        String address = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);
        if (address != null) {
            Log.e("chat", address);
        } else {
            finish();
            return;
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {

            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    /*private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        Log.e("Chat Activity","Connecting Device");
        mChatService.connect(device, secure);
        try {
            tv_name.setText(device.getName());
            tv_address.setText(device.getAddress());
        } catch (Exception ignored) {
            Log.e("error text", ignored.toString());
        }
    }*/

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService != null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            Log.e("chat ","service stopped");
            mChatService.stop();
            AppController.listener=null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                Log.e("chat ","service restarting");
                mChatService.start();
            }
            if(mChatService.getState()==BluetoothChatService.STATE_CONNECTED){
                tv_name.setText(mConnectedDeviceName);
                tv_address.setText(mConnectedDeviceAddress);
            }
            AppController.listener=this;
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            et_mssg.setText(mOutStringBuffer);
        }
    }

    private void setupChat() {

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        et_mssg.setOnEditorActionListener(mWriteListener);

        // Initialize the BluetoothChatService to perform bluetooth connections

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                BTMssg mssg=new BTMssg(message,"text");
                sendMessage(gson.toJson(mssg));
            }
            return true;
        }
    };

    public void sendmsg(View view) {
        String message = et_mssg.getText().toString();
        BTMssg mssg=new BTMssg(message,"text");
        sendMessage(gson.toJson(mssg));
    }

    @Override
    public void read(String mssg) {
        Log.e("read",gson.toJson(mssg));
        BTMssg btMssg=gson.fromJson(mssg,BTMssg.class);
        if(btMssg.type.equals("text"))
        mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + btMssg.mssg);
        else {
            // Image
            Bitmap image=ImageUtils.convert(btMssg.mssg);
            Log.e("image","new image recieved by user");
            Toast.makeText(context, "New image recieved", Toast.LENGTH_SHORT).show();
            // set the image on image view and add in adapter to user
        }
    }

    @Override
    public void write(String mssg) {
        BTMssg btMssg=gson.fromJson(mssg,BTMssg.class);
        Log.e("write",gson.toJson(mssg));
        if(btMssg.type.equals("text"))
            mConversationArrayAdapter.add("Me:  " + btMssg.mssg);
        else {
            // Image
            Bitmap image=ImageUtils.convert(btMssg.mssg);
            Log.e("image","new image sent by me");
            Toast.makeText(context, "New Image Sent by me", Toast.LENGTH_SHORT).show();
            // set the image on image view and add in adapter from user
        }
    }

    public void pickFile(View view) {
        PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
            @Override
            public void onPickResult(PickResult r) {
                //pic.setVisibility(View.GONE);
                //c_pic.setImageBitmap(r.getBitmap());
                result = r;
                showDialog(null);
            }
        }).show(getSupportFragmentManager());
    }
   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==IMAGE_PICK_GALLERY_CODE && resultCode==RESULT_OK){
            Uri uri=data.getData();
            showDialog(uri);
        }
    }*/

    private void showDialog(final Uri uri) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Confirm File Upload");
        builder.setMessage("Do you wish to send");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendFile(uri);
            }
        });
        builder.setNegativeButton("No",null);
        builder.create().show();
    }

    private void sendFile(Uri uri) {
        /*if (uri==null||uri.getPath()==null) {
            Log.e("uri","returning");
            return;
        }*/
        Bitmap bitmap;
        try {
            File file = new File(result.getPath());
            File file1 = Compressor.getDefault(context).compressToFile(file);

            bitmap = AppController.getBitmap(file1);
            Log.e("file","true");
        }catch (Exception e){
            try {
                bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),result.getUri());
                Log.e("bitmap","true");
            } catch (IOException ex) {
                bitmap=null;
            }
        }
        if(bitmap!=null) {
            Log.e("size",bitmap.getByteCount()+"");
            String image = ImageUtils.convert(bitmap);
            BTMssg mssg = new BTMssg(image, "image");
            Log.e("send file", gson.toJson(mssg));
            /*mConversationArrayAdapter.add(gson.toJson(mssg));
            mConversationArrayAdapter.notifyDataSetChanged();*/
            sendMessage(gson.toJson(mssg));
        }else
            Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
    }


}


