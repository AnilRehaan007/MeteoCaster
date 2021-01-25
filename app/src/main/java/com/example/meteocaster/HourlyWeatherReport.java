package com.example.meteocaster;

import java.io.Serializable;

public class HourlyWeatherReport implements Serializable {

   private int date_time;
   private String icon_type,main,temperature,description;


   public int getDate_time() {
      return date_time;
   }

   public void setDate_time(int date_time) {
      this.date_time = date_time;
   }

   public String getIcon_type() {
      return icon_type;
   }

   public void setIcon_type(String icon_type) {
      this.icon_type = icon_type;
   }

   public String getMain() {
      return main;
   }

   public void setMain(String main) {
      this.main = main;
   }

   public String getTemperature() {
      return temperature;
   }

   public void setTemperature(String temperature) {
      this.temperature = temperature;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }
}
