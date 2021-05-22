package com.chatmaster.myblufly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient  mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapsActivity.this, ""+marker.getPosition(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(MapsActivity.this, ""+marker.getPosition(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(MapsActivity.this, ""+marker.getPosition(), Toast.LENGTH_SHORT).show();
            }
        });

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(28,77))
                .title("Hello")
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28,77),10));
        //addpolyline();

        //addcircle();
        addpolygon();
        buildGoogleApiClient();

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    private void addpolygon() {
        PolygonOptions options=new PolygonOptions()
                .add(new LatLng(28,78))
                .add(new LatLng(26.1,78.1))
                .add(new LatLng(28.2,78.2))
                .fillColor(Color.GREEN);

        mMap.addPolygon(options);
    }

    private void addcircle() {
        CircleOptions options=new CircleOptions()
                .center(new LatLng(28,78))
                .radius(100)
                .strokeWidth(2)
                .strokeColor(Color.RED)
                .fillColor(Color.GREEN);

        mMap.addCircle(options);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28,78),16));
    }

    private void addpolyline() {
        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(28,78))
                .add(new LatLng(26.1,78.1))
                .add(new LatLng(28.2,78.2))
                .width(20)
                .color(Color.RED)
                .startCap(new SquareCap())
                .endCap(new RoundCap()));
        //com.google.android.gms.maps.model.CAP
        /*mMap.addMarker(new MarkerOptions().position(new LatLng(28,78)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(26.1,78.1)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(28.2,78.2)));*/
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, (LocationListener) this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng mylatlng = new LatLng(location.getLatitude(), location.getLongitude());

        if (marker != null) {
            marker.remove();
        }
        MarkerOptions options=new MarkerOptions()
                .position(mylatlng)
                .title("My Location")
                .snippet("This is my Location")
                .draggable(true);

        marker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylatlng));
    }
}
