package com.chatmaster.myblufly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BTChatAdapter extends ArrayAdapter<BTModelChat> {

    private LayoutInflater layoutInflater;
    private List<BTModelChat> Messages;
    Context ctx;

    public BTChatAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BTModelChat> objects) {
        super(context, resource, objects);

        ctx=context;
        layoutInflater=LayoutInflater.from(context);
        this.Messages=objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BTModelChat Message=Messages.get(position);
        TextView msg,time;
        if (Message.getSender()==1){
            convertView=layoutInflater.inflate(R.layout.sender_layout,null);
        }else /*if(Message.getReceiver()==2)*/
        {
            convertView=layoutInflater.inflate(R.layout.recceiver_layout,null);
        }
        msg=(TextView) convertView.findViewById(R.id.msg_txt);
        msg.setText(Message.getMsg());
        time=(TextView) convertView.findViewById(R.id.time);

        String formattedDate=new SimpleDateFormat("hh:mm").format(new Date(Message.getTime()));
        time.setText(formattedDate);
        return convertView;
    }
}
