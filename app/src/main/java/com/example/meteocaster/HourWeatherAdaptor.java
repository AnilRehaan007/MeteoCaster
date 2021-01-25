package com.example.meteocaster;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class HourWeatherAdaptor extends RecyclerView.Adapter<HourWeatherAdaptor.MyHolder> {

    private static final String TAG = "HourWeatherAdaptor";

    private final List<HourlyWeatherReport> hourlyWeatherReports;
    private final Context context;
    public static MainFrame.Filter_Status filter_status;

    public HourWeatherAdaptor(List<HourlyWeatherReport> hourlyWeatherReports,Context context) {
        this.hourlyWeatherReports = hourlyWeatherReports;
        this.context=context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_data_holder,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String temperature;

        if(filter_status== MainFrame.Filter_Status.default_Status || filter_status== MainFrame.Filter_Status.celsius) {

            temperature=tempConverter(hourlyWeatherReports.get(position).getTemperature())+"\t°C";
        }
       else if(filter_status== MainFrame.Filter_Status.fahrenheit)
        {
            temperature=tempConverter(hourlyWeatherReports.get(position).getTemperature())+"\t°F";
        }

       else if(DailyWeatherAdaptor.filter_status== MainFrame.Filter_Status.celsius)
        {
            temperature=tempConverter(hourlyWeatherReports.get(position).getTemperature())+"\t°C";

        }
        else if(DailyWeatherAdaptor.filter_status== MainFrame.Filter_Status.fahrenheit)
        {
            temperature=tempConverter(hourlyWeatherReports.get(position).getTemperature())+"\t°F";

        }
        else
        {
            temperature=tempConverter(hourlyWeatherReports.get(position).getTemperature())+"\t°C";
        }
        holder.hourly_temp.setText(temperature);

        holder.hourly_time.setText(dateConverter(hourlyWeatherReports.get(position).getDate_time(),0));


            Picasso.with(context).load(Uri.parse(get_image_path(hourlyWeatherReports.get(position)
                    .getIcon_type()))).error(R.drawable.error_image).into(holder.hourly_image);

    }


    private String tempConverter(String value)
    {

        float data=Float.parseFloat(value);

        if(filter_status== MainFrame.Filter_Status.default_Status)
        {

            return String.format(Locale.US,"%.1f",(float) (data - 273.15));
        }
        else if(filter_status== MainFrame.Filter_Status.fahrenheit)
        {



            return String.format(Locale.US,"%.1f",(float) (data-273.15)* 9/5 + 32);

        }
        else if(filter_status== MainFrame.Filter_Status.celsius)
        {


            return String.format(Locale.US,"%.1f",(float) (data - 273.15));

        }



        return String.format(Locale.US,"%.1f",(float) (data - 273.15));

    }

    public String dateConverter(int date_time,int by_pass)
    {

        SimpleDateFormat sdf=null;
        java.util.Date date=null;

      if(filter_status== MainFrame.Filter_Status.default_Status)
      {
          if(by_pass==0)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);

          }
          else if(by_pass==1)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("hh:mm:a", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);


          }
      }

      else if(filter_status== MainFrame.Filter_Status.hour_24)
      {
          if(by_pass==0)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);

          }
          else if(by_pass==1)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("HH:mm:a", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);


          }

      }
      else if(filter_status== MainFrame.Filter_Status.hour_12)
      {
          if(by_pass==0)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);

          }
          else if(by_pass==1)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("hh:mm:a", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);


          }
      }

      else
      {

          if(by_pass==0)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);

          }
          else if(by_pass==1)
          {
              Calendar calendar = Calendar.getInstance();
              TimeZone tz = TimeZone.getDefault();
              calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
              sdf = new SimpleDateFormat("hh:mm:a", Locale.getDefault());
              date=new java.util.Date((long)date_time*1000);


          }

      }


        return sdf != null ? sdf.format(date):null;
    }

    private String get_image_path(String image_icon)
    {
      return "http://openweathermap.org/img/wn/"+image_icon+"@2x.png";

    }
    @Override
    public int getItemCount() {
        return hourlyWeatherReports!=null && hourlyWeatherReports.size()>0 ? hourlyWeatherReports.size():0;
    }

    public void setHourlyWeatherReports(List<Object> hourlyWeatherReports)
    {
        Log.d(TAG, "setHourlyWeatherReports: called");

        if(this.hourlyWeatherReports.size()>0)
        {
           this.hourlyWeatherReports.clear();

        }

        for (Object hourlyWeatherReport : hourlyWeatherReports) {

            this.hourlyWeatherReports.add((HourlyWeatherReport)hourlyWeatherReport);
        }

        notifyDataSetChanged();

    }
    static class MyHolder extends RecyclerView.ViewHolder
    {

         ImageView hourly_image;
         TextView hourly_temp,hourly_time;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            hourly_image=itemView.findViewById(R.id.weather_icon);
            hourly_temp=itemView.findViewById(R.id.hourly_temperature);
            hourly_time=itemView.findViewById(R.id.hourly_time);
        }
    }
}
