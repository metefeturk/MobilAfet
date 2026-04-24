package com.example.mobilafet.parsers;

import com.example.mobilafet.models.FireHotspot;

import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    /**
     * Safely parses the NASA FIRMS CSV data into a list of FireHotspot models.
     */
    public static List<FireHotspot> parseFirmsCsv(String csvData) {
        List<FireHotspot> hotspots = new ArrayList<>();
        if (csvData == null || csvData.trim().isEmpty()) {
            return hotspots;
        }
        
        String[] lines = csvData.split("\n");
        // Skip the header (index 0)
        for (int i = 1; i < lines.length; i++) {
            String[] tokens = lines[i].split(",");
            // The default FIRMS CSV format provides latitude and longitude at indices 0 and 1
            if (tokens.length >= 7) {
                try {
                    FireHotspot hotspot = new FireHotspot();
                    hotspot.latitude = Double.parseDouble(tokens[0]);
                    hotspot.longitude = Double.parseDouble(tokens[1]);
                    hotspot.brightness = Double.parseDouble(tokens[2]);
                    hotspot.acqDate = tokens[5];
                    hotspot.acqTime = tokens[6];
                    hotspots.add(hotspot);
                } catch (Exception e) {
                    // Safely ignore rows that fail to parse
                }
            }
        }
        return hotspots;
    }
}