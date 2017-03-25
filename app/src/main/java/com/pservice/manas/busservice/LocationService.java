package com.pservice.manas.busservice;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleApiClient;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    public LocationService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

       //     Toast.makeText(getApplicationContext(),"Location Service started",Toast.LENGTH_SHORT).show();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        sp=getSharedPreferences("BusServices",MODE_PRIVATE);
        editor=sp.edit();

        return START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        try {

            editor.putString("latitude",location.getLatitude()+"");
            editor.putString("longitude",location.getLongitude()+"");
            editor.apply();

   //         Toast.makeText(getApplicationContext(),"Location Changed",Toast.LENGTH_SHORT).show();
            if(MyConnectivityManager.getConnectivity(getApplicationContext()))
            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", location.getLatitude());
                jsonObject.put("longitude", location.getLongitude());
                jsonObject.put("drivername", sp.getString("drivername",""));
                jsonObject.put("busno", sp.getString("busno",""));
                jsonObject.put("password", sp.getString("password",""));
                jsonObject.put("stops", sp.getString("stops",""));
                String response = JSONParser.getParsedJson(jsonObject);

                new LocationTask().execute(response);
        }
        }
        catch (Exception e)
        {

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

   //     Toast.makeText(getApplicationContext(),"Location Service connected",Toast.LENGTH_SHORT).show();

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
  //      Toast.makeText(getApplicationContext(),"Location Service conn suspended",Toast.LENGTH_SHORT).show();
        stopSelf();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
  //      Toast.makeText(getApplicationContext(),"Location Service conn failed",Toast.LENGTH_SHORT).show();

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

  //      Toast.makeText(getApplicationContext(),"Location Service stopped",Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    class LocationTask extends AsyncTask<String,String,String> {
        String response;

        @Override
        protected String doInBackground(String... params) {
            try {
                response = ServletInterface.makeRequest(params[0], "http://www.myprojectshub.com/busbackend/updatebus.php");
                return response;
            } catch (final Exception e) {
               return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try
            {
     //           Toast.makeText(getApplicationContext(),"LS:"+s,Toast.LENGTH_SHORT).show();

                JSONObject jsonObject=new JSONObject(s);

                if(jsonObject.getString("status").equals("failure"))
                {
   //                 Toast.makeText(getApplicationContext(),"Location Not updated",Toast.LENGTH_SHORT).show();
                }
                else if(jsonObject.getString("status").equals("success"))
                {
   //                 Toast.makeText(getApplicationContext(),"Location  updated",Toast.LENGTH_SHORT).show();
                }
                else
                {
   //                 Toast.makeText(getApplicationContext(),"Some error occurred",Toast.LENGTH_SHORT).show();
                }

            }
            catch (Exception e)
            {
  //              Toast.makeText(getApplicationContext(),"LOCEXC:"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

    }

    }
