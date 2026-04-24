package com.example.mobilafet.models;

import java.util.List;

public class RouteRequest {
    public List<List<Double>> coordinates;

    public RouteRequest(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
}