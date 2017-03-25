package com.pservice.manas.busservice;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.renderscript.Double2;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    Location mylocation;
    FloatingActionButton fab;
    ArrayList<Marker> markers;
    ArrayList<Bus> buses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        fab=(FloatingActionButton)findViewById(R.id.fab);
    }

    @Override
    public void onLocationChanged(Location location) {

        mylocation=location;
    //     Toast.makeText(getApplicationContext(),"Location Changed",Toast.LENGTH_SHORT).show();
         new ShowBusesTask().execute("");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

//        Toast.makeText(getApplicationContext(), "Location Service connected", Toast.LENGTH_SHORT).show();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                         Manifest.permission.ACCESS_COARSE_LOCATION},1);

                } else
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
        else
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
 //       Toast.makeText(getApplicationContext(), "Location Service conn suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
 //       Toast.makeText(getApplicationContext(), "Location Service conn failed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int ind=markers.indexOf(marker);
                if(ind!=-1)
                {
                    Bus bus=buses.get(ind);
                    TextView t1,t2,t3,t4;
                    Button b;

                    final Dialog dialog=new Dialog(MapsActivity.this);
                    dialog.setContentView(R.layout.layout_dialog);

                    t1=(TextView)dialog.findViewById(R.id.maps_details_name2);
                    t2=(TextView)dialog.findViewById(R.id.maps_details_address2);
                    t3=(TextView)dialog.findViewById(R.id.maps_details_loc2);
                    t4=(TextView)dialog.findViewById(R.id.maps_details_crowd2);
                    b=(Button)dialog.findViewById(R.id.maps_dialog_button);

                    dialog.setTitle("Driver's Details");
                    t1.setText(bus.getDrivername());
                    t2.setText(bus.getBusno());
                    t3.setText(bus.getStops());
                    t4.setText(bus.getCrowd());

                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                    return false;
                }
                else
                return false;
            }
        });
        mMap.clear();


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},1);

            } else
                mMap.setMyLocationEnabled(true);
        }
        else
            mMap.setMyLocationEnabled(true);


    }

    class ShowBusesTask extends AsyncTask<String,String,String>
    {
        String response;

        @Override
        protected String doInBackground(String... params) {
            try
            {
                response=ServletInterface.makeRequest(params[0],"http://www.myprojectshub.com/busbackend/getbusdata.php");
                return response;
            }
            catch (Exception e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {

                JSONObject jsonObject=new JSONObject(s);
                buses=new ArrayList<>();

                if(jsonObject.getString("status").equals("success"))
                {
                    markers=new ArrayList<>();
                    JSONArray jsonArray=jsonObject.getJSONArray("data");
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject jsonObject1=jsonArray.getJSONObject(i);

                        String latitude=jsonObject1.getString("latitude");
                        String longitude=jsonObject1.getString("longitude");

                        if(!latitude.isEmpty() && !longitude.isEmpty())
                        {
                            LatLng sydney = new LatLng(Double.valueOf(latitude),Double.valueOf(longitude));
                            markers.add(mMap.addMarker(new MarkerOptions().position(sydney)));
                            buses.add(new Bus(jsonObject1.getString("drivername"),
                                    jsonObject1.getString("busno"),jsonObject1.getString("stops"),
                                    jsonObject1.getString("passengers")));
                        }
                    }
                }
                else
                {
                    Toast.makeText(MapsActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                }

                if(mylocation!=null)
                {
                    LatLng sydney = new LatLng(mylocation.getLatitude(),mylocation.getLongitude());
                  //  mMap.addMarker(new MarkerOptions().position(sydney).title("You are here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));

                }

            }
            catch (Exception e)
            {
                Toast.makeText(MapsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            mMap.setMyLocationEnabled(true);
        }
        else
            Toast.makeText(MapsActivity.this,"Location Permission Denied",Toast.LENGTH_SHORT).show();

    }
}

