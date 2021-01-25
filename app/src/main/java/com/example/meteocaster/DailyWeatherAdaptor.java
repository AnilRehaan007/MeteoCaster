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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DailyWeatherAdaptor extends RecyclerView.Adapter<DailyWeatherAdaptor.MyHolder> {

    private static final String TAG = "DailyWeatherAdaptor";

    private final List<DailyWeatherReport> dailyWeatherReports;
    private final Context context;
    public static MainFrame.Filter_Status filter_status;

    enum Converter
    {
        day,day_time
    }
    public DailyWeatherAdaptor(List<DailyWeatherReport> dailyWeatherReports, Context context) {
        this.dailyWeatherReports = dailyWeatherReports;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_data_holder,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.month.setText(dateConverter(dailyWeatherReports.get(position).getDate_time(),Converter.day_time));
        holder.day.setText(dateConverter(dailyWeatherReports.get(position).getDate_time(),Converter.day));

        String temperature;

        if(filter_status== MainFrame.Filter_Status.default_Status || filter_status== MainFrame.Filter_Status.celsius) {

            temperature=tempConverter(dailyWeatherReports.get(position).getTemperature())+"\t°C";

        }
        else if(filter_status== MainFrame.Filter_Status.fahrenheit)
        {
            temperature=tempConverter(dailyWeatherReports.get(position).getTemperature())+"\t°F";
        }

        else
        {
            temperature=tempConverter(dailyWeatherReports.get(position).getTemperature())+"\t°C";

        }

         holder.temp.setText(temperature);

        String main_description=dailyWeatherReports.get(position).getMain();

        if(main_description.equals("Thunderstorm"))
        {

            holder.main.setText(R.string.thunder);

        }
        else
        {
            holder.main.setText(dailyWeatherReports.get(position).getMain());

        }

        Picasso.with(context).load(Uri.parse(get_image_path(dailyWeatherReports.get(position)
                .getIcon_type()))).error(R.drawable.error_image).into(holder.icon);

    }

    public String tempConverter(String value)
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
    private String dateConverter(int date_time,Converter converter)
    {
        SimpleDateFormat sdf=null;
        java.util.Date date=null;

       if(converter==Converter.day_time)
       {
           Calendar calendar = Calendar.getInstance();
           TimeZone tz = TimeZone.getDefault();
           calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
           sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
           date=new java.util.Date((long)date_time*1000);

       }
       else if(converter==Converter.day)
       {
           Calendar calendar = Calendar.getInstance();
           TimeZone tz = TimeZone.getDefault();
           calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
           sdf = new SimpleDateFormat("EEE", Locale.getDefault());
           date=new java.util.Date((long)date_time*1000);

       }

        return sdf != null ? sdf.format(date):null;
    }

    public String get_image_path(String image_icon)
    {
        return "http://openweathermap.org/img/wn/"+image_icon+"@2x.png";

    }

    @Override
    public int getItemCount() {
        return dailyWeatherReports!=null && dailyWeatherReports.size()>0 ? dailyWeatherReports.size():0;
    }

    public void setDailyWeatherReports(List<Object> dailyWeatherReports)
    {
        Log.d(TAG, "setHourlyWeatherReports: called");

        if(this.dailyWeatherReports.size()>0)
        {
            this.dailyWeatherReports.clear();
        }

        for (Object dailyWeatherReport : dailyWeatherReports) {

            this.dailyWeatherReports.add((DailyWeatherReport) dailyWeatherReport);
        }

        notifyDataSetChanged();

    }

    static class MyHolder extends RecyclerView.ViewHolder
    {

        TextView month,day,temp,main;
        ImageView icon;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            month=itemView.findViewById(R.id.month);
            day=itemView.findViewById(R.id.day);
            temp=itemView.findViewById(R.id.temp);
            main=itemView.findViewById(R.id.main);
            icon=itemView.findViewById(R.id.daily_weather_icon);
        }
    }
}
