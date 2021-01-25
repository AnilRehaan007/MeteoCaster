package com.example.meteocaster;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherDataFetcher implements Runnable {

    private static final String TAG = "WeatherDataFetcher";

 private  Status status;
 private  String location;
 private  CallBackFromWeatherDataFetcher callBackFromWeatherDataFetcher;
 private GeoLocator[] geoLocators;
 public static final String data_fetching_key = "b035e3e3c099729feb2d06c11390b0d9";
 private Context context;

    public WeatherDataFetcher(Status status, CallBackFromWeatherDataFetcher callBackFromWeatherDataFetcher,String location,Context context) {
        this.status = status;
        this.callBackFromWeatherDataFetcher = callBackFromWeatherDataFetcher;
        this.location=location;
        this.context=context;
    }

    public WeatherDataFetcher() {

        //use to call functions which just need a object to call
    }

    interface CallBackFromWeatherDataFetcher
    {

        void confirmation(List<Object> data,Status status,GeoLocator[] geoLocators);

    }

    enum Status
    {
        Current,Hourly,Daily
    }

    enum JsonDataKey
    {
        dt, sunrise, sunset, temp, feels_like, pressure, humidity, dew_point, uvi, clouds, visibility,
        wind_speed, wind_deg,main, description, icon,

    }

    @Override
    public void run() {

        switch (status)
        {
            case Current:

                if(MainActivity.network_status==Network_Status.TYPE_NOT_CONNECTED)
                {

                   if(callBackFromWeatherDataFetcher!=null)
                   {

                      List<Object> data=MainFrame.filterData.get(Status.Current);

                      callBackFromWeatherDataFetcher.confirmation(data,Status.Current,MainFrame.filter_geoLocator);


                   }

                }
                else
                {

                    try {

                        start_operation(Status.Current);

                    } catch (Exception e) {

                        Log.d(TAG, "run: unable to start operation: " + e.getMessage());
                    }

                }


                break;

            case Hourly:

                if(MainActivity.network_status==Network_Status.TYPE_NOT_CONNECTED)
                {

                    if(callBackFromWeatherDataFetcher!=null)
                    {

                        List<Object> data=MainFrame.filterData.get(Status.Hourly);

                        callBackFromWeatherDataFetcher.confirmation(data,Status.Hourly,MainFrame.filter_geoLocator);


                    }

                }

                else
                {
                    try {

                        start_operation(Status.Hourly);

                    } catch (Exception e) {

                        Log.d(TAG, "run: unable to start operation: " + e.getMessage());
                    }

                }



                break;

            case Daily:

                if(MainActivity.network_status==Network_Status.TYPE_NOT_CONNECTED)
                {

                    if(callBackFromWeatherDataFetcher!=null)
                    {

                        List<Object> data=MainFrame.filterData.get(Status.Daily);

                        callBackFromWeatherDataFetcher.confirmation(data,Status.Daily,MainFrame.filter_geoLocator);


                    }

                }


                else
                {

                    try {

                        start_operation(Status.Daily);

                    } catch (Exception e) {

                        Log.d(TAG, "run: unable to start operation: " + e.getMessage());
                    }

                }


                break;
        }


    }

    private void start_operation(Status status)
    {
        String data_fetching_key = "b035e3e3c099729feb2d06c11390b0d9";

        if(status==Status.Current || status==Status.Daily)
       {

           if(location!=null)
           {
               try {
                   GeoLocator[] geoLocators=getGeoLocators("http://api.openweathermap.org/geo/1.0/" +
                           "direct?q="+location+"&appid="+ data_fetching_key);

                   this.geoLocators=geoLocators;

                   for (GeoLocator geoLocator : geoLocators) {

                       jsonToDataConverter(get_json_data("https://api.openweathermap.org/data/2.5/onecall?lat=" +
                               ""+geoLocator.getLat()+"&lon="+geoLocator.getLon()+"&exclude=hourly&appid="+ data_fetching_key),status);


                   }

               } catch (Exception e) {

                   if(callBackFromWeatherDataFetcher!=null)
                   {

                       List<Object> data=MainFrame.filterData.get(status);

                       callBackFromWeatherDataFetcher.confirmation(data,status,MainFrame.filter_geoLocator);


                   }
               }

           }

           else
           {

               try {

                   GeoLocator geoLocator=new GeoLocator();

                   double lat=MainFrame.currentLocation.getLatitude();
                   double log=MainFrame.currentLocation.getLongitude();

                   Geocoder geocoder=new Geocoder(context,Locale.getDefault());

                   List<Address> addressList=geocoder.getFromLocation(lat,log,1);

                    geoLocator.setCountry(addressList.get(0).getCountryName());
                    geoLocator.setName(addressList.get(0).getLocality());
                    geoLocator.setLon(lat);
                    geoLocator.setLon(log);

                   GeoLocator[] geoLocators={geoLocator};

                   this.geoLocators=geoLocators;

                   for (GeoLocator locator : geoLocators) {

                       jsonToDataConverter(get_json_data("https://api.openweathermap.org/data/2.5/onecall?lat=" +
                               ""+locator.getLat()+"&lon="+locator.getLon()+"&exclude=hourly&appid="+ data_fetching_key),status);


                   }


               } catch (Exception e) {

                   if(callBackFromWeatherDataFetcher!=null)
                   {

                       List<Object> data=MainFrame.filterData.get(status);

                       callBackFromWeatherDataFetcher.confirmation(data,status,MainFrame.filter_geoLocator);


                   }
               }
           }


       }
       else if(status==Status.Hourly)
       {
           if(location!=null)
           {
               try {
                   GeoLocator[] geoLocators=getGeoLocators("http://api.openweathermap.org/geo/1.0/" +
                           "direct?q="+location+"&appid="+ data_fetching_key);

                   this.geoLocators=geoLocators;

                   for (GeoLocator geoLocator : geoLocators) {

                       jsonToDataConverter(get_json_data("https://api.openweathermap.org/data/2.5/onecall?lat=" +
                               ""+geoLocator.getLat()+"&lon="+geoLocator.getLon()+"&exclude=daily&appid="+ data_fetching_key),status);


                   }

               } catch (Exception e) {

                   if(callBackFromWeatherDataFetcher!=null)
                   {

                       List<Object> data=MainFrame.filterData.get(status);

                       callBackFromWeatherDataFetcher.confirmation(data,status,MainFrame.filter_geoLocator);


                   }
               }

           }

           else
           {
               try {

                   GeoLocator geoLocator=new GeoLocator();

                   double lat=MainFrame.currentLocation.getLatitude();
                   double log=MainFrame.currentLocation.getLongitude();

                   Geocoder geocoder=new Geocoder(context,Locale.getDefault());

                   List<Address> addressList=geocoder.getFromLocation(lat,log,1);

                   geoLocator.setCountry(addressList.get(0).getCountryName());
                   geoLocator.setName(addressList.get(0).getLocality());
                   geoLocator.setLon(lat);
                   geoLocator.setLon(log);

                   GeoLocator[] geoLocators={geoLocator};

                   this.geoLocators=geoLocators;

                   for (GeoLocator locator : geoLocators) {

                       jsonToDataConverter(get_json_data("https://api.openweathermap.org/data/2.5/onecall?lat=" +
                               ""+locator.getLat()+"&lon="+locator.getLon()+"&exclude=daily&appid="+ data_fetching_key),status);


                   }


               } catch (Exception e) {

                   if(callBackFromWeatherDataFetcher!=null)
                   {

                       List<Object> data=MainFrame.filterData.get(status);

                       callBackFromWeatherDataFetcher.confirmation(data,status,MainFrame.filter_geoLocator);


                   }
               }
           }

       }

    }

    public GeoLocator[] getGeoLocators(String url) throws Exception
    {

        return new Gson().fromJson(get_json_data(url),GeoLocator[].class);

    }

    private String get_json_data(String url) throws Exception
    {

        HttpURLConnection httpURLConnection=(HttpURLConnection)new URL(url).openConnection();
        httpURLConnection.setReadTimeout(15000);
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();
        int response=httpURLConnection.getResponseCode();

        if(response!=200)
        {
            Log.d(TAG, "get_json_data: response code error: " + response);
        }

        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder jsonData=new StringBuilder();

        for(String value=bufferedReader.readLine();value!=null;value=bufferedReader.readLine())
        {

            jsonData.append(value).append("\n");
        }

      return jsonData.toString();
    }

 private void jsonToDataConverter(String json,Status status) throws Exception
 {

     DailyWeatherReport hourly_weather_report;

     switch (status)
     {
         case Current:

           hourly_weather_report=new DailyWeatherReport();

             JSONObject jsonObject1=new JSONObject(json);

             hourly_weather_report.setSunrise((Integer)jsonObject1.getJSONObject("current").get(JsonDataKey.sunrise.toString()));
             hourly_weather_report.setSunset((Integer)jsonObject1.getJSONObject("current").get(JsonDataKey.sunset.toString()));
             hourly_weather_report.setTemperature(String.valueOf(jsonObject1.getJSONObject("current").get(JsonDataKey.temp.toString())));
             hourly_weather_report.setWind(String.valueOf(jsonObject1.getJSONObject("current").get(JsonDataKey.wind_speed.toString())));
             hourly_weather_report.setPressure((Integer)jsonObject1.getJSONObject("current").get(JsonDataKey.pressure.toString()));
             hourly_weather_report.setUv_index(String.valueOf(jsonObject1.getJSONObject("current").get(JsonDataKey.uvi.toString())));
             hourly_weather_report.setHumidity((Integer)jsonObject1.getJSONObject("current").get(JsonDataKey.humidity.toString()));
             hourly_weather_report.setDate_time((Integer)jsonObject1.getJSONObject("current").get(JsonDataKey.dt.toString()));

             JSONArray current_json_array=jsonObject1.getJSONObject("current").getJSONArray("weather");

             for(int i=0;i<current_json_array.length();i++)
             {

                 JSONObject inner_json_object=current_json_array.getJSONObject(i);

                 hourly_weather_report.setDescription((String)inner_json_object.get(JsonDataKey.description.toString()));
                 hourly_weather_report.setIcon_type((String) inner_json_object.get(JsonDataKey.icon.toString()));
                 hourly_weather_report.setMain((String) inner_json_object.get(JsonDataKey.main.toString()));

             }

             if(callBackFromWeatherDataFetcher!=null)
             {

                 ArrayList<Object> dailyWeatherReports=new ArrayList<>();
                 dailyWeatherReports.add(hourly_weather_report);

                 callBackFromWeatherDataFetcher.confirmation(dailyWeatherReports,Status.Current,geoLocators);
             }

             break;

         case Hourly:

             JSONObject hourly_json_object=new JSONObject(json);
             ArrayList<Object> hourlyWeatherReports=new ArrayList<>();
             JSONArray hourly_json_array=hourly_json_object.getJSONArray("hourly");

             for(int i=0;i<hourly_json_array.length();i++)
             {
                HourlyWeatherReport hourly_weather_reports=new HourlyWeatherReport();

                 JSONObject inner_json_object2=hourly_json_array.getJSONObject(i);

                 hourly_weather_reports.setDate_time((Integer)inner_json_object2.get(JsonDataKey.dt.toString()));
                 hourly_weather_reports.setTemperature(String.valueOf(inner_json_object2.get(JsonDataKey.temp.toString())));

                 JSONArray jsonArray1=inner_json_object2.getJSONArray("weather");

                 for(int j=0;j<jsonArray1.length();j++)
                 {

                     inner_json_object2=jsonArray1.getJSONObject(j);

                     hourly_weather_reports.setIcon_type((String) inner_json_object2.get(JsonDataKey.icon.toString()));
                     hourly_weather_reports.setMain((String) inner_json_object2.get(JsonDataKey.main.toString()));
                     hourly_weather_reports.setDescription((String)inner_json_object2.get(JsonDataKey.description.toString()));
                 }

                 hourlyWeatherReports.add(hourly_weather_reports);

             }

             if(callBackFromWeatherDataFetcher!=null)
             {

                 callBackFromWeatherDataFetcher.confirmation(hourlyWeatherReports,Status.Hourly,geoLocators);
             }

             break;

         case Daily:

             JSONObject daily_json_object=new JSONObject(json);

             ArrayList<Object> dailyWeatherReports=new ArrayList<>();

             JSONArray daily_json_array=daily_json_object.getJSONArray("daily");

             for(int i=0;i<daily_json_array.length();i++)
             {
                 hourly_weather_report=new DailyWeatherReport();

                 JSONObject inner_json_object1=daily_json_array.getJSONObject(i);

                 hourly_weather_report.setSunrise((Integer)inner_json_object1.get(JsonDataKey.sunrise.toString()));
                 hourly_weather_report.setSunset((Integer)inner_json_object1.get(JsonDataKey.sunset.toString()));
                 hourly_weather_report.setTemperature(String.valueOf(inner_json_object1.getJSONObject(JsonDataKey.temp.toString()).get("day")));
                 hourly_weather_report.setWind(String.valueOf(inner_json_object1.get(JsonDataKey.wind_speed.toString())));
                 hourly_weather_report.setPressure((Integer)inner_json_object1.get(JsonDataKey.pressure.toString()));
                 hourly_weather_report.setUv_index(String.valueOf(inner_json_object1.get(JsonDataKey.uvi.toString())));
                 hourly_weather_report.setHumidity((Integer)inner_json_object1.get(JsonDataKey.humidity.toString()));
                 hourly_weather_report.setDate_time((Integer)inner_json_object1.get(JsonDataKey.dt.toString()));


                 JSONArray jsonArray1=inner_json_object1.getJSONArray("weather");

                 for(int j=0;j<jsonArray1.length();j++)
                 {

                     inner_json_object1=jsonArray1.getJSONObject(j);

                     hourly_weather_report.setDescription((String)inner_json_object1.get(JsonDataKey.description.toString()));
                     hourly_weather_report.setIcon_type((String) inner_json_object1.get(JsonDataKey.icon.toString()));
                     hourly_weather_report.setMain((String) inner_json_object1.get(JsonDataKey.main.toString()));
                 }

                 dailyWeatherReports.add(hourly_weather_report);

             }

             if(callBackFromWeatherDataFetcher!=null)
             {

                 callBackFromWeatherDataFetcher.confirmation(dailyWeatherReports,Status.Daily,geoLocators);
             }



             break;
     }

 }
}
