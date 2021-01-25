package com.example.meteocaster;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meteocaster.database.ClimateInformation;
import com.example.meteocaster.database.DataBase;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class MainFrame extends Fragment implements WeatherDataFetcher.CallBackFromWeatherDataFetcher{

    private static final String TAG = "MainFrame";
    private HourWeatherAdaptor hourWeatherAdaptor;
    private DailyWeatherAdaptor dailyWeatherAdaptor;
    private View main_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final Handler handler=new Handler();
    public static GeoLocator[] filter_geoLocator;
    int task_counter=0;
    public static final Map<WeatherDataFetcher.Status, List<Object>> filterData=new HashMap<>();

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int REQUEST_GRANT_PERMISSION = 2;
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    public static Location currentLocation;
    private LocationCallback locationCallback;

    enum Filter_Status
    {

        celsius,fahrenheit,hour_12,hour_24,default_Status
    }



    public MainFrame() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main_frame, container, false);
    }


    private ClimateInformation feedData() throws Exception
    {

        return new ClimateInformation(geoLocationConverter(MainFrame.filter_geoLocator),climateDataConverter());
    }


    private byte[] geoLocationConverter(GeoLocator[] geoLocators) throws Exception
    {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(geoLocators);

        return byteArrayOutputStream.toByteArray();

    }

    private byte[] climateDataConverter() throws Exception
    {

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(MainFrame.filterData);

        return byteArrayOutputStream.toByteArray();


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        main_view=view;
        DrawerLayout drawerLayout=view.findViewById(R.id.drawerLayout);
        Toolbar toolbar=view.findViewById(R.id.open_navigation);
        NavigationView navigationView=view.findViewById(R.id.navigation_bar);
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        RecyclerView hourly_holder=view.findViewById(R.id.hour_climate_holder);
        RecyclerView daily_holder=view.findViewById(R.id.daily_climate_holder);
        ImageView searchView=view.findViewById(R.id.searchview);
        hourly_holder.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        daily_holder.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyWeatherAdaptor=new DailyWeatherAdaptor(new ArrayList<>(),getContext());
        hourWeatherAdaptor=new HourWeatherAdaptor(new ArrayList<>(),getContext());
        hourly_holder.setAdapter(hourWeatherAdaptor);
        daily_holder.setAdapter(dailyWeatherAdaptor);

        Menu menuNav = navigationView.getMenu();

        MenuItem location_menu,temperature_menu,date_menu;
        location_menu= menuNav.findItem(R.id.location_menu);
        temperature_menu=menuNav.findItem(R.id.temperature_menu);
        date_menu=menuNav.findItem(R.id.time_menu);

        Dialog temperature_selected_box=new Dialog(getContext(),R.style.translate_animator);
        Dialog time_selected_box=new Dialog(getContext(),R.style.translate_animator);
        Timer timer=new Timer();

        location_menu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                createLocationRequest();
                settingsCheck();


                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

                    Log.d(TAG, "onMenuItemClick: requesting call");
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_GRANT_PERMISSION);

                }
                if(locationCallback==null)
                {
                    Log.d(TAG, "onMenuItemClick: location call back called");
                    buildLocationCallback();
                }
                if(currentLocation==null)

                {
                    Log.d(TAG, "onMenuItemClick: current call back");

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }

                 else
                {
                    Log.d(TAG, "onMenuItemClick: calling nothing");
                }

                return true;
            }
        });
        temperature_menu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                time_selected_box.dismiss();
                temperature_selected_box.setContentView(R.layout.select_temperature);
                temperature_selected_box.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                temperature_selected_box.setCanceledOnTouchOutside(true);
                Window window=temperature_selected_box.getWindow();
                window.setGravity(Gravity.CENTER);
                temperature_selected_box.show();

                (temperature_selected_box.findViewById(R.id.celsius)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                temperature_selected_box.dismiss();


                              handler.post(new Runnable() {
                                  @Override
                                  public void run() {

                                      HourWeatherAdaptor.filter_status=Filter_Status.celsius;
                                      DailyWeatherAdaptor.filter_status=Filter_Status.celsius;
                                      get_Weather_Data();
                                  }
                              });


                            }
                        },500);

                    }
                });

                (temperature_selected_box.findViewById(R.id.fahrenheit)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                temperature_selected_box.dismiss();

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        HourWeatherAdaptor.filter_status=Filter_Status.fahrenheit;
                                        DailyWeatherAdaptor.filter_status=Filter_Status.fahrenheit;
                                        get_Weather_Data();
                                    }
                                });


                            }
                        },500);

                    }
                });

                return true;
            }
        });
        date_menu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                temperature_selected_box.dismiss();
                time_selected_box.setContentView(R.layout.select_time);
                time_selected_box.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                time_selected_box.setCanceledOnTouchOutside(true);
                Window window=time_selected_box.getWindow();
                window.setGravity(Gravity.CENTER);
                time_selected_box.show();

                (time_selected_box.findViewById(R.id.hour_24)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                time_selected_box.dismiss();

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        HourWeatherAdaptor.filter_status=Filter_Status.hour_24;
                                        get_Weather_Data();
                                    }
                                });

                            }
                        },500);

                    }
                });

                (time_selected_box.findViewById(R.id.hour_12)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                time_selected_box.dismiss();

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        HourWeatherAdaptor.filter_status=Filter_Status.hour_12;
                                        get_Weather_Data();
                                    }
                                });

                            }
                        },500);

                    }
                });

                return true;
            }
        });



        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(getActivity(),drawerLayout,toolbar,R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog search_location_box=new Dialog(getContext(),R.style.translate_animator);
                search_location_box.setContentView(R.layout.search_location_box);
                search_location_box.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                search_location_box.setCanceledOnTouchOutside(false);
                Window window=search_location_box.getWindow();
                window.setGravity(Gravity.CENTER);
                search_location_box.show();
                ImageView loading=search_location_box.findViewById(R.id.loading);
                EditText location=search_location_box.findViewById(R.id.location);
                Button search=search_location_box.findViewById(R.id.search_location);
                loading.setVisibility(View.INVISIBLE);

               search_location_box.findViewById(R.id.minimize).setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                       search_location_box.dismiss();
                   }
               });

               search.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                       if(location.getText().length()==0)
                       {

                           location.setError("enter location");
                       }
                       else
                       {
                           location.setError(null);

                           if(MainActivity.network_status==Network_Status.TYPE_NOT_CONNECTED)
                           {


                               DialogShower dialogShower=new DialogShower(R.layout.internet_error,R.style.translate_animator,getContext());
                               dialogShower.showDialog();

                           }
                           else
                           {
                               loading.setVisibility(View.VISIBLE);
                               search.setEnabled(false);


                               new Thread(new Runnable() {
                                   @Override
                                   public void run() {


                                       try {
                                           GeoLocator[] geoLocators=new WeatherDataFetcher().getGeoLocators("http://api.openweathermap.org/geo/1.0/" +
                                                   "direct?q="+location.getText().toString()+"&appid="+ WeatherDataFetcher.data_fetching_key);

                                           handler.post(new Runnable() {
                                               @Override
                                               public void run() {

                                                   loading.setVisibility(View.INVISIBLE);
                                                   search.setEnabled(true);
                                               }
                                           });

                                           if(geoLocators.length!=0)
                                           {
                                               search_location_box.dismiss();
                                               new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Current,
                                                       MainFrame.this,location.getText().toString(),getContext())).start();
                                               new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Daily,
                                                       MainFrame.this,location.getText().toString(),getContext())).start();
                                               new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Hourly,
                                                       MainFrame.this,location.getText().toString(),getContext())).start();

                                           }
                                           else
                                           {
                                               handler.post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       loading.setVisibility(View.INVISIBLE);
                                                       search.setEnabled(true);
                                                       Toast.makeText(getContext(),"location not available",Toast.LENGTH_SHORT).show();
                                                   }
                                               });
                                           }

                                       } catch (Exception e) {

                                           handler.post(new Runnable() {
                                               @Override
                                               public void run() {
                                                   loading.setVisibility(View.INVISIBLE);
                                                   search.setEnabled(true);
                                                   Toast.makeText(getContext(),"location not found",Toast.LENGTH_SHORT).show();
                                               }
                                           });
                                       }


                                   }
                               }).start();
                           }
                       }
                   }
               });
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                  if(MainActivity.network_status==Network_Status.TYPE_NOT_CONNECTED)
                {
                    DialogShower dialogShower=new DialogShower(R.layout.internet_error,R.style.translate_animator,getContext());
                    dialogShower.showDialog();
                    swipeRefreshLayout.setRefreshing(false);
                }

             else
                  {
                      get_Weather_Data();
                  }

            }
        });

          get_Weather_Data();

    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Check for location settings
    public void settingsCheck() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                Log.d("TAG", "onSuccess: settingsCheck");
                getCurrentLocation();
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Log.d("TAG", "onFailure: settingsCheck");
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    public void getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
            {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_GRANT_PERMISSION);

            }
            else
            {

                AlertDialog.Builder builder=new AlertDialog.Builder(getContext())
                        .setMessage("Click ok than select permissions and set allow location").setNegativeButton("CANCEL", (dialogInterface, i) -> {

                            Log.d(TAG, "onRequestPermissionsResult: canceled");

                        }).setPositiveButton("OK", (dialogInterface, i) -> {

                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        });
                builder.setCancelable(false);
                builder.show();


            }
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("TAG", "onSuccess: getLastLocation");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentLocation=location;
                            new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Current,MainFrame.this,
                                    null,getContext())).start();
                            new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Daily,MainFrame.this,
                                    null,getContext())).start();
                            new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Hourly,MainFrame.this,
                                    null,getContext())).start();
                        }else{
                            Log.d("TAG", "location is null");
                            buildLocationCallback();
                        }
                    }
                });
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    currentLocation=location;
                }


                new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Current,MainFrame.this,
                        null,getContext())).start();
                new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Daily,MainFrame.this,
                        null,getContext())).start();
                new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Hourly,MainFrame.this,
                        null,getContext())).start();

            };
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: called");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {

            Log.d(TAG, "onResume: permission granted");
        }
        else
        {
            Log.d(TAG, "onResume: not granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_GRANT_PERMISSION){
            getCurrentLocation();
        }
        else {

            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
            {

               requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_GRANT_PERMISSION);

            }
            else
            {

                AlertDialog.Builder builder=new AlertDialog.Builder(getContext())
                        .setMessage("Click ok than select permissions and set allow location").setNegativeButton("CANCEL", (dialogInterface, i) -> {

                            Log.d(TAG, "onRequestPermissionsResult: canceled");

                        }).setPositiveButton("OK", (dialogInterface, i) -> {

                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        });
                builder.setCancelable(false);
                builder.show();


            }
        }
    }
    //called after user responds to location settings popup
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: ");
        if(requestCode==REQUEST_CHECK_SETTINGS && resultCode== Activity.RESULT_OK)
            getCurrentLocation();
        if(requestCode==REQUEST_CHECK_SETTINGS && resultCode==Activity.RESULT_CANCELED)
            Toast.makeText(getContext(), "Please enable Location settings...!!!", Toast.LENGTH_SHORT).show();
    }

    private void get_Weather_Data()
    {


        new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Current,this,"shimla",getContext())).start();
        new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Daily,this,"shimla",getContext())).start();
        new Thread(new WeatherDataFetcher(WeatherDataFetcher.Status.Hourly,this,"shimla",getContext())).start();


    }

    @Override
    public void confirmation(List<Object> data, WeatherDataFetcher.Status status,GeoLocator[] geoLocators) {


        Log.d(TAG, "confirmation: complete");

        swipeRefreshLayout.setRefreshing(false);

         handler.post(new Runnable() {
             @Override
             public void run() {


                 filter_geoLocator=geoLocators;

                 filterData.put(status,data);


                 if(status== WeatherDataFetcher.Status.Current && data!=null)
                 {

                     task_counter+=1;

                     update_main_frame(data,geoLocators);

                 }
                 else if(status== WeatherDataFetcher.Status.Daily && data!=null)
                 {
                     task_counter+=1;
                     dailyWeatherAdaptor.setDailyWeatherReports(data);
                 }
                 else if(status== WeatherDataFetcher.Status.Hourly && data!=null)
                 {
                     task_counter+=1;
                     hourWeatherAdaptor.setHourlyWeatherReports(data);
                 }

                 else if(DataBase.getInstance(getContext()).dao().get_information()!=0)
                 {

                     List<ClimateInformation> data=DataBase.getInstance(getContext()).dao().getClimateData();

                     for (ClimateInformation datum : data) {


                         try {
                             Map<WeatherDataFetcher.Status, List<Object>> final_data=get_climate_data(datum.getClimate_data());
                             GeoLocator[] geoLocators1=getGeoLocation(datum.getGeoLocator());

                             if(status== WeatherDataFetcher.Status.Current)
                             {

                                 List<Object> value=final_data.get(status);

                                 update_main_frame(value,geoLocators1);

                             }
                             else if(status== WeatherDataFetcher.Status.Daily)
                             {
                                 List<Object> value=final_data.get(status);

                                 dailyWeatherAdaptor.setDailyWeatherReports(value);
                             }
                             else if(status== WeatherDataFetcher.Status.Hourly)
                             {
                                 List<Object> value=final_data.get(status);

                                 hourWeatherAdaptor.setHourlyWeatherReports(value);
                             }

                         } catch (Exception e) {
                             Log.d(TAG, "run: unable to get data from database: " + e.getMessage());
                         }

                     }

                 }


                 if(task_counter==3)
                 {

                     DataBase.getInstance(getContext()).dao().delete();
                     task_counter=0;

                     try {
                         DataBase.getInstance(getContext()).dao().insertion(feedData());

                         Log.d(TAG, "onMenuItemClick: inserted");
                     } catch (Exception e) {
                         Log.d(TAG, "onMenuItemClick: unable to feed data: " + e.getMessage());
                     }

                 }

             }
         });

    }
    private Map<WeatherDataFetcher.Status, List<Object>> get_climate_data(byte[] data) throws Exception
    {

        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream=new ObjectInputStream(byteArrayInputStream);

       return (Map<WeatherDataFetcher.Status, List<Object>>)objectInputStream.readObject();
    }

    private GeoLocator[] getGeoLocation(byte[] data) throws Exception
    {

        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream=new ObjectInputStream(byteArrayInputStream);

        return (GeoLocator[])objectInputStream.readObject();


    }

    private void update_main_frame(List<Object> data,GeoLocator[] geoLocators)
    {

        TextView todayTemperature,todayDescription,todayWind,todayPressure,todayHumidity,todaySunrise,todaySunset,todayUvIndex,location;
        ImageView climateImage,house;
        RelativeLayout background_changer;
        todayTemperature=main_view.findViewById(R.id.todayTemperature);
        todayTemperature.setText(R.string._0_c);
        todayDescription=main_view.findViewById(R.id.todayDescription);
        todayDescription.setText(R.string.no_data);
        todayWind=main_view.findViewById(R.id.todayWind);
        todayWind.setText(R.string.wind_0_m_s);
        todayPressure=main_view.findViewById(R.id.todayPressure);
        todayPressure.setText(R.string.pressure_0_hpa);
        todayHumidity=main_view.findViewById(R.id.todayHumidity);
        todayHumidity.setText(R.string.humidity_0);
        todaySunrise=main_view.findViewById(R.id.todaySunrise);
        todaySunrise.setText(R.string.sunrise_00_00);
        todaySunset=main_view.findViewById(R.id.todaySunset);
        todaySunset.setText(R.string.sunset_00_00);
        todayUvIndex=main_view.findViewById(R.id.todayUvIndex);
        todayUvIndex.setText(R.string.uv_index_loading);
        location=main_view.findViewById(R.id.location);
        location.setText(R.string.location);
        background_changer=main_view.findViewById(R.id.status_holder);
        climateImage=main_view.findViewById(R.id.climateImage);
        house=main_view.findViewById(R.id.house);

        for (GeoLocator geoLocator : geoLocators) {

            location.setText(new String(geoLocator.getName()+"\t\t"+geoLocator.getCountry()));
        }


        for (Object datum : data) {

            DailyWeatherReport dailyWeatherReport=(DailyWeatherReport)datum;

            String temperature;

             if(HourWeatherAdaptor.filter_status==Filter_Status.default_Status || HourWeatherAdaptor.filter_status==Filter_Status.celsius)
             {

                 temperature=dailyWeatherAdaptor.tempConverter(dailyWeatherReport.getTemperature())+"\t°C";

             }
             else if(HourWeatherAdaptor.filter_status==Filter_Status.fahrenheit)
             {

                 temperature=dailyWeatherAdaptor.tempConverter(dailyWeatherReport.getTemperature())+"\t°F";
             }

             else if(DailyWeatherAdaptor.filter_status==Filter_Status.default_Status || DailyWeatherAdaptor.filter_status==Filter_Status.celsius)
             {

                 temperature=dailyWeatherAdaptor.tempConverter(dailyWeatherReport.getTemperature())+"\t°C";
             }

             else if(DailyWeatherAdaptor.filter_status==Filter_Status.fahrenheit)
             {

                 temperature=dailyWeatherAdaptor.tempConverter(dailyWeatherReport.getTemperature())+"\t°F";
             }

             else
             {
                 temperature=dailyWeatherAdaptor.tempConverter(dailyWeatherReport.getTemperature())+"\t°C";

             }
           todayTemperature.setText(temperature);
            todayDescription.setText(dailyWeatherReport.getDescription());

            todaySunrise.setText(todaySunrise.getText().toString().replaceAll("00:00",
                    (hourWeatherAdaptor.dateConverter(dailyWeatherReport.getSunrise(),1))));
            todaySunset.setText( todaySunset.getText().toString().replaceAll("00:00",
                    (hourWeatherAdaptor.dateConverter(dailyWeatherReport.getSunset(),1))));

            todayPressure.setText(todayPressure.getText().toString().replaceAll("0",String.valueOf(dailyWeatherReport.getPressure())));

            todayHumidity.setText(todayHumidity.getText().toString().replaceAll("0",String.valueOf(dailyWeatherReport.getHumidity())));

            todayUvIndex.setText(todayUvIndex.getText().toString().replaceAll("Loading...",get_uvRay_status(dailyWeatherReport.getUv_index())));

         todayWind.setText(todayWind.getText().toString().replaceAll("0",String.format(Locale.US,
                 "%.1f",Float.parseFloat(dailyWeatherReport.getWind()))));

         if(dailyWeatherReport.getDescription().equals("clear sky"))
         {

             if(background_status(dailyWeatherReport))
             {
                 Log.d(TAG, "update_main_frame: day");

                 climateImage.setImageResource(R.drawable.sun);

             }
             else
             {
                 Log.d(TAG, "update_main_frame: night");

                 climateImage.setImageResource(R.drawable.moon1);
             }

         }
         else
         {
             Picasso.with(getContext()).load(Uri.parse(dailyWeatherAdaptor.get_image_path(dailyWeatherReport
                     .getIcon_type()))).error(R.drawable.error_image).into(climateImage);

         }
          if(background_status(dailyWeatherReport))
          {

              background_changer.setBackgroundResource(R.drawable.day_background);
              house.setImageResource(R.drawable.day_landscape);

          }
          else
          {

              background_changer.setBackgroundResource(R.drawable.night_background);
              house.setImageResource(R.drawable.night_landscape);

          }

        }
    }

    private boolean background_status(DailyWeatherReport value)
    {
        return value.getDate_time()>=value.getSunrise() && value.getDate_time()<=value.getSunset();
    }

    private String get_uvRay_status(String value)

    {

        String status;

       int data=(int)Double.parseDouble(value);

       if(data>=0 && data<=2)
       {
           status="Low";

       }
       else if(data>=3 && data<=5)
       {
           status="Moderate";
       }
       else if(data>=6 && data<=7)
       {
           status="High";
       }
       else if(data>=8 && data<=10)
       {
           status="Very high";
       }
       else
       {
           status="Extreme";

       }

        return value+"\t("+status+")";
    }
}