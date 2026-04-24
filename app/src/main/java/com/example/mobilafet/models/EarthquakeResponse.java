package com.example.mobilafet.models;

import java.util.List;

public class EarthquakeResponse {
    public List<Feature> features;

    public static class Feature {
        public Properties properties;
        public Geometry geometry;
    }
    public static class Properties {
        public double mag;
        public String place;
        public long time;
    }
    public static class Geometry {
        public List<Double> coordinates; // Order: [longitude, latitude, depth]
    }
}