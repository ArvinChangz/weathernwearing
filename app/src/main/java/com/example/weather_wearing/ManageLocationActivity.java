package com.example.weather_wearing;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather_wearing.database.Database;
import com.example.weather_wearing.database.Databaseadd;
import com.example.weather_wearing.database.Databaselat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ManageLocationActivity extends AppCompatActivity {

    SearchView searchView;
    TableLayout table;
    ImageView imageButton;
    CheckBox checkBox;
    TextView location;
    ArrayList<String> add_station = new ArrayList<>(); //新增測站
    //預設用來接從DataActivity傳來的資料
    ArrayList<String> all_station = new ArrayList<>(); //所有測站名
    ArrayList<String> all_lat = new ArrayList<>(); //所有緯度
    ArrayList<String> all_lon = new ArrayList<>(); //所有經度
    ArrayList<String> all_address = new ArrayList<>(); //所有地址
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
    //搜尋地點的經緯度
    //資料庫
    private String TABLE_LAT = "MyTableLat";
    private final String DB_LATNAME = "lat2";
    private String TABLE_ADD = "MyTableAdd";
    private final String DB_ADDNAME = "add3";
    private final int DB_VERSION = 1;
    Databaselat mDBHelper2;
    Databaseadd mDBHelper3;
    ArrayList<HashMap<String, String>> arrayList2 = new ArrayList<>();//取得所有資料(測站資料)
    ArrayList<HashMap<String, String>> arrayList3 = new ArrayList<>();//取得所有資料(測站資料)

    int count = 0;
    double min;
    double a; //用來計算最短距離
    int index;
    String add;
    String city;
    String dist;
    String air ;
    String uvi;
    String loc ;
    Double lat = 0.0;
    Double lon = 0.0;
    Double searchlat ;
    Double searchlon ;
    String pass_air_id = "7";
    String pass_uvi_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managelocation);
        searchView = findViewById(R.id.sv_location);
        imageButton = findViewById(R.id.imageButton);
        checkBox = findViewById(R.id.checkbox);
        location = findViewById(R.id.text1);
        table = findViewById(R.id.table);
        //取得DataActivity傳過來的值。
        Intent i = getIntent();
        all_station = i.getStringArrayListExtra("station");
        all_lat = i.getStringArrayListExtra("latstr");
        all_lon = i.getStringArrayListExtra("lonstr");
        all_address = i.getStringArrayListExtra("address");

        mDBHelper3 = new Databaseadd(this, DB_ADDNAME, null, DB_VERSION, TABLE_ADD);
        mDBHelper3.chickTable();//確認是否存在資料表，沒有則新增
       // mDBHelper3.addData("測站","距離","地點","縣市","區","空汙","紫外線");

        //讀取陣列
        mDBHelper2 = new Databaselat(this, DB_LATNAME, null, DB_VERSION, TABLE_LAT);
        mDBHelper2.chickLatTable();//確認是否存在資料表，沒有則新增
        arrayList2 = mDBHelper2.showlatAll();
        if (arrayList2.size() == 0) {
            for (int q = 0; q < all_station.size(); q++) {
                mDBHelper2.addLatData(all_station.get(q), all_lat.get(q), all_lon.get(q), all_address.get(q));
            }
        }
        arrayList2 = mDBHelper2.showlatAll();

        String url_air = "https://opendata.epa.gov.tw/api/v1/AQI?skip=0&top=1000&format=json";
        String url_uvi = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0003-001?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&elementName=H_UVI";
        getData_air(url_air);
        getData_uvi(url_uvi);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageLocationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        addstation();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final String location = searchView.getQuery().toString(); //搜尋地點
                List<Address> addressList = null;
                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(ManageLocationActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList.size() == 0) {
                        Toast.makeText(ManageLocationActivity.this, "查無此地!請輸入更明確的地點", Toast.LENGTH_LONG).show();
                    } else {
                        Address address = addressList.get(0);
                        min = 300.0;
                        for (int i = 0; i < arrayList2.size(); i++) {
                            a = caldistance.caldistance(address.getLatitude(), address.getLongitude(), Double.valueOf(mDBHelper2.searchlatById(arrayList2.get(i).get("id")).get(0).get("lat")), Double.valueOf(mDBHelper2.searchlatById(arrayList2.get(i).get("id")).get(0).get("lon")));
                            if (a < min) {
                                min = a;
                                index = i;
                                lat = Double.valueOf(mDBHelper2.searchlatById(arrayList2.get(i).get("id")).get(0).get("lat"));
                                lon = Double.valueOf(mDBHelper2.searchlatById(arrayList2.get(i).get("id")).get(0).get("lon"));
                            }
                            add_station.add(0, mDBHelper2.searchlatById(arrayList2.get(index).get("id")).get(0).get("station"));
                            add_station.add(1, " (" + min + "km)");
                            add_station.add(2, location);
                        }
                        city = mDBHelper2.searchlatById(arrayList2.get(index).get("id")).get(0).get("address").substring(0, 3);
                        dist = mDBHelper2.searchlatById(arrayList2.get(index).get("id")).get(0).get("address").substring(3, 6);
                        air = near_air(lat,lon);
                        uvi = near_uvi(lat,lon);
                        add = add_station.get(0);
                        mDBHelper3.addData(add_station.get(0), add_station.get(1), add_station.get(2),address.getLatitude(),address.getLongitude(), city, dist,air,uvi);
                        search();
                        add_station.clear();
                        }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    // 顯示已新增地點
    public void addstation() {
            mDBHelper3 = new Databaseadd(this, DB_ADDNAME, null, DB_VERSION, TABLE_ADD);
            arrayList3 = mDBHelper3.showAll();
            if (arrayList3.size() != 0) {
                for (int e = 0; e < arrayList3.size(); e++) {
                    final TableRow row = new TableRow(getApplicationContext());
                    final TextView text = new TextView(getApplication());
                    final TextView text1 = new TextView(getApplication());
                    final Button button = new Button(getApplication());
                    row.setId(e);
                    row.setGravity(Gravity.CENTER);
                    row.setPadding(20, 15, 0, 15);
                    text.setPadding(30, 15, 30, 15);
                    text.setText("\n" + mDBHelper3.searchById(arrayList3.get(e).get("id")).get(0).get("station") + " 測站" + mDBHelper3.searchById(arrayList3.get(e).get("id")).get(0).get("distance") + "\n" + "搜尋地點：" + mDBHelper3.searchById(arrayList3.get(e).get("id")).get(0).get("location") + "\n" );
                    text.setBackgroundColor(Color.rgb(230, 230, 250));
                    text.setTextColor(Color.rgb(105, 105, 105));
                    text.setGravity(Gravity.CENTER);
                    text.setTextSize(20);
                    button.setText("刪除");
                  //  button.setBackgroundColor(Color.rgb(230, 230, 250));
                    row.addView(text);
                    row.addView(text1);
                    text1.setWidth(30);
                    table.setPadding(20, 15, -250, 15);
                    table.addView(row);
                    row.addView(button);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            add = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("station");
                            mDBHelper3.deleteByStation(add);
                            TableRow row = (TableRow) view.getParent();
                            table.removeView(row);
                        }
                    });
                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            city = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("city");
                            dist = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("dist");
                            add = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("station");
                            air = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("air");
                            uvi = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("uvi");
                            loc = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("location");
                            searchlat = Double.parseDouble(mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("lat"));
                            searchlon = Double.parseDouble(mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("lon"));
                            Intent intent = new Intent(ManageLocationActivity.this, ShowActivity.class);
                            intent.putExtra("top", dist + "(" + add + ")");
                            intent.putExtra("pass_name", add);
                            intent.putExtra("pass_city", city);
                            intent.putExtra("pass_dist", dist);
                            intent.putExtra("airid",  air);
                            intent.putExtra("uviname",uvi);
                            intent.putExtra("searchlat",searchlat);
                            intent.putExtra("searchlon",searchlon);
                            intent.putExtra("loc",loc);
                            //指定地點Toolbar顏色
                            intent.putExtra("R",243);
                            intent.putExtra("G",231);
                            intent.putExtra("B",233);
                            //指定地點字體顏色
                            intent.putExtra("TR",105);
                            intent.putExtra("TG",105);
                            intent.putExtra("TB",105);
                            startActivity(intent);
                            ManageLocationActivity.this.finish();
                        }
                    });
                }
                    imageButton.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            count = count + 1;
                            if (count % 2 != 0) {
                                table.setPadding(30, 15, 30, 15);
                            } else {
                                table.setPadding(20, 15, -250, 15);
                            }
                        }
                    });
            }else{
                imageButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        count = count + 1;
                        if (count % 2 != 0) {
                            table.setPadding(30, 15, 30, 15);
                        } else {
                            table.setPadding(20, 15, -250, 15);
                        }
                    }
                });
            }
    }

    // 搜尋後新增地點
    public void search() {
        mDBHelper3 = new Databaseadd(this, DB_ADDNAME, null, DB_VERSION, TABLE_ADD);
        arrayList3 = mDBHelper3.showAll();
        Log.d(TAG,"test："+arrayList3);
            for (int z = arrayList3.size()-1; z < arrayList3.size(); z++) {
                final TableRow row = new TableRow(getApplicationContext());
                final TextView text = new TextView(getApplication());
                final TextView text1 = new TextView(getApplication());
                final Button button = new Button(getApplication());
                row.setId(z);
                row.setGravity(Gravity.CENTER);
                row.setPadding(20, 15, 20, 15);
                text.setPadding(30, 15, 30, 15);
                text.setText("\n" + mDBHelper3.searchById(arrayList3.get(z).get("id")).get(0).get("station") + " 測站" + mDBHelper3.searchById(arrayList3.get(z).get("id")).get(0).get("distance") + "\n" + "搜尋地點：" + mDBHelper3.searchById(arrayList3.get(z).get("id")).get(0).get("location") + "\n" );
                text.setBackgroundColor(Color.rgb(230, 230, 250));
                text.setTextColor(Color.rgb(105, 105, 105));
                text.setGravity(Gravity.CENTER);
                text.setTextSize(20);
                button.setText("刪除");
                row.addView(text);
                row.addView(text1);
                text1.setWidth(30);
                table.setPadding(20, 15, -250, 15);
                table.addView(row);
                row.addView(button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        add = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("station");
                        mDBHelper3.deleteByStation(add);
                        TableRow row = (TableRow) view.getParent();
                        table.removeView(row);
                    }
                });
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        city = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("city");
                        dist = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("dist");
                        add = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("station");
                        air = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("air");
                        uvi = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("uvi");
                        loc = mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("location");
                        searchlat = Double.parseDouble(mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("lat"));
                        searchlon = Double.parseDouble(mDBHelper3.searchById(arrayList3.get(row.getId()).get("id")).get(0).get("lon"));
                        Intent intent = new Intent(ManageLocationActivity.this, ShowActivity.class);
                        intent.putExtra("top", dist + "(" + add + ")");
                        intent.putExtra("pass_name", add);
                        intent.putExtra("pass_city", city);
                        intent.putExtra("pass_dist", dist);
                        intent.putExtra("airid",  air);
                        intent.putExtra("uviname",uvi);
                        intent.putExtra("searchlat",searchlat);
                        intent.putExtra("searchlon",searchlon);
                        intent.putExtra("loc",loc);
                        //指定地點Toolbar顏色
                        intent.putExtra("R",243);
                        intent.putExtra("G",231);
                        intent.putExtra("B",233);
                        //指定地點字體顏色
                        intent.putExtra("TR",105);
                        intent.putExtra("TG",105);
                        intent.putExtra("TB",105);
                        startActivity(intent);
                        ManageLocationActivity.this.finish();
                    }
                });
            }
        }
    //空汙
    private void getData_air(String urlString) {
        //        //使用JsonObjectRequest類別要求JSON資料。
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(urlString,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("回傳結果", "結果=" + response.toString());
                        // tv.setText(response.toString());
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
                        uvilat.add(Double.valueOf(d.getString("lat")));
                        uvilon.add(Double.valueOf(d.getString("lon")));
                    }
                }
            }
        }
    }
    public String near_air(double a,double b){
        if (a != 0.0 && b != 0.0) {
            for (int i = 0; i < airid.size(); i++){
                tt.add(i,caldistance.caldistance(a,b, airlat.get(i), airlon.get(i)));
            }
            Collections.sort(tt);
            for (int k = 0; k < airid.size(); k++) {
                if (tt.get(0) == caldistance.caldistance(a, b, airlat.get(k), airlon.get(k))) {
                    pass_air_id = airid.get(k);
                }
            }
        }
        return  pass_air_id;
    }
    public String near_uvi(double c,double d){
        if (c != 0.0 && d != 0.0) {
            for (int i = 0; i < uviname.size(); i++){
                ttt.add(i,caldistance.caldistance(c,d, uvilat.get(i), uvilon.get(i)));
            }
            Collections.sort(ttt);
            for (int k = 0; k < uviname.size(); k++) {
                if (ttt.get(0) == caldistance.caldistance(c, d, uvilat.get(k), uvilon.get(k))) {
                    pass_uvi_name = uviname.get(k);
                }
            }
        }
        return  pass_uvi_name;
    }
}