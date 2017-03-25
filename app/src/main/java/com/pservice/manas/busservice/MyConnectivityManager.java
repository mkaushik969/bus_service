package com.pservice.manas.busservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by HP on 29-11-2016.
 */

public class MyConnectivityManager {

    public static boolean getConnectivity(Context context)
    {
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info[]=cm.getAllNetworkInfo();
        if(info[0].getState()== NetworkInfo.State.CONNECTED || info[0].getState()== NetworkInfo.State.CONNECTING || info[1].getState()== NetworkInfo.State.CONNECTED || info[1].getState()== NetworkInfo.State.CONNECTING)
            return true;
        else
            return false;
    }

    public static String getActiveNetwork(Context context)
    {
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        return info.getTypeName();
    }
}
