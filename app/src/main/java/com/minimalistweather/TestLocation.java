package com.minimalistweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class TestLocation extends AppCompatActivity {

    public AMapLocationClient mLocationClient = null;
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation != null) {
                if(aMapLocation.getErrorCode() == 0) {
                    // 解析aMapLocation
                    Toast.makeText(TestLocation.this, "数据来源：" + aMapLocation.getLocationType(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestLocation.this, "国家：" + aMapLocation.getCountry(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestLocation.this, "位置信息：" + aMapLocation.getAddress(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestLocation.this, "地区信息：" + aMapLocation.getDistrict(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestLocation.this, "城市编码：" + aMapLocation.getCityCode(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(TestLocation.this, "地区编码：" + aMapLocation.getAdCode(), Toast.LENGTH_SHORT).show();
                }else {
                    // 定位失败
                    Log.e("AmapError","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo: "
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };
    public AMapLocationClientOption mLocationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 测试高德地图定位
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 申请网络定位权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 申请GPS定位权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请写入缓存数据的权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请读取缓存数据的权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 申请读取手机当前状态的权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }

        // 初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        // 设置参数
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy); // 高精度模式
        mLocationOption.setOnceLocation(true); // 单次定位
        mLocationOption.setNeedAddress(true); // 返回地址信息
        mLocationOption.setLocationCacheEnable(false); // 关闭缓存机制
        mLocationClient.setLocationOption(mLocationOption); // 给客户端对象设置定位参数
        // 启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();
    }
}
