package com.chatmaster.myblufly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter  extends RecyclerView.Adapter<ChatAdapter.MyHolder> {

    private static final int MSG_TYPE_R=0;
    private static final int MSG_TYPE_S=1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser firebaseUser;

    public ChatAdapter(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==MSG_TYPE_S){
            View view= LayoutInflater.from(context).inflate(R.layout.sender_layout,parent,false);
            return new MyHolder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.recceiver_layout,parent,false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
//get data
        String message=chatList.get(position).getMessage();
        String timeStamp=chatList.get(position).getTimestamp();
        String type=chatList.get(position).getType();

        //convert time stamp to hrs and min
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
       // calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dtime= DateFormat.format(" hh:mm aa",calendar).toString();
//
        if (type==null||type.equals("text")){
            holder.message.setVisibility(View.VISIBLE);
            holder.media.setVisibility(View.GONE);
            holder.message.setText(message);
        }else {
            holder.media.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.GONE);

            Picasso.get().load(message).placeholder(R.drawable.gal).into(holder.media);
        }
//
        //holder.message.setText(message);
        holder.time.setText(dtime);


        try {
            Picasso.get().load(imageUrl).into(holder.imageView);
        }catch (Exception e){

        }

        holder.mlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Do you want to delete this Message");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void deleteMessage(int position) {
        final String myUId=FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgtime=chatList.get(position).getTimestamp();
        DatabaseReference dref= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dref.orderByChild("timestamp").equalTo(msgtime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){

                    if (ds.child("sender").getValue().equals(myUId)){

                        ds.getRef().removeValue();

                        /*HashMap<String,Object>hashMap=new HashMap<>();
                        hashMap.put("message","This message was deleted..");
                        ds.getRef().updateChildren(hashMap);*/

                        Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "only sent message can be deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public int getItemViewType(int position){
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_S;
        }else{
            return MSG_TYPE_R;
        }

    }
    //view holder class

    class MyHolder extends RecyclerView.ViewHolder{

        //views
        CircleImageView imageView;
        ImageView media;
        TextView message,time;
        LinearLayout mlayout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.mdp);
            message=itemView.findViewById(R.id.msg_txt);
            time=itemView.findViewById(R.id.time);
            media=itemView.findViewById(R.id.media);
            mlayout=itemView.findViewById(R.id.mlayout);

        }
    }
}
