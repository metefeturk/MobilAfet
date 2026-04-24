package com.example.mobilafet.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public Wind wind;
    public Rain rain;
    public Clouds clouds;
    
    public static class Main {
        public double temp;
        public double humidity;
    }
    public static class Weather {
        public String main;
        public String description;
    }
    public static class Wind {
        public double speed;
    }
    public static class Rain {
        @SerializedName("1h")
        public Double oneHour; // Using Double instead of double allows it to be safely null
    }
    public static class Clouds {
        public int all;
    }
}