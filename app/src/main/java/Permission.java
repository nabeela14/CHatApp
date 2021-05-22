package com.chatmaster.myblufly;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Permission extends AppCompatActivity {
Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        SharedPreferences preferences=getSharedPreferences("preferences",MODE_PRIVATE);
        boolean firststart=preferences.getBoolean("firststart",true);
        if (firststart) {
            showDialog();
        }
        btn = findViewById(R.id.btn);


        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {

            Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show();
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothAdapter.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 1);

                }else{
                    Toast.makeText(Permission.this, "Already On", Toast.LENGTH_SHORT).show();
                    sendTo();
                }


                }
        });


    }

    private void showDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("You may setup your profile")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
                    }
                }).create().show();
        SharedPreferences preferences=getSharedPreferences("preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("firststart",false);
        editor.apply();
    }

    private void sendTo() {
        Intent intent= new Intent(Permission.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                sendTo();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth Enabling Cancelled ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}


