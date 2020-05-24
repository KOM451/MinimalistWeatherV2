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
            // 刷新实况天气数据
            String weatherNowUrl = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=1f973beb7602432bb31cdceb9da27525";
            HttpUtil.sendHttpRequest(weatherNowUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseStr = response.body().string();
                    final HeWeatherNow heWeatherNow = JsonParser.parseWeatherNowResponse(responseStr);
                    if (heWeatherNow != null && BaseConfigUtil.API_STATUS_OK.equals(heWeatherNow.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RegularRefreshService.this).edit();
                        editor.putString("weather_now", responseStr);
                        editor.apply();
                        Log.i(TAG, "接口数据更新时间：" + heWeatherNow.update.loc);
                    }
                }
            });
            // 刷新3日天气预报
            String weatherForecastUrl = "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId + "&key=1f973beb7602432bb31cdceb9da27525";
            HttpUtil.sendHttpRequest(weatherForecastUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseStr = response.body().string();
                    final HeWeatherForecast heWeatherForecast = JsonParser.parseWeatherForecastResponse(responseStr);
                    if (heWeatherForecast != null && BaseConfigUtil.API_STATUS_OK.equals(heWeatherForecast.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RegularRefreshService.this).edit();
                        editor.putString("weather_forecast", responseStr);
                        editor.apply();
                        Log.i(TAG, "接口数据更新时间：" + heWeatherForecast.update.loc);
                    }
                }
            });
            // 刷新实况天气质量
            String weatherAirQualityUrl = "https://free-api.heweather.net/s6/air/now?location=" + weatherId +"&key=1f973beb7602432bb31cdceb9da27525";
            HttpUtil.sendHttpRequest(weatherAirQualityUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseStr = response.body().string();
                    final HeWeatherAirQuality heWeatherAirQuality = JsonParser.parseWeatherAirQuality(responseStr);
                    if (heWeatherAirQuality != null && BaseConfigUtil.API_STATUS_OK.equals(heWeatherAirQuality.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RegularRefreshService.this).edit();
                        editor.putString("weather_air_quality", responseStr);
                        editor.apply();
                        Log.i(TAG, "接口数据更新时间：" + heWeatherAirQuality.update.loc);
                    }
                }
            });
            // 刷新生活指数数据
            String lifeStyleUrl = "https://free-api.heweather.net/s6/weather/lifestyle?location=" +weatherId+ "&key=1f973beb7602432bb31cdceb9da27525";
            HttpUtil.sendHttpRequest(lifeStyleUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseStr = response.body().string();
                    final HeWeatherLifestyle heWeatherLifestyle = JsonParser.parseWeatherLifestyleResponse(responseStr);
                    if (heWeatherLifestyle != null && BaseConfigUtil.API_STATUS_OK.equals(heWeatherLifestyle.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RegularRefreshService.this).edit();
                        editor.putString("weather_lifestyle", responseStr);
                        editor.apply();
                        Log.i(TAG, "接口数据更新时间：" + heWeatherLifestyle.update.loc);
                    }
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
