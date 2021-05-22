package com.chatmaster.myblufly;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AllContacts extends AppCompatActivity {
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    RecyclerView recyclerView;
    UsersAdapter usersAdapter;
    List<ModelUsers> UsersList;
    RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        recyclerView=findViewById(R.id.allusers);
        recyclerView.setHasFixedSize(true);
        mLayoutManager=new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);

        UsersList=new ArrayList<>();
        getAllUsers();
    }

    private void getAllUsers() {
        progressDialog.show();
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UsersList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    if (!modelUsers.getUid().equals(firebaseUser.getUid())){
                        UsersList.add(modelUsers);
                        progressDialog.dismiss();
                    }
                    usersAdapter=new UsersAdapter(UsersList,getApplicationContext());
                    recyclerView.setAdapter(usersAdapter);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
progressDialog.dismiss();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.smenu, menu);

        MenuItem item=menu.findItem(R.id.search);
        final SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())){
                    searchUser(query);
                }else{
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query.trim())){
                    searchUser(query);
                }else{
                    getAllUsers();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void searchUser(final String query) {
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UsersList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    //getting all searched users.
                    if (!modelUsers.getUid().equals(firebaseUser.getUid())){
                        if (modelUsers.getUsername().toLowerCase().contains(query.toLowerCase())||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                            UsersList.add(modelUsers);
                        }

                    }
                    usersAdapter=new UsersAdapter(UsersList, getApplicationContext());

                    usersAdapter.notifyDataSetChanged();

                    recyclerView.setAdapter(usersAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id==R.id.logout){
            progressDialog.setTitle("Logging Out");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();

            mAuth.signOut();
            Intent send = new Intent(AllContacts.this, Login.class);
            startActivity(send);
        }else if (id==R.id.profile){
            Intent intent = new Intent(AllContacts.this, Profile.class);
            startActivity(intent);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent(AllContacts.this,MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

}



