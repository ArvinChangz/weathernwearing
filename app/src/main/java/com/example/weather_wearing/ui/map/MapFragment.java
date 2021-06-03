package com.example.weather_wearing.ui.map;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather_wearing.R;
import com.example.weather_wearing.ShowActivity;
import com.example.weather_wearing.caldistance;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;


public class MapFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Context mContext;
    private String getStationName = "";
    private Double mylat = 0.0;
    private Double mylon = 0.0;
    private Double searchlat = 0.0;
    private Double searchlon = 0.0;
    private String getSearchName = "";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        getStationName = ((ShowActivity) activity).getStationName();
        mylat = ((ShowActivity) activity).getMylat();
        mylon =  ((ShowActivity) activity).getMylon();
        searchlat = ((ShowActivity) activity).getSearchlat();
        searchlon = ((ShowActivity) activity).getSearchlon();
        getSearchName = ((ShowActivity) activity).searchlocation();
    }

    // 定義這個權限要求的編號
    private final int REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION = 100;

    private int station_number = 450;  //預設測站數
    private int count = 0;  //測站實際數量
    private int location_number = 562; //測站地址數
    //天氣資料element
    private String [] station = new String[station_number];  //測站名
    private String [] temp = new String[station_number];
    private String [] dtx = new String[station_number];
    private String [] dtn = new String[station_number];
    //測站地址location
    private String [] stationname= new String[location_number];
    private String [] address = new String[location_number];
    private Double[] latt = new Double[location_number];
    private Double[] lonn = new Double[location_number];
    //比對後呈現
    private String [] add = new String[station_number];
    private Double[] lat = new Double[station_number];
    private Double[] lon = new Double[station_number];
    //計算距離
    private Double[] z = new Double[station_number];  //複製t備用(排序前)

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationManager mLocationMgr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 建立一個GoogleApiClient物件。
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;
                Location location = locationResult.getLastLocation();
                updateMapLocation(location);
            }
        };
        mLocationMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        String url_element = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0001-001?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742";
        String url_location = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/C-B0074-002?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&status=%E7%8F%BE%E5%AD%98%E6%B8%AC%E7%AB%99";
        getData_element(url_element);
        getData_location(url_location);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_weathermap, container, false);
        // 建立SupportMapFragment，並且設定Map的callback
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        supportMapFragment.getMapAsync(this);
        //  把SupportMapFragment放到介面佈局檔裡頭的FrameLayout顯示。
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_weathermap, supportMapFragment)
                .commit();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 啟動 Google API。
        mGoogleApiClient.connect();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        // 停止定位
        enableLocation(false);
    }
    @Override
    public void onStop() {
        super.onStop();
        // 停用 Google API
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // 設定Google Map的Info Window
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.map_info_window, null);
                TextView title = v.findViewById(R.id.stationtitle);
                title.setText(marker.getTitle());
                TextView txt = v.findViewById(R.id.station);
                txt.setText(marker.getSnippet());
                return v;
            }
            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Google API 連線成功時會執行這個方法
        //Toast.makeText(MapsActivity.this, "Google API 連線成功",Toast.LENGTH_LONG).show();
        // 啟動定位
        enableLocation(true);
    }
    @Override
    public void onConnectionSuspended(int i) {
        // Google API 無故斷線時，才會執行這個方法
        // 程式呼叫disconnect()時不會執行這個方法
        switch (i) {
            case CAUSE_NETWORK_LOST:
                //Toast.makeText(MapsActivity.this, "網路斷線，無法定位",Toast.LENGTH_LONG).show();
                break;
            case CAUSE_SERVICE_DISCONNECTED:
                //Toast.makeText(MapsActivity.this, "Google API 異常，無法定位",Toast.LENGTH_LONG).show();
                break;
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // 和 Google API 連線失敗時會執行這個方法
        //Toast.makeText(MapsActivity.this, "Google API 連線失敗",Toast.LENGTH_LONG).show();
    }

    private void enableLocation(boolean on) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // 這項功能尚未取得使用者的同意
            // 開始執行徵詢使用者的流程
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(getActivity());
                altDlgBuilder.setTitle("提示");
                altDlgBuilder.setMessage("需要位置權限");
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                // 提示使用者允許權限
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{
                                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                            }
                        });
                altDlgBuilder.show();
                return;
            } else {
                // 提示使用者允許權限
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                return;
            }
        }
        // 已允許權限
        if (on) {
            // 取得上一次定位資料
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mMap.setMyLocationEnabled(true);
                        //Toast.makeText(MapsActivity.this, "成功取得上一次定位",Toast.LENGTH_LONG).show();
                        updateMapLocation(location);
                    } else {
                        AlertDialog.Builder altDlgBuilder = new AlertDialog.Builder(getActivity());
                        altDlgBuilder.setTitle("提示");
                        altDlgBuilder.setMessage("請開啟定位功能，並重新啟動");
                        altDlgBuilder.setCancelable(false);
                        altDlgBuilder.setPositiveButton("確定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                        altDlgBuilder.show();
                    }
                }
            });
            // 準備一個LocationRequest物件，設定定位參數，在啟動定位時使用
            LocationRequest locationRequest = LocationRequest.create();
            // 設定二次定位之間的時間間隔，單位是千分之一秒。
            //locationRequest.setInterval(5000);
            // 二次定位之間的最大距離，單位是公尺。
            locationRequest.setSmallestDisplacement(5);
            // 啟動定位，如果GPS功能有開啟，優先使用GPS定位，否則使用網路定位。
            if (mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationRequest.setPriority(
                        LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else if (mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
            }
            // 啟動定位功能
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        } else {
            // 停止定位功能
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void getData_element(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            //Velloy採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果:天氣資料 = " + response.toString());
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
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }
    private void parseJSON_element(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("location");
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.getJSONObject(i);
            JSONArray children = o.getJSONArray("weatherElement");
            station[i] = o.getString("locationName");
            count++;
            for (int k = 0; k < children.length(); k++) {
                JSONObject o2 = children.getJSONObject(k);
                if (o2.getString("elementName").equals("TEMP")) {
                    double tempa = o2.getDouble("elementValue");
                    String tempstr;
                    if (tempa == -99) {
                        tempstr = "測站儀器故障!";
                    } else {
                        tempstr = Math.round(tempa) + "°C";
                    }
                    temp[i] = tempstr;
                } else if (o2.getString("elementName").equals("D_TX")) {
                    double dtx2 = o2.getDouble("elementValue");
                    String dtxstr;
                    if (dtx2 == -999) {
                        dtxstr = "測站儀器故障! / ";
                    } else {
                        dtxstr = Math.round(dtx2) + "°C / ";
                    }
                    dtx[i] = dtxstr;
                } else if (o2.getString("elementName").equals("D_TN")) {
                    double dtn2 = o2.getDouble("elementValue");
                    String dtnstr;
                    if (dtn2 == -999) {
                        dtnstr = "測站儀器故障!";
                    } else {
                        dtnstr = Math.round(dtn2) + "°C ";
                    }
                    dtn[i] = dtnstr;
                }
            }
        }
        if (count < station.length) {
            for (int z = count; z < station.length; z++) {
                station[z] = "無";
                temp[z] = "0";
                dtx[z] = "0";
                dtn[z] = "0";
            }
        }
    }

    private void getData_location(String urlString) {
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString, null,
                        new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            //採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果:測站地址 = " + response.toString());
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
                        Log.e("回傳結果", "錯誤訊息：" + error.toString());
                    }
                });
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }
    private void parseJSON_location(JSONObject jsonObject) throws JSONException {
        JSONArray station_data = jsonObject.getJSONObject("records").getJSONObject("data").getJSONObject("stationStatus").getJSONArray("station");
        for (int i = 0; i < station_data.length(); i++) {
            JSONObject o = station_data.getJSONObject(i);
            stationname[i] = o.getString("stationName");
            address[i] = o.getString("countyName") + o.getString("stationAddress");
            latt[i] = o.getDouble("latitude");
            lonn[i] = o.getDouble("longitude");
        }
    }

    private void updateMapLocation(Location location) {
        if (location != null){
            mylat = location.getLatitude();
            mylon = location.getLongitude();
        }
        for (int s = 0; s < station.length; s++) {
            for (int j = 0; j < stationname.length; j++) {
                if (station[s] != null && stationname[j] != null) {
                    if (station[s].equals(stationname[j])) {
                        add[s] = address[j];
                        lat[s] = latt[j];
                        lon[s] = lonn[j];
                    }
                }
            }
            if (lat[s] == null || lon[s] == null) {
                lat[s] = -99.0;
                lon[s] = -99.0;
            }
            if (mylat != null && searchlat == 0.0 && searchlon == 0.0) {
                z[s] = caldistance.caldistance(mylat, mylon, lat[s], lon[s]);
            } else {
                z[s] = caldistance.caldistance(searchlat, searchlon, lat[s], lon[s]);
            }
        }
        double lat_show;
        double lon_show;
        double distance_show;
        String stationName_show;
        String temp_show;
        String maxmintemp_show;
        String add_show;
        for (int k = 0; k < count; k++) {
            lat_show = lat[k];
            lon_show = lon[k];
            stationName_show = station[k];
            temp_show = "現在溫度：" + temp[k];
            maxmintemp_show = "最高溫/最低溫：" + dtx[k] + dtn[k];
            distance_show = z[k];
            add_show = add[k];
            LatLng mindis = new LatLng(lat_show, lon_show);
            if (stationName_show.equals(getStationName)) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mindis, 13));
                mMap.addMarker(new MarkerOptions().position(mindis).title(stationName_show + "(" + distance_show + "km)").snippet(temp_show + "\n" + maxmintemp_show + "\n測站地址：" + add_show).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag2)));
            } else {
                if (lat_show != -99.0 && lon_show != -99.0)
                    mMap.addMarker(new MarkerOptions().position(mindis).title(stationName_show + "(" + distance_show + "km)").snippet(temp_show + "\n" + maxmintemp_show + "\n測站地址：" + add_show).icon(BitmapDescriptorFactory.fromResource(R.drawable.locate1)));
            }
        }
        if(searchlat != 0.0 && searchlon != 0.0){
            LatLng search = new LatLng(searchlat, searchlon);
            mMap.addMarker(new MarkerOptions().position(search).title("搜尋地點："+getSearchName).icon(BitmapDescriptorFactory.fromResource(R.drawable.flagn)));
        }
    }
}