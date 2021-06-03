package com.example.weather_wearing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {

    private final int REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION = 100;

    double mylat;
    double mylon;

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager mLocationMgr;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;
                Location location = locationResult.getLastLocation();
                updateMapLocation(location);
            }
        };
        mLocationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        enableLocation(false);
    }
    @Override
    public void onStop() {
        super.onStop();
        //Toast.makeText(MapsActivity.this, "已停用 Google API", Toast.LENGTH_LONG).show();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION) {
            if (grantResults.length != 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        enableLocation(true);
    }
    @Override
    public void onConnectionSuspended(int i) {
        switch (i) {
            case CAUSE_NETWORK_LOST:
                break;
            case CAUSE_SERVICE_DISCONNECTED:
                break;
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Toast.makeText(MapsActivity.this, "Google API 連接失敗",Toast.LENGTH_LONG).show();
    }

    public void enableLocation(boolean on) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // 這項功能尚未取得使用者的同意
            // 開始執行徵詢使用者的流程
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(this);
                altDlgBuilder.setTitle("提示");
                altDlgBuilder.setMessage("需要位置權限");
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                // 顯示詢問使用者是否同意功能權限的對話盒
                                // 使用者答覆後會執行onRequestPermissionsResult()
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{
                                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                            }
                        });
                altDlgBuilder.show();
                return;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                return;
            }
        }
        // 已同意權限
        if (on) {
            // 取得上一次定位資料
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) { //成功取得上次定位
                        updateMapLocation(location);
                    } else { //沒有上次定位資料
                        AlertDialog.Builder altDlgBuilder = new AlertDialog.Builder(MainActivity.this);
                        altDlgBuilder.setTitle("提示");
                        altDlgBuilder.setMessage("請開啟定位功能，並重新啟動");
                        altDlgBuilder.setCancelable(false);
                        altDlgBuilder.setPositiveButton("確定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                        altDlgBuilder.show();
                    }
                }
            });
            LocationRequest locationRequest = LocationRequest.create();
            // 更新時間(毫秒)
            //locationRequest.setInterval(5000);
            locationRequest.setSmallestDisplacement(5);
            if (mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                //Toast.makeText(MapsActivity.this, "使用GPS定位",Toast.LENGTH_LONG).show();
            } else if (mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                //Toast.makeText(MapsActivity.this, "使用網路定位",Toast.LENGTH_LONG).show();
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        } else {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            //Toast.makeText(MapsActivity.this, "停止定位", Toast.LENGTH_LONG).show();
        }
    }

    private void updateMapLocation(Location location) {
        mylat = location.getLatitude();
        mylon = location.getLongitude();
        Intent intent = new Intent(MainActivity.this,DataActivity.class);
        intent.putExtra("mylat",location.getLatitude());
        intent.putExtra("mylon",location.getLongitude());
        startActivity(intent);
    }
}




