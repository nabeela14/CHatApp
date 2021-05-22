package com.chatmaster.myblufly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    TabLayout tab1;
    ViewPager viewPager;
    List<Fragment> fragments = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    ProgressDialog progressDialog;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private Context context;


    private final int LOCATION_PERMISSION_REQUEST = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        Initialized();


        }






    private void Initialized() {

        tab1 = findViewById(R.id.tab1);
        viewPager = findViewById(R.id.vp);
        fragments.add(new Scan());
        fragments.add(new Paired());

        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tab1.setupWithViewPager(viewPager);
        int[] tabIcons={
                R.drawable.scan,
                R.drawable.paired
        };
        for (int i=0;i<tab1.getTabCount();i++){
            if (tab1.getTabAt(i)!=null){
                tab1.getTabAt(i).setIcon(tabIcons[i]);

            }
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AllContacts.class);
                startActivity(intent);
            }
        });

        CheckPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
        }
       /* else if (id == R.id.paired) {
            CheckPermissions();
            Intent intent=new Intent(this,Paired.class);
            startActivity(intent);
        }*/

        else if (id == R.id.online) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Switch To Online Mode");
            builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bluetoothAdapter.disable();
                    Toast.makeText(MainActivity.this, "Turning Off Bluetooth", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, AllContacts.class));
                }
            });
            builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "cancelled", Toast.LENGTH_SHORT).show();

                }
            });
            builder.create().show();
        } else if (id == R.id.logout) {
            progressDialog.setTitle("Logging Out");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();

            mAuth.signOut();
            Intent send = new Intent(MainActivity.this, Login.class);
            startActivity(send);

        }
        return true;
    }



    public class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {

                return "Scan";
            } else
                return "Devices";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            SendToLogin();
        }
    }

    private void SendToLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }

    private boolean CheckPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return false;
        }
    else
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, Paired.class);
                startActivity(intent);
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("Location Permission Required")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CheckPermissions();
                            }
                        })
                        .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }
}
