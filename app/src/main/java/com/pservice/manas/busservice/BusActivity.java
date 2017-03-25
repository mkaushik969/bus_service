package com.pservice.manas.busservice;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

public class BusActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    TextView t1,t2,t3;
    SharedPreferences sp;
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    RadioGroup radioGroup;
    RadioButton r1,r2,r3,rbtn;
    String status;
    Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Your location");


        sp=getSharedPreferences("BusServices",MODE_PRIVATE);
        t1=(TextView)findViewById(R.id.ba_cv_name2);
        t2=(TextView)findViewById(R.id.ba_cv_bno2);
        t3=(TextView)findViewById(R.id.ba_cv_stops2);

        radioGroup=(RadioGroup) findViewById(R.id.ba_cv_rbg);
        r1=(RadioButton) findViewById(R.id.ba_cv_rb1);
        r2=(RadioButton) findViewById(R.id.ba_cv_rb2);
        r3=(RadioButton) findViewById(R.id.ba_cv_rb3);

        status=sp.getString("passengers","");
        if(status.equals("low"))
            r1.setChecked(true);
        else if(status.equals("medium"))
            r2.setChecked(true);
        else if(status.equals("high"))
            r3.setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                try
                {
                    rbtn=(RadioButton)findViewById(checkedId);
//                Toast.makeText(BusActivity.this,rbtn.getText().toString(),Toast.LENGTH_SHORT).show();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("busno", sp.getString("busno",""));
                    jsonObject.put("passenger", rbtn.getText().toString().toLowerCase());
                    String response = JSONParser.getParsedJson(jsonObject);
                    new StatusTask().execute(response);
                }
                catch (Exception e)
                {
                    Toast.makeText(BusActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},1);

            } else
                startService(new Intent(this,LocationService.class));
        }
        else
            startService(new Intent(this,LocationService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();

        t1.setText(sp.getString("drivername",""));
        t2.setText(sp.getString("busno",""));
        t3.setText(sp.getString("stops",""));

    }

    @Override
    public void onLocationChanged(Location location) {

  //      Toast.makeText(getApplicationContext(),"BA:Location Changed",Toast.LENGTH_SHORT).show();
        LatLng sydney = new LatLng(location.getLatitude(),location.getLongitude());
//        mMap.addMarker(new MarkerOptions().position(sydney).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        if(mMap!=null)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    //    Toast.makeText(getApplicationContext(), "BA:Location Service connected", Toast.LENGTH_SHORT).show();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
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
  //      Toast.makeText(getApplicationContext(), "BA:Location Service conn suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
 //       Toast.makeText(getApplicationContext(), "BA:Location Service conn failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            startService(new Intent(this,LocationService.class));
        }
        else
            Toast.makeText(BusActivity.this,"Location Permission Denied",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_bus,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.menu_bus_edp)
        {
            startActivity(new Intent(this,EditProfileActivity.class));
            return true;
        }
        else if(item.getItemId()==R.id.menu_bus_logout)
        {
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("drivername","");
            editor.putString("stops","");
            editor.putString("busno","");
            editor.putString("password","");
            editor.putString("latitude","");
            editor.putString("longitude","");
            editor.putString("passengers","");
            editor.apply();

            Toast.makeText(BusActivity.this,"Successfully logged out",Toast.LENGTH_SHORT).show();

            finish();
            return true;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,LocationService.class));

        SharedPreferences.Editor editor=sp.edit();
        editor.putString("drivername","");
        editor.putString("stops","");
        editor.putString("busno","");
        editor.putString("password","");
        editor.putString("latitude","");
        editor.putString("longitude","");
        editor.putString("passengers","");
        editor.apply();

        Toast.makeText(BusActivity.this,"Successfully logged out",Toast.LENGTH_SHORT).show();
    }

    class StatusTask extends AsyncTask<String,String,String>
    {
        String response;
        @Override
        protected String doInBackground(String... params) {
            try {

                response = ServletInterface.makeRequest(params[0], "http://www.myprojectshub.com/busbackend/updatepassenger.php");

                return response;
            }
            catch (final Exception e)
            {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BusActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                return "EXCP";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try
            {
                //           Toast.makeText(EditProfileActivity.this,"RES:"+s,Toast.LENGTH_SHORT).show();
                JSONObject jsonObject=new JSONObject(s);

                if(jsonObject.getString("status").equals("failure"))
                {
                    Toast.makeText(BusActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                }
                else if(jsonObject.getString("status").equals("success"))
                {
                    Toast.makeText(BusActivity.this,"Status Updated",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(BusActivity.this,"Some error occurred",Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(BusActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

    }
}
