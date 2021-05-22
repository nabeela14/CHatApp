package com.chatmaster.myblufly;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;

import static com.chatmaster.myblufly.Scan.EXTRA_DEVICE_ADDRESS;

public class AppController extends Application {

    public static BluetoothChatService mChatService = null;
    public static String mConnectedDeviceName;
    public static String mConnectedDeviceAddress;
    private static Context context;
    public static BTListener listener;
    public static Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();

        //gson=new Gson();
        gson = new GsonBuilder().disableHtmlEscaping().create();

        mChatService = new BluetoothChatService(this, mHandler);

        context = this;
    }

    public static Bitmap getBitmap(File file) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @SuppressLint("HandlerLeak")
    public static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(context, "Connecting", Toast.LENGTH_SHORT).show();

                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(context, "Not Connected", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    if (listener != null)
                        listener.write(writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (listener != null)
                        listener.read(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    mConnectedDeviceAddress = msg.getData().getString(Constants.DEVICE_ADDRESS);
                    Toast.makeText(context, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, BTChatActivity.class).putExtra(EXTRA_DEVICE_ADDRESS, mConnectedDeviceAddress).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != context) {
                        Toast.makeText(context, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

}
