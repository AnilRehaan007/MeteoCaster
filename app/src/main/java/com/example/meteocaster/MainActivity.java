package com.example.meteocaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BroadcastReceiver networkChangeReceiver;
    public static int network_status=1;

    private void broadCast_caller()
    {
        networkChangeReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(TAG, "onReceive: called");

                int status=Network_Status.get_connectivity_status(context);

                if(status==Network_Status.TYPE_MOBILE)
                {

                    Log.d(TAG, "onReceive: mobile type");

                    network_status=1;
                }
                else if(status==Network_Status.TYPE_WIFI)
                {
                    Log.d(TAG, "onReceive: wifi");
                    network_status=1;
                }

                else if(status==Network_Status.TYPE_NOT_CONNECTED)
                {
                    network_status=Network_Status.TYPE_NOT_CONNECTED;
                    DialogShower dialogShower=new DialogShower(R.layout.internet_error,R.style.translate_animator,MainActivity.this);
                    dialogShower.showDialog();
                }

            }
        };

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         broadCast_caller();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new Introduction()).addToBackStack(null).commit();

       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {

               getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MainFrame()).addToBackStack(null).commit();

           }
       },2000);


    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(networkChangeReceiver);
    }
}