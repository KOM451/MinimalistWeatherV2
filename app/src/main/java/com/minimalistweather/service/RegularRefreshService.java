package com.minimalistweather.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.preference.PreferenceManager;

/**
 * 定时刷新数据服务
 */
public class RegularRefreshService extends Service {
    public RegularRefreshService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        return super.onStartCommand(intent, flags, startId);
    }

    public void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
