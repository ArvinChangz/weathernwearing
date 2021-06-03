package com.example.weather_wearing;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;

public class DataActivity extends AppCompatActivity {
    //天氣資料element
    ArrayList<String> station = new ArrayList<>(); //測站名
    //測站地址location
    ArrayList<String> stationname = new ArrayList<>();
    ArrayList<String> address = new ArrayList<>();
    ArrayList<Double> latt = new ArrayList<>();
    ArrayList<Double> lonn = new ArrayList<>();
    //比對後呈現 所有測站
    ArrayList<String> stationn = new ArrayList<>();
    ArrayList<String> add = new ArrayList<>();
    ArrayList<String> city = new ArrayList<>();//縣市
    ArrayList<String> dist = new ArrayList<>();//區
    ArrayList<Double> lat = new ArrayList<>();
    ArrayList<Double> lon = new ArrayList<>();
    //計算距離
    ArrayList<Double> t = new ArrayList<>();
    //空汙
    ArrayList<String> airid = new ArrayList<>();
    ArrayList<Double> airlat = new ArrayList<>();
    ArrayList<Double> airlon = new ArrayList<>();
    //計算距離空汙
    ArrayList<Double> tt = new ArrayList<>();
    //紫外線
    ArrayList<String> uviname = new ArrayList<>();
    ArrayList<Double> uvilat = new ArrayList<>();
    ArrayList<Double> uvilon = new ArrayList<>();
    //計算距離紫外線
    ArrayList<Double> ttt = new ArrayList<>();
    //現在位置
    Double[] mylat = new Double[1];
    Double[] mylon = new Double[1];
    //上方顯示 區(測站)
    String top = "";
    //傳給ManageLocation
    ArrayList<String> latstr = new ArrayList<>();
    ArrayList<String> lonstr = new ArrayList<>();
    //設定要傳的變數
    double pass_lat = 0.0;
    double pass_lon = 0.0;
    String pass_name = "";
    String pass_city = "";
    String pass_dist = "";
    String pass_air_id = "";
    String pass_uvi_name = "";
    double pass_now_lat = 0.0;
    double pass_now_lon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        //接收定位
        Bundle bundle = getIntent().getExtras();
        Double getmylat = bundle.getDouble("mylat",25.035768);
        Double getmylon = bundle.getDouble("mylon",121.433778);
        mylat[0] = getmylat;
        mylon[0] = getmylon;
        pass_now_lat = getmylat;
        pass_now_lon = getmylon;
        //抓資料
        String url_station_element = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0001-001?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742";
        String url_station_location = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/C-B0074-002?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&status=現存測站";
        String url_air = "https://opendata.epa.gov.tw/api/v1/AQI?skip=0&top=1000&format=json";
        String url_uvi = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0003-001?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&elementName=H_UVI";
        getData_station_element(url_station_element);
        getData_station_location(url_station_location);
        getData_air(url_air);
        getData_uvi(url_uvi);
    }

    //天氣資訊
    public void getData_station_element(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            //Velloy採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果=" + response.toString());
                                try {
                                    parseJSON_element(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Response.ErrorListener 監聽錯誤
                        Log.e("回傳結果", "錯誤訊息：" + error.toString());
                    }
                });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    public void parseJSON_element(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("location");
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.getJSONObject(i);
            station.add(o.getString("locationName"));
        }
        update();
    }
    //測站地址
    public void getData_station_location(String urlString){
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString,null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            //採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果=" + response.toString());
                                try {
                                    //整理資料
                                    parseJSON_location(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Response.ErrorListener 監聽錯誤
                        Log.e("回傳結果","錯誤訊息："+ error.toString());
                    }
                });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    public void parseJSON_location(JSONObject jsonObject) throws JSONException {
        JSONArray station_data = jsonObject.getJSONObject("records").getJSONObject("data").getJSONObject("stationStatus").getJSONArray("station");
        for (int i = 0; i < station_data.length(); i++) {
            JSONObject o = station_data.getJSONObject(i);
            stationname.add(o.getString("stationName"));
            address.add(o.getString("countyName") + o.getString("stationAddress"));
            latt.add(o.getDouble("latitude"));
            lonn.add(o.getDouble("longitude"));
        }
        update();
    }
    //空汙
    private void getData_air(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(urlString,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("回傳結果", "結果=" + response.toString());
                        try {
                            parseJSON_air(response.toString());
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error){
                Log.d("回傳結果", "結果=" + error.toString());
            }
        });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void parseJSON_air(String s) throws JSONException {
        JSONArray data = new JSONArray(s);
        for (int i = 0; i < data.length(); i++){
            JSONObject d = data.getJSONObject(i);
            if (!d.getString("Status").equals("設備維護")){
                airid.add(d.getString("SiteId"));
                airlat.add(d.getDouble("Latitude"));
                airlon.add(d.getDouble("Longitude"));
            }
        }
        update();
    }
    //紫外線
    private void getData_uvi(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            //採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果=" + response.toString());
                                try {
                                    parseJSON_uvi(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Response.ErrorListener 監聽錯誤
                        Log.e("回傳結果", "錯誤訊息：" + error.toString());
                    }
                });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void parseJSON_uvi(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("location");
        for (int i = 0; i < data.length(); i++) {
            JSONObject d = data.getJSONObject(i);
            JSONArray dd = d.getJSONArray("weatherElement");
            for(int k = 0; k < dd.length(); k++) {
                JSONObject ddd = dd.getJSONObject(k);
                if (ddd.getString("elementName").equals("H_UVI")) {
                    if (ddd.getDouble("elementValue") >= 0){
                        uviname.add(d.getString("locationName"));
                        uvilat.add(d.getDouble("lat"));
                        uvilon.add(d.getDouble("lon"));
                    }
                }
            }
        }
        update();
    }

    public void near() {
        for (int s = 0; s < station.size(); s++) {
            stationn.add(s,"");
            add.add(s, "");
            city.add(s, "");
            dist.add(s, "");
            lat.add(s, -99.0);
            lon.add(s, -99.0);
            for (int j = 0; j < stationname.size(); j++) {
                if (station.get(s) != null && stationname.get(j) != null) {
                    if (station.get(s).equals(stationname.get(j))) {
                        stationn.set(s,stationname.get(j));
                        add.set(s, address.get(j));
                        city.set(s, address.get(j).substring(0,3));
                        dist.set(s, address.get(j).substring(3,6));
                        lat.set(s, latt.get(j));
                        lon.set(s, lonn.get(j));
                    }
                }
            }
            if (mylat[0] != null && mylon[0] != null) {
                t.add(s,caldistance.caldistance(mylat[0], mylon[0], lat.get(s), lon.get(s)));
            }
        }
        Collections.sort(t);
        for (int k = 0; k < station.size(); k++) {
            if (t.get(0) == caldistance.caldistance(mylat[0], mylon[0], lat.get(k), lon.get(k))) {
                top = dist.get(k) + "(" + station.get(k) + ")";
                pass_name = station.get(k);
                pass_lat = lat.get(k);
                pass_lon = lon.get(k);
                pass_dist = dist.get(k);
                pass_city = city.get(k);
            }
        }
    }
    public void near_air(){
        if (mylat[0]!= null && mylon[0] != null) {
            for (int i = 0; i < airid.size(); i++){
                tt.add(i,caldistance.caldistance(mylat[0], mylon[0], airlat.get(i), airlon.get(i)));
            }
            Collections.sort(tt);
            for (int k = 0; k < airid.size(); k++) {
                if (tt.get(0) == caldistance.caldistance(mylat[0], mylon[0], airlat.get(k), airlon.get(k))) {
                    pass_air_id = airid.get(k);
                }
            }
        }
    }
    public void near_uvi(){
        if (mylat[0] != null && mylon[0] != null) {
            for (int i = 0; i < uviname.size(); i++){
                ttt.add(i,caldistance.caldistance(mylat[0], mylon[0], uvilat.get(i), uvilon.get(i)));
            }
            Collections.sort(ttt);
            for (int k = 0; k < uviname.size(); k++) {
                if (ttt.get(0) == caldistance.caldistance(mylat[0], mylon[0], uvilat.get(k), uvilon.get(k))) {
                    pass_uvi_name = uviname.get(k);
                }
            }
        }
    }

    public void update(){
        Intent intent = new Intent(DataActivity.this,ShowActivity.class);
        near();
        intent.putExtra("top",top);
        intent.putExtra("pass_name",pass_name);
        intent.putExtra("pass_city",pass_city);
        intent.putExtra("pass_dist",pass_dist);
        near_air();
        intent.putExtra("airid",pass_air_id);
        near_uvi();
        intent.putExtra("uviname",pass_uvi_name);
        //DataActivity -> ShowActivity -> ManageLocation (ArrayList)
        for(int z = 0; z < lat.size(); z++){
            latstr.add(z,lat.get(z).toString());
            lonstr.add(z,lon.get(z).toString());
        }

        intent.putExtra("stationn",stationn); //比對後所有測站名
        intent.putExtra("latstr",latstr);
        intent.putExtra("lonstr",lonstr);
        intent.putExtra("address",add);
        //map
        intent.putExtra("mylat",pass_now_lat);
        intent.putExtra("mylon",pass_now_lon);
        startActivity(intent);
    }
}