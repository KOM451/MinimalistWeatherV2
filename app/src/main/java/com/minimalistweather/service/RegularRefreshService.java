package com.minimalistweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.minimalistweather.entity.gson_entity.HeWeatherAirQuality;
import com.minimalistweather.entity.gson_entity.HeWeatherForecast;
import com.minimalistweather.entity.gson_entity.HeWeatherLifestyle;
import com.minimalistweather.entity.gson_entity.HeWeatherNow;
import com.minimalistweather.util.BaseConfigUtil;
import com.minimalistweather.util.HttpUtil;
import com.minimalistweather.util.JsonParser;
import com.minimalistweather.view.fragment.WeatherFragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 定时刷新数据服务
 */
public class RegularRefreshService extends Service {

    private static final String TAG = "RegularRefreshService";

    AlarmManager mAlarmManager;

    PendingIntent mPendingIntent;

    public RegularRefreshService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "定时刷新服务启动");
        updateWeather();
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int refreshInterval = 1000 * 10; // 自动刷新时间间隔
        long refreshTime = SystemClock.elapsedRealtime() + refreshInterval;
        Intent intentService = new Intent(this, RegularRefreshService.class);
        mPendingIntent = PendingIntent.getService(this, 0, intentService, 0);
        mAlarmManager.cancel(mPendingIntent);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, refreshTime, mPendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAlarmManager.cancel(mPendingIntent);
        Log.i(TAG, "定时刷新任务销毁");
    }

    public void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherNow = sharedPreferences.getString("weather_now", null);
        if (weatherNow != null) {
            HeWeatherNow heWeatherNow = JsonParser.parseWeatherNowResponse(weatherNow);
            String weatherId = heWeatherNow.basic.cid;

            final Intent intent = new Intent();
            intent.setAction(WeatherFragment.ACTION_REFRESH);
            intent.putExtra("weather_id", weatherId);
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
