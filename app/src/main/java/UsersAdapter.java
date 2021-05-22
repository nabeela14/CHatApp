package com.chatmaster.myblufly;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserListViewHolder> {

    List<ModelUsers> UserList;
    Context context;

    public UsersAdapter(List<ModelUsers> UserList,Context context) {
        this.UserList=UserList;
        this.context=context;
    }



    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView= LayoutInflater.from(context).inflate(R.layout.user_display,parent,false);
        UserListViewHolder recyclerView=new UserListViewHolder(layoutView);
        return recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
      holder.username.setText(UserList.get(position).getUsername());
        holder.userst.setText(UserList.get(position).getAbout());

        String userdp=UserList.get(position).getDp();
        try {
            Picasso.get().load(userdp).placeholder(R.drawable.ppic).into(holder.userdp);
        }catch (Exception e){

        }
//unique chat activity
            final String ret_name=UserList.get(position).getUsername();
            final String ret_dp=UserList.get(position).getDp();
            final String userUID=UserList.get(position).getUid();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatintent=new Intent(context, ChatActivity.class);
                chatintent.putExtra("useruid",userUID);
                chatintent.putExtra("ret_name",ret_name);
                chatintent.putExtra("ret_dp",ret_dp);
                chatintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chatintent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return UserList.size();
    }


    public static class UserListViewHolder extends RecyclerView.ViewHolder{
        CircleImageView userdp;
        TextView username,userst;

        public UserListViewHolder(@NonNull View itemView) {
            super(itemView);
            userdp=itemView.findViewById(R.id.userdp);
            username=itemView.findViewById(R.id.username);
            userst=itemView.findViewById(R.id.userst);

        }
    }

}
