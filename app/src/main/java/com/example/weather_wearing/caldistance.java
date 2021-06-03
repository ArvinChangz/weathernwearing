package com.example.weather_wearing;



public class caldistance {
    public static double caldistance(double lat, double lon, double qw, double we) {
        double EARTH_RADIUS = 6378137.0; //地球半徑
        double radLat1 = (lat * Math.PI / 180.0);
        double radLat2 = (qw * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lon - we) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS / 1000;
        double km = (Math.round(s * 100.0) / 100.0);
        return km;
    }
}

