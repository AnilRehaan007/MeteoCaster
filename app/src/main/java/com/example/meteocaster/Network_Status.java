package com.example.meteocaster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network_Status {
    public static final int TYPE_WIFI=1;
    public static final int TYPE_MOBILE=2;
    public static final int TYPE_NOT_CONNECTED=0;


    public static int get_connectivity_status(Context context)
    {

        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();

        if(networkInfo==null)
        {
            return TYPE_NOT_CONNECTED;
        }

        if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI)
        {
            return TYPE_WIFI;
        }
        else if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE)
        {
            return TYPE_MOBILE;
        }

        return TYPE_NOT_CONNECTED;
    }
}
