package com.example.weather_wearing.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather_wearing.NotificationHelper;
import com.example.weather_wearing.R;
import com.example.weather_wearing.ShowActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private String getStationName = "";
    private String getAirId = "";
    private String getUviName = "";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getStationName = ((ShowActivity) activity).getStationName();
        getAirId = ((ShowActivity) activity).getAirId();
        getUviName = ((ShowActivity) activity).getUviName();
    }

    private TextView temp;
    private TextView dtx;
    private TextView dtn;
    private TextView feeltemp;
    private TextView air;
    private TextView sunray;
    private ImageView icon;
    private TableLayout table;
    private double[] a = new double[4]; //存用來算體感溫度的東西，最後一個用來判斷舒適度
    private double rain = -99.0;
    private boolean isFirstLoading = true;

    private ArrayList<View> views = new ArrayList<>();
    private ViewPager viewPager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ImageView btn;
        temp = root.findViewById(R.id.Temp);
        dtx = root.findViewById(R.id.dtx);
        dtn = root.findViewById(R.id.dtn);
        feeltemp = root.findViewById(R.id.feel_temp);
        air = root.findViewById(R.id.air);
        sunray = root.findViewById(R.id.uvi);
        icon = root.findViewById(R.id.pic);
        btn = root.findViewById(R.id.btn);
        table = root.findViewById(R.id.table);

        viewPager = root.findViewById(R.id.viewpager);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog builder = new AlertDialog.Builder(getActivity())
                        .setMessage("空汙及紫外線指數分級標準")
                        .setView(R.layout.air_uvi)
                        .show();
                try {
                    //獲取mAlert物件
                    Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                    mAlert.setAccessible(true);
                    Object mAlertController = mAlert.get(builder);
                    //獲取mMessageView並設定大小顏色
                    Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                    mMessage.setAccessible(true);
                    TextView mMessageView = (TextView) mMessage.get(mAlertController);
                    mMessageView.setGravity(Gravity.CENTER_HORIZONTAL);
                    mMessageView.setTextColor(Color.BLACK);
                    mMessageView.setTextSize(20);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                builder.getWindow().setLayout(1000, 1800);
            }
        });

        //無人自動站氣象資料
        String url_no = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0001-001?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&elementName=TEMP,D_TX,D_TN,WDSD,HUMD,PRES&parameterName=TOWN&locationName=" + getStationName;
        getData_no(url_no);
        //有人的氣象站資料-紫外線
        String url_uvi = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0003-001?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&elementName=H_UVI&locationName=" + getUviName;
        getData_uvi(url_uvi);
        //空汙
        String url_air = "https://opendata.epa.gov.tw/api/v1/AQI?skip=0&top=1000&format=json";
        getData_air(url_air);
        //雨量
        String rain = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0002-001?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&locationName=" + getStationName + "&elementName=MIN_10";
        getData_rain(rain);
        return root;
    }

    private void getData_no(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            //採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果=" + response.toString());
                                try {
                                    parseJSON_no(response);
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

    private void parseJSON_no(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("location");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Perference", MODE_PRIVATE); //儲存偏好
        int savedRadioIndex = sharedPreferences.getInt("radio", 0);
        for (int i = 0; i < data.length(); i++) {
            JSONObject d = data.getJSONObject(i);
            JSONArray element = d.getJSONArray("weatherElement");
            for (int k = 0; k < element.length(); k++) {
                JSONObject e = element.getJSONObject(k);
                if (e.getString("elementName").equals("TEMP")) {
                    double temp_a = e.getDouble("elementValue");
                    if (temp_a < -50.0) {
                        temp.setText(". . .");
                        a[0] = 0.0;
                        NotificationHelper.temp_Notification = "故障!";
                    } else {
                        String temp_str = Math.round(temp_a) + "°C";
                        temp.setText(temp_str);
                        a[0] = e.getDouble("elementValue");
                        NotificationHelper.temp_Notification = temp_str;
                    }
                } else if (e.getString("elementName").equals("D_TX")) {
                    double dtx_a = e.getDouble("elementValue");
                    if (dtx_a < -50.0) {
                        dtx.setText("...");
                        NotificationHelper.temp_high_Notification = "故障!";
                    } else {
                        String dtx_str = Math.round(dtx_a) + "°C ";
                        dtx.setText(dtx_str);
                        NotificationHelper.temp_high_Notification = dtx_str;
                    }
                } else if (e.getString("elementName").equals("D_TN")) {
                    double dtn_a = e.getDouble("elementValue");
                    if (dtn_a < -50.0) {
                        dtn.setText(" ...");
                        NotificationHelper.temp_low_Notification = "故障!";
                    } else {
                        String dtn_str = " " + Math.round(dtn_a) + "°C";
                        dtn.setText(dtn_str);
                        NotificationHelper.temp_low_Notification = dtn_str;
                    }
                } else if (e.getString("elementName").equals("WDSD")) {//風速 每秒/公尺
                    a[1] = e.getDouble("elementValue");
                } else if (e.getString("elementName").equals("HUMD")) {//相對濕度
                    a[2] = e.getDouble("elementValue");
                }
                double feel = (1.07 * a[0]) + (0.2 * (a[2] * 6.105 * Math.exp((17.27 * a[0]) / (237.7 + a[0])))) - (0.65 * a[1]) - 2.7;
                if (feel < 0.0) {
                    String feelt = "體感溫度" + "\n" + "測站故障";
                    feeltemp.setText(feelt);
                    NotificationHelper.feeling_Notification = "測站故障";
                } else {
                    String feelt = "體感溫度" + "\n" + "    " + Math.round(feel) + "°C";
                    feeltemp.setText(feelt);
                    NotificationHelper.feeling_Notification = Math.round(feel) + "°C";
                }
                double td = (Math.pow(a[2], 1 / 8) * (112 + (0.9 * a[0]))) + (0.1 * a[0]) - 112;
                double thi = a[0] - 0.55 * (1 - (Math.exp((17.269 * td) / (td + 237.3)) / Math.exp((17.269 * a[0]) / (a[0] + 237.3)))) * (a[0] - 14);
                if (thi > 0.0) {
                    a[3] = Math.round(thi);
                } else {
                    a[3] = 23.0;
                }
                if (savedRadioIndex == 1) {
                    a[3] += 5;
                } else if (savedRadioIndex == 2) {
                    a[3] -= 5;
                }
                //推薦圖片
                View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.a, null);
                View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.b, null);
                View view3 = LayoutInflater.from(getActivity()).inflate(R.layout.c, null);
                View view4 = LayoutInflater.from(getActivity()).inflate(R.layout.d, null);
                View view5 = LayoutInflater.from(getActivity()).inflate(R.layout.e, null);
                View view6 = LayoutInflater.from(getActivity()).inflate(R.layout.f, null);
                View view7 = LayoutInflater.from(getActivity()).inflate(R.layout.g, null);
                View view8 = LayoutInflater.from(getActivity()).inflate(R.layout.h, null);
                View view9 = LayoutInflater.from(getActivity()).inflate(R.layout.i, null);
                View view10 = LayoutInflater.from(getActivity()).inflate(R.layout.j, null);
                View view11 = LayoutInflater.from(getActivity()).inflate(R.layout.k, null);
                View view12 = LayoutInflater.from(getActivity()).inflate(R.layout.l, null);
                View view13 = LayoutInflater.from(getActivity()).inflate(R.layout.m, null);
                View view14 = LayoutInflater.from(getActivity()).inflate(R.layout.n, null);
                View view15 = LayoutInflater.from(getActivity()).inflate(R.layout.o, null);
                View view16 = LayoutInflater.from(getActivity()).inflate(R.layout.p, null);
                View view17 = LayoutInflater.from(getActivity()).inflate(R.layout.q, null);
                View view18 = LayoutInflater.from(getActivity()).inflate(R.layout.r, null);
                if (a[3] <= 10) { //非常寒冷
                    views.clear();
                    views.add(view1);
                    views.add(view2);
                    views.add(view3);
                } else if (a[3] > 10 && a[3] <= 15) {//寒冷
                    views.clear();
                    views.add(view4);
                    views.add(view5);
                    views.add(view6);
                } else if (a[3] > 15 && a[3] <= 20) {//稍寒
                    views.clear();
                    views.add(view7);
                    views.add(view8);
                    views.add(view9);
                } else if (a[3] > 20 && a[3] <= 25) {//舒適
                    views.clear();
                    views.add(view10);
                    views.add(view11);
                    views.add(view12);
                } else if (a[3] > 25 && a[3] <= 30) {//悶熱
                    views.clear();
                    views.add(view13);
                    views.add(view14);
                    views.add(view15);
                } else if (a[3] >= 30) {//易中暑
                    views.clear();
                    views.add(view16);
                    views.add(view17);
                    views.add(view18);
                }
            }
        }
        //為ViewPager設定介面卡
        viewPager.setAdapter(new MyAdapter(views));
        viewPager.setCurrentItem(1);
    }

    class MyAdapter extends PagerAdapter {
        private ArrayList<View> views;

        private MyAdapter(ArrayList<View> views) {
            this.views = views;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (views.get(position) != null) {
                container.removeView(views.get(position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object == view;
        }
    }

    private void getData_rain(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            //採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果=" + response.toString());
                                try {
                                    parseJSON_rain(response);
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
    private void parseJSON_rain(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("location");
        for (int i = 0; i < data.length(); i++) {
            JSONObject d = data.getJSONObject(i);
            JSONArray dd = d.getJSONArray("weatherElement");
            for (int k = 0; k < dd.length(); k++) {
                JSONObject ddd = dd.getJSONObject(k);
                if (ddd.getString("elementName").equals("MIN_10")) {
                    rain = ddd.getDouble("elementValue");
                }
            }
        }
        if (rain > 0.0) {
            icon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));//雨
            NotificationHelper.bitmap_Notification = BitmapFactory.decodeResource(getResources(), R.drawable.rainy);
            NotificationHelper.rainNow_Notification = true;
            TableRow row = new TableRow(getActivity());
            TextView n = new TextView(getActivity());
            n.setTextSize(18);
            n.setText("出門記得攜帶雨具");
            n.setPadding(0, 0, 0, 3);
            row.setGravity(Gravity.CENTER);
            row.addView(n);
            table.addView(row);
            NotificationHelper.rain_Notification = "出門記得攜帶雨具";
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        if (!isFirstLoading) {

            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
        }
        isFirstLoading = false;
    }

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
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }

    private void parseJSON_uvi(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("location");
        for (int i = 0; i < data.length(); i++) {
            JSONObject d = data.getJSONObject(i);
            JSONArray dd = d.getJSONArray("weatherElement");
            for (int k = 0; k < dd.length(); k++) {
                JSONObject ddd = dd.getJSONObject(k);
                if (ddd.getString("elementName").equals("H_UVI")) {
                    double huvi_a = ddd.getDouble("elementValue");
                    String huvi;
                    if (huvi_a <= 2) {
                        huvi = "紫外線" + "\n" + "  低量";//多雲
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.cloud));
                        NotificationHelper.bitmap_Notification = BitmapFactory.decodeResource(getResources(), R.drawable.cloud);
                    } else if (huvi_a <= 5) {
                        huvi = "紫外線" + "\n" + "  中量";//多雲到晴
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.clouds));
                        NotificationHelper.bitmap_Notification = BitmapFactory.decodeResource(getResources(), R.drawable.clouds);
                    } else if (huvi_a <= 7) {
                        huvi = "紫外線" + "\n" + "  高量";//晴
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.suncloud));
                        NotificationHelper.bitmap_Notification = BitmapFactory.decodeResource(getResources(), R.drawable.suncloud);
                    } else if (huvi_a <= 10) {
                        huvi = "紫外線" + "\n" + "  過量";//太陽
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                        NotificationHelper.bitmap_Notification = BitmapFactory.decodeResource(getResources(), R.drawable.sunny);
                        TableRow row = new TableRow(getActivity());
                        TextView n = new TextView(getActivity());
                        n.setTextSize(18);
                        n.setText("外出做好防曬工作");
                        n.setPadding(0, 0, 0, 3);
                        row.setGravity(Gravity.CENTER);
                        row.addView(n);
                        table.addView(row);
                        NotificationHelper.sun_Notification = "紫外線過量，外出請做好防曬工作";
                    } else {
                        huvi = "紫外線" + "\n" + "  危險";//太陽
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                        TableRow row = new TableRow(getActivity());
                        TextView n = new TextView(getActivity());
                        n.setTextSize(18);
                        n.setText("記得防曬，10點到14點避免外出");
                        n.setPadding(0, 0, 0, 3);
                        row.setGravity(Gravity.CENTER);
                        row.addView(n);
                        table.addView(row);
                        NotificationHelper.sun_Notification = "紫外線危險，請記得防曬，10點至14點避免外出";
                    }
                    sunray.setText(huvi);
                    if (rain > 0.0) {
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    }
                }
            }
        }
    }

    private void getData_air(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(urlString,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("回傳結果", "結果=" + response.toString());
                        // tv.setText(response.toString());
                        try {
                            parseJSON_air(response.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("回傳結果", "結果=" + error.toString());
            }
        });
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }

    private void parseJSON_air(String s) throws JSONException {
        JSONArray data = new JSONArray(s);
        for (int i = 0; i < data.length(); i++) {
            JSONObject d = data.getJSONObject(i);
            if (d.getString("SiteId").equals(getAirId)) {
                int uvi = d.getInt("AQI");
                if (uvi <= 50) {
                    air.setText("空汙指數" + "\n" + "    良好");
                } else if (uvi <= 100) {
                    air.setText("空汙指數" + "\n" + "    普通");
                } else if (uvi <= 150) {
                    air.setText("空汙指數" + "\n" + "    注意");
                    TableRow row = new TableRow(getActivity());
                    TextView n = new TextView(getActivity());
                    n.setTextSize(18);
                    n.setText("空氣品質較差，敏感族群建議戴口罩");
                    n.setPadding(0, 0, 0, 3);
                    row.setGravity(Gravity.CENTER);
                    row.addView(n);
                    table.addView(row);
                    NotificationHelper.mask_Notification = "空氣品質較差，敏感族群建議戴口罩";
                } else if (uvi <= 200) {
                    air.setText("空汙指數" + "\n" + "    不良");
                    TableRow row = new TableRow(getActivity());
                    TextView n = new TextView(getActivity());
                    n.setTextSize(18);
                    n.setText("空氣品質不良，建議外出者戴口罩");
                    n.setPadding(0, 0, 0, 3);
                    row.setGravity(Gravity.CENTER);
                    row.addView(n);
                    table.addView(row);
                    NotificationHelper.mask_Notification = "空氣品質不良，建議外出者戴口罩";
                } else if (uvi <= 300) {
                    air.setText("空汙指數" + "\n" + "    危險");
                    TableRow row = new TableRow(getActivity());
                    TextView n = new TextView(getActivity());
                    n.setTextSize(18);
                    n.setText("空汙嚴重，建議戴口罩或減少外出");
                    n.setPadding(0, 0, 0, 3);
                    row.setGravity(Gravity.CENTER);
                    row.addView(n);
                    table.addView(row);
                    NotificationHelper.mask_Notification = "空汙嚴重，建議戴口罩或減少外出";
                } else {
                    air.setText("空汙指數" + "\n" + "    有害");
                    TableRow row = new TableRow(getActivity());
                    TextView n = new TextView(getActivity());
                    n.setTextSize(18);
                    n.setText("空汙非常嚴重，非必要請避免外出");
                    n.setPadding(0, 0, 0, 3);
                    row.setGravity(Gravity.CENTER);
                    row.addView(n);
                    table.addView(row);
                    NotificationHelper.mask_Notification = "空汙非常嚴重，非必要請避免外出";
                }
            }
        }
    }
}