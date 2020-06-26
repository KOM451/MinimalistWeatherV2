package com.minimalistweather.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.minimalistweather.entity.database_entity.ManagedCity;
import com.minimalistweather.entity.gson_entity.Location;
import com.minimalistweather.util.BaseConfigUtil;
import com.minimalistweather.util.HttpUtil;
import com.minimalistweather.util.JsonParser;
import com.minimalistweather.view.fragment.WeatherFragment;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
                String url = "https://search.heweather.net/find?location="
                        + BaseConfigUtil.AD_CODE
                        + "&key=1f973beb7602432bb31cdceb9da27525";
                HttpUtil.sendHttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseStr = response.body().string();
                        Location location = JsonParser.parseLocation(responseStr);
                        if (location != null && BaseConfigUtil.API_STATUS_OK.equals(location.status)) {
                            String cid = location.basic.get(0).cid;
                            if (LitePal.where("cid = ?", String.valueOf(cid)).find(ManagedCity.class) == null) {
                                String districtName = location.basic.get(0).location;
                                ManagedCity city = new ManagedCity();
                                city.setCid(cid);
                                city.setCityName(districtName);
                                city.save();
                            }

                            BaseConfigUtil.CID = cid;

                            final Intent intent = new Intent();
                            intent.setAction(WeatherFragment.ACTION_UPDATE);
                            intent.putExtra("weather_id", cid);
                            sendBroadcast(intent);
                        }
                    }
                });
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
        mLocationOption.setInterval(10000);
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
