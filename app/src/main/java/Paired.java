package com.chatmaster.myblufly;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.chatmaster.myblufly.AppController.mChatService;


/**
 * A simple {@link Fragment} subclass.
 */
public class Paired extends Fragment {

    private View ChatsView;
    BluetoothAdapter bluetoothAdapter;

    ListView listView;

    List<BluetoothDevice> devices=new ArrayList<>();


    public Paired() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        listView = ChatsView.findViewById(R.id.pairedlist);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device=devices.get(position);
                mChatService.connect(device, false);

            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        execute();
        return ChatsView;
    }

    private void execute() {
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        String[] strings = new String[bt.size()];
        int index = 0;
        if (bt.size() > 0) {
            for (BluetoothDevice device : bt) {
                devices.add(device);
                try {
                    strings[index] = device.getName();
                } catch (Exception e) {
                    strings[index] = device.getAddress();
                }
                index++;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strings);
            listView.setAdapter(adapter);
        }

    }


}
