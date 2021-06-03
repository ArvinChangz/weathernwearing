package com.example.weather_wearing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShowActivity extends AppCompatActivity {
    public static final String TAG = "Menu_TAG";

    String name = "";
    String city = "";
    String dist = "";
    String airid = "";
    String uviname = "";
    String searchloc = "";
    Double searchlat;
    Double searchlon;
    //所有測站名+經緯度(新增搜尋)
    ArrayList<String> all_station = new ArrayList<>();
    ArrayList<String> all_latstr = new ArrayList<>();
    ArrayList<String> all_lonstr = new ArrayList<>();
    ArrayList<String> all_address = new ArrayList<>();
    //現在位置
    double mylat = 0.0;
    double mylon = 0.0;
    //顯示
    TextView topp;
    //設定
    int rain = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        topp = findViewById(R.id.top);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);//原本true
        getSupportActionBar().setDisplayShowHomeEnabled(false);//原本true
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_weathermap, R.id.navigation_home,  R.id.navigation_weatherdetail)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //接值
        Bundle bundle = getIntent().getExtras();
        String top = bundle.getString("top");

        int r = bundle.getInt("R");
        int g = bundle.getInt("G");
        int b = bundle.getInt("B");

        if(r+g+b != 0) {
            toolbar.setBackgroundColor(Color.rgb(r, g, b));
        }

        int tr = bundle.getInt("TR");
        int tg = bundle.getInt("TG");
        int tb = bundle.getInt("TB");

        if(tr + tg + tb != 0) {
            topp.setTextColor(Color.rgb(tr,tg,tb));
        }

        topp.setText(top);

        name = bundle.getString("pass_name");
        city = bundle.getString("pass_city");
        dist = bundle.getString("pass_dist");
        airid = bundle.getString("airid");
        uviname = bundle.getString("uviname");
        searchlat = bundle.getDouble("searchlat");
        searchlon = bundle.getDouble("searchlon");
        searchloc = bundle.getString("loc");
        //所有測站名+經緯度(新增搜尋)
        all_station = bundle.getStringArrayList("stationn");
        all_latstr = bundle.getStringArrayList("latstr");
        all_lonstr = bundle.getStringArrayList("lonstr");
        all_address = bundle.getStringArrayList("address");
        //現在位置
        mylat = bundle.getDouble("mylat");
        mylon = bundle.getDouble("mylon");

        int index = 0;
        String[] ids = {"001", "005", "009", "013", "017", "021", "025", "029", "033", "037", "041", "045", "049", "053", "057", "061", "065", "069", "073", "077", "081", "085"};
        String[] idsp = {"宜蘭縣", "桃園市", "新竹縣", "苗栗縣", "彰化縣", "南投縣", "雲林縣", "嘉義縣", "屏東縣", "臺東縣", "花蓮縣", "澎湖縣", "基隆縣", "新竹市", "嘉義市", "臺北市", "高雄市", "新北市", "臺中市", "臺南市", "連江縣", "金門縣"};
        for (int t = 0; t < idsp.length; t++) {
            if (idsp[t].equals(city)) {
                index = t;
            }
        }
        String url3 = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-" + ids[index] + "?Authorization=CWB-F1D4B318-40F0-4861-A619-6735B2477742&elementName=T,PoP6h,Wx";
        getData_rain(url3); //取得網址字串
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                Log.i(TAG, "action search selected");
                //傳給ManagementActivity
                Intent s = new Intent(ShowActivity.this, ManageLocationActivity.class);
                s.putStringArrayListExtra("station",all_station); //所有測站名稱
                s.putExtra("latstr", all_latstr); //所有緯度
                s.putExtra("lonstr", all_lonstr); //所有經度
                s.putExtra("address", all_address);
                startActivity(s);
                return true;
            case R.id.menu_setting:
                Log.i(TAG, "action settings selected");
                Intent r = new Intent(ShowActivity.this, SettingsActivity.class);
                startActivity(r);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onBackPressed() {
//        鎖住Back鍵
//        如tbtn被選的話，不執行super 就可以把Back預設行為無效
    }
    public String getStationName() {
        return name;
    }
    public String getCityName() {
        return city;
    }

    public String getDistName() {
        return dist;
    }
    public String getAirId() {
        return airid;
    }
    public String getUviName() {
        return uviname;
    }
    public Double getSearchlat() {
        return searchlat;
    }
    public Double getSearchlon() {
        return searchlon;
    }
    public String searchlocation() {
        return searchloc;
    }

    //現在位置
    public double getMylat(){
        return mylat;
    }
    public double getMylon(){
        return mylon;
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
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void parseJSON_rain(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONObject("records").getJSONArray("locations");
        ArrayList<String> starttime = new ArrayList<>();
        ArrayList<String> rainy = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.getJSONObject(i);
            JSONArray data1 = o.getJSONArray("location");
            for (int j = 0; j < data1.length(); j++) {
                JSONObject o2 = data1.getJSONObject(j);
                if (o2.getString("locationName").equals(dist)) {
                    JSONArray data2 = o2.getJSONArray("weatherElement");
                    for (int k = 0; k < data2.length(); k++) {
                        JSONObject o3 = data2.getJSONObject(k);
                        if (o3.getString("elementName").equals("PoP6h")) {
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
                        }
                    }
                }
            }
        }
        for (int g = 0; g < starttime.size(); g++) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = dateFormat.parse(starttime.get(g));
                Date curDate = new Date(System.currentTimeMillis());
                if (curDate.getTime() < date.getTime()) {
                    rain = Integer.valueOf(rainy.get(g));
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "降雨機率：" + rain + "%");
        NotificationHelper.rain_Future = rain;
    }

    //ViewPage連結
    public void non(View view){
        Toast.makeText(ShowActivity.this,"查無此商品", Toast.LENGTH_LONG).show();
    }

    public void am2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425030"));
        startActivity(intent);
    }
    public void am3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/420314"));
        startActivity(intent);
    }
    public void am4(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422967"));
        startActivity(intent);
    }
    public void aw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/420253"));
        startActivity(intent);
    }
    public void bm4(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422363"));
        startActivity(intent);
    }
    public void cm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/419993"));
        startActivity(intent);
    }
    public void cw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/421618"));
        startActivity(intent);
    }
    public void dm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425030"));
        startActivity(intent);
    }
    public void dm3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/418917"));
        startActivity(intent);
    }
    public void dw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425030"));
        startActivity(intent);
    }
    public void dw3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/427736"));
        startActivity(intent);
    }
    public void em1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/418705"));
        startActivity(intent);
    }
    public void em2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425415"));
        startActivity(intent);
    }
    public void ew3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/424578"));
        startActivity(intent);
    }
    public void ew4(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425344"));
        startActivity(intent);
    }
    public void fm1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425059"));
        startActivity(intent);
    }
    public void fm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/423571"));
        startActivity(intent);
    }
    public void fm3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/415539"));
        startActivity(intent);
    }
    public void fw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/426232"));
        startActivity(intent);
    }
    public void fw3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425501"));
        startActivity(intent);
    }
    public void gm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425042"));
        startActivity(intent);
    }
    public void gm3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422360"));
        startActivity(intent);
    }
    public void gw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/424586"));
        startActivity(intent);
    }
    public void gw3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/430616"));
        startActivity(intent);
    }
    public void hm1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425104"));
        startActivity(intent);
    }
    public void hm3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425872"));
        startActivity(intent);
    }
    public void hw3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425500"));
        startActivity(intent);
    }
    public void im2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422986"));
        startActivity(intent);
    }
    public void iw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425125"));
        startActivity(intent);
    }
    public void iw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422986"));
        startActivity(intent);
    }
    public void jm1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422996"));
        startActivity(intent);
    }
    public void jm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425302"));
        startActivity(intent);
    }
    public void jw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422699"));
        startActivity(intent);
    }
    public void jw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/426284"));
        startActivity(intent);
    }
    public void km1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/423983"));
        startActivity(intent);
    }
    public void km2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/428591"));
        startActivity(intent);
    }
    public void kw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/424192"));
        startActivity(intent);
    }
    public void lm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422983"));
        startActivity(intent);
    }
    public void lw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/426985"));
        startActivity(intent);
    }
    public void mm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/424145"));
        startActivity(intent);
    }
    public void mw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/426442"));
        startActivity(intent);
    }
    public void nm1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/315406"));
        startActivity(intent);
    }
    public void nm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/305116"));
        startActivity(intent);
    }
    public void nw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/316887"));
        startActivity(intent);
    }
    public void nw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/311109"));
        startActivity(intent);
    }
    public void nw3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/317776"));
        startActivity(intent);
    }
    public void om1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425104"));
        startActivity(intent);
    }
    public void om2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425145"));
        startActivity(intent);
    }
    public void ow1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/422722"));
        startActivity(intent);
    }
    public void ow2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425551"));
        startActivity(intent);
    }
    public void pm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/424146"));
        startActivity(intent);
    }
    public void pw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425471"));
        startActivity(intent);
    }
    public void pw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.uniqlo.com/tw/store/goods/425550"));
        startActivity(intent);
    }
    public void qw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/313343"));
        startActivity(intent);
    }
    public void rm1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/309867"));
        startActivity(intent);
    }
    public void rm2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/313560"));
        startActivity(intent);
    }
    public void rm3(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/289021"));
        startActivity(intent);
    }
    public void rw1(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/319486"));
        startActivity(intent);
    }
    public void rw2(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.net-fashion.net/product/311783"));
        startActivity(intent);
    }
}
