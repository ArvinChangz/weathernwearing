package com.example.weather_wearing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class Databaseadd extends SQLiteOpenHelper {
    String TAG = Database.class.getSimpleName();
    String TableName;

    public Databaseadd(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, String TableName) {
        super(context, name, factory, version);
        this.TableName = TableName;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQLTable = "CREATE TABLE IF NOT EXISTS " + TableName + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Station TEXT, " +
                "Distance TEXT," +
                "Location TEXT," +
                "Lat TEXT," +
                "Lon TEXT," +
                "City TEXT," +
                "Dist TEXT,"+
                "Air TEXT," +
                "Uvi TEXT "+//最後一個沒有逗號！！
                ");";
        db.execSQL(SQLTable);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE " + TableName;
        db.execSQL(SQL);
    }
    //檢查資料表狀態，若無指定資料表則新增
    public void chickTable(){
        Cursor cursor = getWritableDatabase().rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '" + TableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() == 0)
                getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS " + TableName + "( " +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "Station TEXT, " +
                        "Distance TEXT," +
                        "Location TEXT, " +
                        "Lat TEXT," +
                        "Lon TEXT," +
                        "City TEXT, " +
                        "Dist TEXT, "+
                        "Air TEXT, " +
                        "Uvi TEXT "+
                        ");");
            cursor.close();
        }
    }
    //取得有多少資料表,並以陣列回傳
    public ArrayList<String> getTables(){
        Cursor cursor = getWritableDatabase().rawQuery(
                "select DISTINCT tbl_name from sqlite_master", null);
        ArrayList<String> tables = new ArrayList<>();
        while (cursor.moveToNext()){
            String getTab = new String (cursor.getBlob(0));
            if (getTab.contains("android_metadata")){}
            else if (getTab.contains("sqlite_sequence")){}
            else tables.add(getTab.substring(0,getTab.length()-1));

        }
        return tables;
    }
    //新增資料
    public void addData(String station,String distance,String location,Double lat,Double lon,String city,String dist,String air,String uvi) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("station", station);
        values.put("distance", distance);
        values.put("location", location);
        values.put("lat",lat);
        values.put("lon",lon);
        values.put("city", city);
        values.put("dist", dist);
        values.put("air",air);
        values.put("uvi",uvi);

        db.insert(TableName, null, values);
    }

    //顯示所有資料
    public ArrayList<HashMap<String, String>> showAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + TableName, null);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        while (c.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();

            String id = c.getString(0);
            String station = c.getString(1);
            String distance = c.getString(2);
            String location = c.getString(3);
            Double lat = c.getDouble(4);
            Double lon = c.getDouble(5);
            String city = c.getString(6);
            String dist = c.getString(7);
            String air = c.getString(8);
            String uvi = c.getString(9);

            hashMap.put("id", id);
            hashMap.put("station", station);
            hashMap.put("distance", distance);
            hashMap.put("location",location);
            hashMap.put("lat",lat.toString());
            hashMap.put("lon",lon.toString());
            hashMap.put("city",city);
            hashMap.put("dist",dist);
            hashMap.put("air",air);
            hashMap.put("uvi",uvi);

            arrayList.add(hashMap);
        }
        return arrayList;

    }
    //以id搜尋特定資料
    public ArrayList<HashMap<String,String>> searchById(String getId){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + TableName
                + " WHERE _id =" + "'" + getId + "'", null);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        while (c.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();


            String id = c.getString(0);
            String station = c.getString(1);
            String distance = c.getString(2);
            String location = c.getString(3);
            Double lat = c.getDouble(4);
            Double lon = c.getDouble(5);
            String city = c.getString(6);
            String dist = c.getString(7);
            String air = c.getString(8);
            String uvi = c.getString(9);

            hashMap.put("id", id);
            hashMap.put("station", station);
            hashMap.put("distance", distance);
            hashMap.put("location",location);
            hashMap.put("lat",lat.toString());
            hashMap.put("lon",lon.toString());
            hashMap.put("city",city);
            hashMap.put("dist",dist);
            hashMap.put("air",air);
            hashMap.put("uvi",uvi);

            arrayList.add(hashMap);
        }
        return arrayList;
    }
    //以id刪除資料(簡單)
    public void deleteByStation(String station){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TableName, "Station='"+ station + "'",null);
    }
    //刪除全部資料
    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM"+TableName);
    }
}
