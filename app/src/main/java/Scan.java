package com.chatmaster.myblufly;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import static com.chatmaster.myblufly.AppController.mChatService;

public class Scan extends Fragment {

    private View ScanView;

    ListView listView;
    Button button;
    ProgressDialog progressDialog;
    //ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();
    //BluetoothAdapter bluetoothAdapter;
    //private ArrayAdapter<String> btArrayAdapter;
    //ArrayList<String> btNames = new ArrayList<>();

    private static final int REQUEST_ENABLE_BT = 1;

    private String TAG = "Bluetooth";

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;
    Context context;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ScanView = inflater.inflate(R.layout.fragment_scan, container, false);

        listView = ScanView.findViewById(R.id.slist);
        button = ScanView.findViewById(R.id.sbtn);

        context=getActivity();
        if(context==null){
            return null;
        }

        mNewDevicesArrayAdapter = new ArrayAdapter<String>(context, R.layout.device_name);


        listView.setAdapter(mNewDevicesArrayAdapter);
        listView.setOnItemClickListener(mDeviceClickListener);

        progressDialog = new ProgressDialog(getContext());

        button.setOnClickListener(btnScanDeviceOnClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        ensureDiscoverable();

        return ScanView;
    }

    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void doDiscovery() {
        Log.e(TAG, "doDiscovery()");

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        context.unregisterReceiver(mReceiver);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            mChatService.connect(mBtAdapter.getRemoteDevice(address),true);

            /*// Create the result Intent and include the MAC address
            Intent intent = new Intent(context,BTChatActivity.class);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intent);*/
            // Set result and finish this Activity
        }
    };

    private Button.OnClickListener btnScanDeviceOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            mNewDevicesArrayAdapter.clear();
            progressDialog.show();
            progressDialog.setMessage("Searching");

            doDiscovery();

        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if(device.getName()!=null)
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
            progressDialog.dismiss();
        }
    };

}


