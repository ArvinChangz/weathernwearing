package com.example.weather_wearing.ui.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather_wearing.R;
import com.example.weather_wearing.ShowActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class DetailFragment extends Fragment {
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private TextView d1;
    private TextView d2;
    private TextView d3;
    private TextView d4;
    private String getCityName = "";
    private String getDistName = "";
    public static ImageView clothbtn;
    public static ImageView clothbtn2;
    private ArrayList<View> views = new ArrayList<>();
    private Double test;
    private Double test2;
    private String test1 = "";
    private String test3 = "";

    private boolean isFirstLoading = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getCityName = ((ShowActivity) activity).getCityName();
        getDistName = ((ShowActivity) activity).getDistName();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_weatherdetail, container, false);
        tv1 = root.findViewById(R.id.textView1);
        tv2 = root.findViewById(R.id.textView2);
        tv3 = root.findViewById(R.id.textView3);
        tv4 = root.findViewById(R.id.textView4);
        d1 = root.findViewById(R.id.date1);
        d2 = root.findViewById(R.id.date2);
        d3 = root.findViewById(R.id.date3);
        d4 = root.findViewById(R.id.date4);
        tv1.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv1.setHorizontallyScrolling(true); // 不讓超出螢幕的文字自動換行，使用滾動條
        tv2.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv2.setHorizontallyScrolling(true); // 不讓超出螢幕的文字自動換行，使用滾動條
        tv3.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv3.setHorizontallyScrolling(true); // 不讓超出螢幕的文字自動換行，使用滾動條
        tv4.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv4.setHorizontallyScrolling(true); // 不讓超出螢幕的文字自動換行，使用滾動條
        clothbtn = root.findViewById(R.id.clothbtn);
        clothbtn2 = root.findViewById(R.id.clothbtn2);

        int index = 0;
        String[] ids = {"001","005","009","013","017","021","025","029","033","037","041","045","049","053","057","061","065","069","073","077","081","085"};
        String[] idsp = {"宜蘭縣","桃園市","新竹縣","苗栗縣","彰化縣","南投縣","雲林縣","嘉義縣","屏東縣","臺東縣","花蓮縣","澎湖縣","基隆縣","新竹市","嘉義市","臺北市","高雄市","新北市","臺中市","臺南市","連江縣","金門縣"};
        for(int t = 0; t < idsp.length; t++){
            if(idsp[t].equals(getCityName)){
                index = t;
            }
        }
        String url3 = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-"+ids[index]+"?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&elementName=T,PoP6h,Wx,CI";
        getData(url3); //取得網址字串
        return root;
    }

    private void getData(String urlString) {
        //使用JsonObjectRequest類別要求JSON資料。
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(urlString, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            //採非同步作業，Response.Listener  監聽回應
                            public void onResponse(JSONObject response) {
                                Log.d("回傳結果", "結果=" + response.toString());
                                try {
                                    parseJSON(response);
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

    private void parseJSON(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("locations");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Perference", MODE_PRIVATE);    //儲存偏好
        int savedRadioIndex = sharedPreferences.getInt("radio", 0);

        ArrayList<String> datetime = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();

        ArrayList<String> starttime = new ArrayList<>();
        ArrayList<String> datatime = new ArrayList<>();
        ArrayList<String> rainy = new ArrayList<>();
        ArrayList<String> icon = new ArrayList<>();
        ArrayList<String> ci = new ArrayList<>();
        String imgStr = "";
        String imgStr1 = "";
        String imgStr2 = "";
        String imgStr3 = "";
        String imgStr4 = "";

        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                return null;
            }
        };
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.getJSONObject(i);
            JSONArray data1 = o.getJSONArray("location");
            for (int j = 0; j < data1.length(); j++) {
                JSONObject o2 = data1.getJSONObject(j);
                if (o2.getString("locationName").equals(getDistName)) {
                    JSONArray data2 = o2.getJSONArray("weatherElement");
                    for (int k = 0; k < data2.length(); k++) {
                        JSONObject o3 = data2.getJSONObject(k);
                        if (o3.getString("elementName").equals("T")) {
                            JSONArray data3 = o3.getJSONArray("time");
                            for (int a = 0; a < data3.length(); a++) {
                                JSONObject o4 = data3.getJSONObject(a);
                                datetime.add(o4.getString("dataTime"));
                                JSONArray data4 = o4.getJSONArray("elementValue");
                                JSONObject o5 = data4.getJSONObject(0);
                                temp.add(o5.getString("value"));
                            }
                        } else if (o3.getString("elementName").equals("PoP6h")) {
                            JSONArray data5 = o3.getJSONArray("time");
                            for (int c = 0; c < data5.length(); c++) {
                                JSONObject o5 = data5.getJSONObject(c);
                                starttime.add(o5.getString("startTime"));
                                JSONArray data6 = o5.getJSONArray("elementValue");
                                for (int d = 0; d < data6.length(); d++) {
                                    JSONObject o6 = data6.getJSONObject(d);
                                    rainy.add(o6.getString("value"));
                                }
                            }
                        }else if (o3.getString("elementName").equals("CI")) {
                            JSONArray data9 = o3.getJSONArray("time");
                            for (int h = 0; h < data9.length(); h++) {
                                JSONObject o9 = data9.getJSONObject(h);
                                datatime.add(o9.getString("dataTime"));
                                JSONArray data10 = o9.getJSONArray("elementValue");
                                for (int m = 0; m < data10.length(); m++) {
                                    JSONObject o10 = data10.getJSONObject(m);
                                    if((m&1)!=1) {
                                        ci.add(o10.getString("value"));
                                    }
                                }
                            }
                        } else if (o3.getString("elementName").equals("Wx")) {
                            JSONArray data7 = o3.getJSONArray("time");
                            for (int e = 0; e < data7.length(); e++) {
                                JSONObject o7 = data7.getJSONObject(e);
                                JSONArray data8 = o7.getJSONArray("elementValue");
                                for (int f = 0; f < data8.length(); f++) {
                                    JSONObject o8 = data8.getJSONObject(f);
                                    icon.add(o8.getString("value"));
                                    imgStr = "<img src='" + R.drawable.rainy + "'>";
                                    imgStr1 = "<img src='" + R.drawable.cloud + "'>";
                                    imgStr2 = "<img src='" + R.drawable.clouds + "'>";
                                    imgStr3 = "<img src='" + R.drawable.sunny + "'>";
                                    imgStr4 = "<img src='" + R.drawable.littlethu + "'>";
                                    imageGetter = new Html.ImageGetter() {
                                        @Override
                                        public Drawable getDrawable(String source) {
                                            int id = Integer.parseInt(source);
                                            Drawable draw = getResources().getDrawable(id);
                                            draw.setBounds(0, 0, 65, 65);
                                            return draw;
                                        }
                                    };
                                }
                            }
                        }
                    }
                }
            }
        }
        //未來服裝推薦
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, +1);
        cal2.add(Calendar.DAY_OF_MONTH, +2);
        String tomorrow = format.format(cal.getTime());//明
        String tomorrow2 = format.format(cal2.getTime());//後
        int tdd1 = 0;
        int tdd2 = 0;
        for(int dd = 0 ; dd < datatime.size() ; dd++){
            if(datatime.get(dd).equals(tomorrow+" 09:00:00")){
                tdd1 = dd;
            }else if(datatime.get(dd).equals(tomorrow2+" 09:00:00")){
                tdd2 = dd;
            }
        }
        String day1ci = ci.get(tdd1);//明天舒適度
        Double day1cin = Double.valueOf(day1ci);
        String day2ci = ci.get(tdd2);//後天舒適度
        Double day2cin = Double.valueOf(day2ci);
        //推荐1
        String mmm = (String) datetime.get(tdd1).subSequence(5, 7);
        if (mmm.subSequence(0,1).equals("0")){
            mmm = " " + mmm.subSequence(1,2) + "月";
        }else {
            mmm = mmm + "月";
        }
        String nnn = (String) datetime.get(tdd1).subSequence(8, 10);
        if (nnn.subSequence(0,1).equals("0")){
            nnn = " " + nnn + "日";
        }else{
            nnn = nnn + "日";
        }
        String day1 = mmm + nnn;
        //推薦2
        String mm = (String) datetime.get(tdd2).subSequence(5, 7);
        if (mm.subSequence(0,1).equals("0")){
            mm = " " + mm.subSequence(1,2) + "月";
        }else {
            mm = mm + "月";
        }
        String nn = (String) datetime.get(tdd2).subSequence(8, 10);
        if (nn.subSequence(0,1).equals("0")){
            nn = " " + nn + "日";
        }else{
            nn = nn + "日";
        }
        String day2 = mm + nn;
        test = day1cin;
        test2 = day2cin;
        test1 = day1;//明天日期
        test3 = day2;//後天日期
        //判斷選怕熱or怕冷
        if (savedRadioIndex == 1) {
            test += 5;
            test2 += 5;
        } else if (savedRadioIndex == 2) {
            test -= 5;
            test2 -= 5;
        }

        clothbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ViewPager viewPager = new ViewPager(getActivity());
                if (test <= 10) { //非常寒冷
                    views.clear();
                    int images[] = {R.drawable.a, R.drawable.b, R.drawable.c};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test > 10 && test <= 15) {//寒冷
                    views.clear();
                    int images[] = {R.drawable.d, R.drawable.e, R.drawable.f};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test > 15 && test <= 20) {//稍寒
                    views.clear();
                    int images[] = {R.drawable.g, R.drawable.h, R.drawable.i};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test > 20 && test <= 25) {//舒適
                    views.clear();
                    int images[] = {R.drawable.j, R.drawable.k, R.drawable.l};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test > 25 && test <= 30) {//悶熱
                    views.clear();
                    int images[] = {R.drawable.m, R.drawable.n, R.drawable.o};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test >= 30) {//易中暑
                    views.clear();
                    int images[] = {R.drawable.p, R.drawable.q, R.drawable.r};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                }
                viewPager.setAdapter(new MyAdapter());
                viewPager.setCurrentItem(1);
                AlertDialog builder = new AlertDialog.Builder(getActivity())
                        .setMessage(test1+"\t\t推薦穿搭")
                        .setView(viewPager)
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
                    mMessageView.setTextColor(Color.rgb(69,69,69));
                    mMessageView.setTextSize(20);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                builder.getWindow().setLayout(1000, 1200);
            }
        });

        clothbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ViewPager viewPager = new ViewPager(getActivity());
                if (test2 <= 10) { //非常寒冷
                    views.clear();
                    int images[] = {R.drawable.a, R.drawable.b, R.drawable.c};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test2 > 10 && test2 <= 15) {//寒冷
                    views.clear();
                    int images[] = {R.drawable.d, R.drawable.e, R.drawable.f};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test2 > 15 && test2 <= 20) {//稍寒
                    views.clear();
                    int images[] = {R.drawable.g, R.drawable.h, R.drawable.i};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test2 > 20 && test2 <= 25) {//舒適
                    views.clear();
                    int images[] = {R.drawable.j, R.drawable.k, R.drawable.l};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test2 > 25 && test2 <= 30) {//悶熱
                    views.clear();
                    int images[] = {R.drawable.m, R.drawable.n, R.drawable.o};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                } else if (test2 >= 30) {//易中暑
                    views.clear();
                    int images[] = {R.drawable.p, R.drawable.q, R.drawable.r};
                    for (int z = 0; z < images.length; z++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setImageResource(images[z]);
                        views.add(imageView);
                    }
                }
                viewPager.setAdapter(new MyAdapter());
                viewPager.setCurrentItem(1);
                AlertDialog builder = new AlertDialog.Builder(getActivity())
                        .setMessage(test3+"\t\t推薦穿搭")
                        .setView(viewPager)
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
                    mMessageView.setTextColor(Color.rgb(69,69,69));
                    mMessageView.setTextSize(20);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                builder.getWindow().setLayout(1000, 1200);
            }
        });

        String[] d = new String[24];
        String[] a = new String[24];
        String[] b = new String[24];
        String[] c = new String[24];
        String[] e = new String[24];
        for (int index = 0; index < 24; index++) {
            String m = (String) datetime.get(index).subSequence(5, 7);
            if (m.subSequence(0,1).equals("0")){
                m = " " + m.subSequence(1,2) + "月";
            }else {
                m = m + "月";
            }
            String n = (String) datetime.get(index).subSequence(8, 10);
            if (n.subSequence(0,1).equals("0")){
                n = " " + n + "日";
            }else{
                n = n + "日";
            }
            d[index] = m + n;
            a[index] = datetime.get(index).subSequence(11, 13) + "時";
            b[index] = temp.get(index);
            e[index] = icon.get(index * 2+1);
            int index2 = index / 2;
            int tmp_r1 = (index - 1) / 2;//不重要
            int tmp_r2 = (index + 1) / 2;//跟上面差不多
            if (datetime.get(0).equals(starttime.get(0))) {//第一筆資料有雨量
                if (index % 2 == 0) {
                    if(rainy.size() == 11 && index == 22){
                        c[index] = c[index - 1];
                    } else {
                        c[index] = rainy.get(index2);
                    }
                } else {
                    if(index == 23){
                        c[index] = c[index - 1];
                    } else if(rainy.size() == 11 && index == 21){
                        c[index] = c[index2];
                    } else if(Integer.valueOf(rainy.get(tmp_r1)) >= Integer.valueOf(rainy.get(tmp_r2))){
                        c[index] = rainy.get(tmp_r1);
                    } else {
                        c[index] = rainy.get(tmp_r2);
                    }
                }
            } else {//第一筆資料沒有雨量
                if (index % 2 == 1) {
                    if(rainy.size() == 11 && index == 23){
                        c[index] = c[index - 1];
                    } else {
                        c[index] = rainy.get(index2);
                    }
                } else {
                    if(index == 0){
                        c[index] = rainy.get(index2);
                    } else if(rainy.size() == 11 && index == 22){
                        c[index] = c[index - 1];
                    } else if(Integer.valueOf(rainy.get(tmp_r1)) >= Integer.valueOf(rainy.get(tmp_r2))){
                        c[index] = rainy.get(tmp_r1);
                    } else {
                        c[index] = rainy.get(tmp_r2);
                    }
                }
            }
        }
        int count = 0;
        while (d[count + 1].equals(d[count])) {
            count += 1;
        }
        int count2 = 0;
        d1.append(d[0]); //第一天的日期 改
        tv1.append("\t\t\t\t\t");
        for (int i = 0; i <= count; i++) {
            tv1.append(a[i] + "\t\t\t\t\t\t");  //第一天的時間
        }
        tv1.append("\n\t\t\t\t\t");
        for (int i = 0; i <= count; i++) {
            if (e[count2 + i].equals("08") || e[count2 + i].equals("09") || e[count2 + i].equals("10") || e[count2 + i].equals("11") || e[count2 + i].equals("12") || e[count2 + i].equals("13") || e[count2 + i].equals("14") || e[count2 + i].equals("20") || e[count2 + i].equals("29") || e[count2 + i].equals("30") || e[count2 + i].equals("31") || e[count2 + i].equals("32") || e[count2 + i].equals("38") || e[count2 + i].equals("39")) {//短暫雨
                tv1.append(Html.fromHtml(imgStr, imageGetter, null));
                tv1.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("01") || e[count2 + i].equals("02") || e[count2 + i].equals("03") || e[count2 + i].equals("24")) {//晴
                tv1.append(Html.fromHtml(imgStr3, imageGetter, null));
                tv1.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("04") || e[count2 + i].equals("05") || e[count2 + i].equals("25") || e[count2 + i].equals("26") || e[count2 + i].equals("27")) {//多雲
                tv1.append(Html.fromHtml(imgStr2, imageGetter, null));
                tv1.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("06") || e[count2 + i].equals("07") || e[count2 + i].equals("28")) {//陰
                tv1.append(Html.fromHtml(imgStr1, imageGetter, null));
                tv1.append("\t\t\t\t\t\t\t ");
            }else if (e[count2 + i].equals("15") || e[count2 + i].equals("16") || e[count2 + i].equals("17") ||e[count2 + i].equals("18") || e[count2 + i].equals("19") || e[count2 + i].equals("21") || e[count2 + i].equals("22") || e[count2 + i].equals("33") || e[count2 + i].equals("34") || e[count2 + i].equals("35") || e[count2 + i].equals("36") || e[count2 + i].equals("41")) {//短暫陣雨或雷雨
                tv1.append(Html.fromHtml(imgStr4, imageGetter, null));
                tv1.append("\t\t\t\t\t\t\t ");
            }
        }
        tv1.append("\n\t\t\t\t\t");
        for (int i = 0; i <= count; i++) {
            tv1.append(b[i] + "°C\t\t\t\t\t\t"); // 第一天的溫度
        }
        tv1.append("\n\t\t\t\t\t");
        for (int i = 0; i <= count; i++) {
            if(c[count2 + i].equals("0")){
                tv1.append("10 %"+"\t\t\t\t\t\t"); //第一天的雨量
            } else if(c[count2 + i].equals("100")){
                tv1.append("100 %\t\t\t\t\t"); //第一天的雨量
            } else {
                tv1.append(c[count2 + i] + " %\t\t\t\t\t\t"); //第一天的雨量
            }
        }
        count2 += count;
        d2.append(d[count2 + 1]); //第二天的日期 改
        tv2.append("\t\t\t\t\t");
        for (int i = 1; i <= 8; i++) {
            tv2.append(a[count2 + i] + "\t\t\t\t\t\t"); //第二天的時間
        }
        tv2.append("\n\t\t\t\t\t");
        for (int i = 1; i <= 8; i++) {
            if (e[count2 + i].equals("08") || e[count2 + i].equals("09") || e[count2 + i].equals("10") || e[count2 + i].equals("11") || e[count2 + i].equals("12") || e[count2 + i].equals("13") || e[count2 + i].equals("14") || e[count2 + i].equals("20") || e[count2 + i].equals("29") || e[count2 + i].equals("30") || e[count2 + i].equals("31") || e[count2 + i].equals("32") || e[count2 + i].equals("38") || e[count2 + i].equals("39")) {//短暫雨
                tv2.append(Html.fromHtml(imgStr, imageGetter, null));
                tv2.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("01") || e[count2 + i].equals("02") || e[count2 + i].equals("03") || e[count2 + i].equals("24")) {//晴
                tv2.append(Html.fromHtml(imgStr3, imageGetter, null));
                tv2.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("04") || e[count2 + i].equals("05") || e[count2 + i].equals("25") || e[count2 + i].equals("26") || e[count2 + i].equals("27")) {//多雲
                tv2.append(Html.fromHtml(imgStr2, imageGetter, null));
                tv2.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("06") || e[count2 + i].equals("07") || e[count2 + i].equals("28")) {//陰
                tv2.append(Html.fromHtml(imgStr1, imageGetter, null));
                tv2.append("\t\t\t\t\t\t\t ");
            }else if (e[count2 + i].equals("15") || e[count2 + i].equals("16") || e[count2 + i].equals("17") ||e[count2 + i].equals("18") || e[count2 + i].equals("19") || e[count2 + i].equals("21") || e[count2 + i].equals("22") || e[count2 + i].equals("33") || e[count2 + i].equals("34") || e[count2 + i].equals("35") || e[count2 + i].equals("36") || e[count2 + i].equals("41")) {//短暫陣雨或雷雨
                tv2.append(Html.fromHtml(imgStr4, imageGetter, null));
                tv2.append("\t\t\t\t\t\t\t ");
            }
        }
        tv2.append("\n\t\t\t\t\t");
        for (int i = 1; i <= 8; i++) {
            tv2.append(b[count2 + i] + "°C\t\t\t\t\t\t");//第二天的溫度
        }
        tv2.append("\n\t\t\t\t\t");
        for (int i = 1; i <= 8; i++) {
            if(c[count2 + i].equals("0")){
                tv2.append("10 %"+"\t\t\t\t\t\t"); //第二天的雨量
            } else if(c[count2 + i].equals("100")){
                tv2.append("100 %\t\t\t\t\t"); //第一天的雨量
            } else {
                tv2.append(c[count2 + i] + " %\t\t\t\t\t\t"); //第二天的雨量
            }
        }
        count2 += 9;
        d3.append(d[count2]); //第三天的日期 改
        tv3.append("\t\t\t\t\t");
        for (int i = 0; i < 8; i++) {
            tv3.append(a[count2 + i] + "\t\t\t\t\t\t"); //第三天的時間
        }
        tv3.append("\n\t\t\t\t\t");
        for (int i = 0; i < 8; i++) {
            if (e[count2 + i].equals("08") || e[count2 + i].equals("09") || e[count2 + i].equals("10") || e[count2 + i].equals("11") || e[count2 + i].equals("12") || e[count2 + i].equals("13") || e[count2 + i].equals("14") || e[count2 + i].equals("20") || e[count2 + i].equals("29") || e[count2 + i].equals("30") || e[count2 + i].equals("31") || e[count2 + i].equals("32") || e[count2 + i].equals("38") || e[count2 + i].equals("39")) {//短暫雨
                tv3.append(Html.fromHtml(imgStr, imageGetter, null));
                tv3.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("01") || e[count2 + i].equals("02") || e[count2 + i].equals("03") || e[count2 + i].equals("24")) {//晴
                tv3.append(Html.fromHtml(imgStr3, imageGetter, null));
                tv3.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("04") || e[count2 + i].equals("05") || e[count2 + i].equals("25") || e[count2 + i].equals("26") || e[count2 + i].equals("27")) {//多雲
                tv3.append(Html.fromHtml(imgStr2, imageGetter, null));
                tv3.append("\t\t\t\t\t\t\t ");
            } else if (e[count2 + i].equals("06") || e[count2 + i].equals("07") || e[count2 + i].equals("28")) {//陰
                tv3.append(Html.fromHtml(imgStr1, imageGetter, null));
                tv3.append("\t\t\t\t\t\t\t ");
            }else if (e[count2 + i].equals("15") || e[count2 + i].equals("16") || e[count2 + i].equals("17") ||e[count2 + i].equals("18") || e[count2 + i].equals("19") || e[count2 + i].equals("21") || e[count2 + i].equals("22") || e[count2 + i].equals("33") || e[count2 + i].equals("34") || e[count2 + i].equals("35") || e[count2 + i].equals("36") || e[count2 + i].equals("41")) {//短暫陣雨或雷雨
                tv3.append(Html.fromHtml(imgStr4, imageGetter, null));
                tv3.append("\t\t\t\t\t\t\t ");
            }
        }
        tv3.append("\n\t\t\t\t\t");
        for (int i = 0; i < 8; i++) {
            tv3.append(b[count2 + i] + "°C\t\t\t\t\t\t"); //第三天的溫度
        }
        tv3.append("\n\t\t\t\t\t");
        for (int i = 0; i < 8; i++) {
            if(c[count2 + i].equals("0")){
                tv3.append("10 %"+"\t\t\t\t\t\t"); //第三天的雨量
            } else if(c[count2 + i].equals("100")){
                tv3.append("100 %\t\t\t\t\t"); //第一天的雨量
            } else {
                tv3.append(c[count2 + i] + " %\t\t\t\t\t\t"); //第三天的雨量
            }
        }
        count2 += 8;
        tv4.append("\t\t\t\t\t");
        if(count2 < 24){
            d4.append(d[count2]); //第四天的日期 改
            int count3 = 24 - count2;
            for (int i = 0; i < count3; i++) {
                tv4.append(a[count2 + i] + "\t\t\t\t\t\t"); //第四天的時間
            }
            tv4.append("\n\t\t\t\t\t");
            for (int i = 0; i < count3; i++) {
                if (e[count2 + i].equals("08") || e[count2 + i].equals("09") || e[count2 + i].equals("10") || e[count2 + i].equals("11") || e[count2 + i].equals("12") || e[count2 + i].equals("13") || e[count2 + i].equals("14") || e[count2 + i].equals("20") || e[count2 + i].equals("29") || e[count2 + i].equals("30") || e[count2 + i].equals("31") || e[count2 + i].equals("32") || e[count2 + i].equals("38") || e[count2 + i].equals("39")) {//短暫雨
                    tv4.append(Html.fromHtml(imgStr, imageGetter, null));
                    tv4.append("\t\t\t\t\t\t\t ");
                } else if (e[count2 + i].equals("01") || e[count2 + i].equals("02") || e[count2 + i].equals("03") || e[count2 + i].equals("24")) {//晴
                    tv4.append(Html.fromHtml(imgStr3, imageGetter, null));
                    tv4.append("\t\t\t\t\t\t\t ");
                } else if (e[count2 + i].equals("04") || e[count2 + i].equals("05") || e[count2 + i].equals("25") || e[count2 + i].equals("26") || e[count2 + i].equals("27")) {//多雲
                    tv4.append(Html.fromHtml(imgStr2, imageGetter, null));
                    tv4.append("\t\t\t\t\t\t\t ");
                } else if (e[count2 + i].equals("06") || e[count2 + i].equals("07") || e[count2 + i].equals("28")) {//陰
                    tv4.append(Html.fromHtml(imgStr1, imageGetter, null));
                    tv4.append("\t\t\t\t\t\t\t ");
                }else if (e[count2 + i].equals("15") || e[count2 + i].equals("16") || e[count2 + i].equals("17") ||e[count2 + i].equals("18") || e[count2 + i].equals("19") || e[count2 + i].equals("21") || e[count2 + i].equals("22") || e[count2 + i].equals("33") || e[count2 + i].equals("34") || e[count2 + i].equals("35") || e[count2 + i].equals("36") || e[count2 + i].equals("41")) {//短暫陣雨或雷雨
                    tv4.append(Html.fromHtml(imgStr4, imageGetter, null));
                    tv4.append("\t\t\t\t\t\t\t ");
                }
            }
            tv4.append("\n\t\t\t\t\t");
            for (int i = 0; i < count3; i++) {
                tv4.append(b[count2 + i] + "°C\t\t\t\t\t\t"); //第四天的溫度
            }
            tv4.append("\n\t\t\t\t\t");
            for (int i = 0; i < count3; i++) {
                if(c[count2 + i].equals("0")){
                    tv4.append("10 %"+"\t\t\t\t\t\t"); //第四天的雨量
                } else if(c[count2 + i].equals("100")){
                    tv4.append("100 %\t\t\t\t\t"); //第一天的雨量
                } else {
                    tv4.append(c[count2 + i] + " %\t\t\t\t\t\t"); //第四天的雨量
                }
            }
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

    class MyAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return views.size();
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position);
            container.addView(v);
            return v;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View v = views.get(position);
            container.removeView(v);
        }
    }
}