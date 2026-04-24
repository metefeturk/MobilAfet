
package com.example.mobilafet.utils;

/**
 * Centralized configuration for all API endpoints and keys.
 * All API URLs and keys are stored here.
 */
public class ApiConfig {

    // 🌍 USGS (Deprem)
    public static final String USGS_BASE_URL = "https://earthquake.usgs.gov/";

    // 🌦️ OpenWeather (Hava)
    public static final String OPENWEATHER_BASE_URL = "https://api.openweathermap.org/";
    public static final String OPENWEATHER_API_KEY = "";

    // 🧭 OpenRouteService (Rota / Tahliye)
    public static final String OPENROUTESERVICE_BASE_URL = "https://api.openrouteservice.org/";
    public static final String OPENROUTESERVICE_API_KEY =
            "=";

    // 🗺️ MapTiler (Harita)
    public static final String MAPTILER_API_KEY = "";

    // Harita style (ana kullanılacak)
    public static final String MAPTILER_STYLE_URL =
            "https://api.maptiler.com/maps/streets/style.json?key=" + MAPTILER_API_KEY;

    // Tile URL (opsiyonel)
    public static final String MAPTILER_TILE_URL =
            "https://api.maptiler.com/maps/streets/256/{z}/{x}/{y}.png?key=" + MAPTILER_API_KEY;

    // 🔥 NASA FIRMS (Yangın)
    public static final String FIRMS_BASE_URL = "https://firms.modaps.eosdis.nasa.gov/";
    public static final String FIRMS_API_KEY = "";
}