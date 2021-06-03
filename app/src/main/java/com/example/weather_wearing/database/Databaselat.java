package com.example.weather_wearing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.weather_wearing.database.Database;

import java.util.ArrayList;
import java.util.HashMap;

public class Databaselat extends SQLiteOpenHelper {
    //private static final String DATABASE_NAME = "Station";
    //private static final int DATABASE_VERSION = 1;
    String TAG = Database.class.getSimpleName();
    String TableName;

    public Databaselat(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version,String TableName) {
        super(context, name, factory, version);
        this.TableName = TableName;
    }
    public void onCreate(SQLiteDatabase db) {
        String SQLTable = "CREATE TABLE IF NOT EXISTS " + TableName + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Station TEXT, " +
                "Lat TEXT," +
                "Lon TEXT," +
                "Address TEXT" +//最後一個沒有逗號！！
                ");";
        db.execSQL(SQLTable);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE " + TableName;
        db.execSQL(SQL);
    }
    //檢查資料表狀態，若無指定資料表則新增
    public void chickLatTable(){
        Cursor cursor = getWritableDatabase().rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '" + TableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() == 0)
                getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS " + TableName + "( " +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "Station TEXT, " +
                        "Lat TEXT," +
                        "Lon TEXT," +
                        "Address TEXT" +
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
    public void addLatData(String station,String lat,String lon,String address) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Station",station);
        values.put("Lat",lat);
        values.put("Lon",lon);
        values.put("Address",address);
        db.insert(TableName, null, values);
    }

    //顯示所有資料
    public ArrayList<HashMap<String, String>> showlatAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + TableName, null);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        while (c.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();

            String id = c.getString(0);
            String station = c.getString(1);
            String lat = c.getString(2);
            String lon = c.getString(3);
            String address = c.getString(4);

            hashMap.put("id", id);
            hashMap.put("station", station);
            hashMap.put("lat", lat);
            hashMap.put("lon",lon);
            hashMap.put("address", address);

            arrayList.add(hashMap);
        }
        return arrayList;

    }
    //以id搜尋特定資料
    public ArrayList<HashMap<String,String>> searchlatById(String getId){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + TableName
                + " WHERE _id =" + "'" + getId + "'", null);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        while (c.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();

            String id = c.getString(0);
            String station = c.getString(1);
            String lat = c.getString(2);
            String lon = c.getString(3);
            String address = c.getString(4);

            hashMap.put("id", id);
            hashMap.put("station", station);
            hashMap.put("lat", lat);
            hashMap.put("lon",lon);
            hashMap.put("address", address);

            arrayList.add(hashMap);
        }
        c.close();
        return arrayList;
    }
    //以id刪除資料(簡單)
    public void deleteByIdEZ(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TableName,"_id = " + id,null);
    }

    //修改資料(簡單)
    public void modifyEZ(String id, String station,String lat,String lon,String address) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Station",station);
        values.put("Lat",lat);
        values.put("Lon",lon);
        values.put("Address",address);

        db.update(TableName, values, "_id = " + id, null);
        db.close();
    }

}
