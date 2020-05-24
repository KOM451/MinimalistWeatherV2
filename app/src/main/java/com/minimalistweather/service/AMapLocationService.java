package com.minimalistweather.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.minimalistweather.util.BaseConfigUtil;

public class AMapLocationService extends Service {

    private static final String TAG = "AMapLocationService";

    // 声明定位客户端
    public AMapLocationClient mLocationClient = null;
    // 声明回调监听器
    public AMapLocationListener mLocationListener = aMapLocation -> {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                // 解析定位信息
                BaseConfigUtil.AD_CODE = aMapLocation.getAdCode();
                Log.i(TAG, "当前地区编码：" + BaseConfigUtil.AD_CODE);
            } else {
                String errContent = "定位失败，" + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e(TAG, errContent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "定位服务创建");
        // 初始化定位客户端对象
        mLocationClient = new AMapLocationClient(getApplicationContext());
        // 声明AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位间隔
        mLocationOption.setInterval(20000);
        // 设置超时时间
        mLocationOption.setHttpTimeOut(30000);
        mLocationClient.setLocationListener(mLocationListener);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
        Log.i(TAG, "定位服务销毁");
    }

    public AMapLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
