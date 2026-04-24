package com.example.mobilafet.models;

import java.util.List;

public class RouteResponse {
    public List<Route> routes;

    public static class Route {
        public Summary summary;
        public String geometry;
    }
    public static class Summary {
        public double distance;
        public double duration;
    }
}