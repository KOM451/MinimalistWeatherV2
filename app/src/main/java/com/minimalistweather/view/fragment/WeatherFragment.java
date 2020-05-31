package com.minimalistweather.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.minimalistweather.R;
import com.minimalistweather.entity.gson_entity.DailyForecast;
import com.minimalistweather.entity.gson_entity.HeWeatherAirQuality;
import com.minimalistweather.entity.gson_entity.HeWeatherForecast;
import com.minimalistweather.entity.gson_entity.HeWeatherLifestyle;
import com.minimalistweather.entity.gson_entity.HeWeatherNow;
import com.minimalistweather.entity.gson_entity.Lifestyle;
import com.minimalistweather.util.BaseConfigUtil;
import com.minimalistweather.util.HttpUtil;
import com.minimalistweather.util.JsonParser;
import com.minimalistweather.view.activity.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherFragment extends Fragment {

    private static final String TAG = "WeatherFragment";


    public static final String ACTION_UPDATE = "action.update";

    public LocationChangeReceiver mLocationChangeReceiver;

    public static final String ACTION_REFRESH = "action.refresh";

    public AutoRefreshReceiver mAutoRefreshReceiver;

    public DrawerLayout drawerLayout; // 用于实现滑动菜单逻辑

    public String currentWeatherId; // 当前城市cid

    public SwipeRefreshLayout refresh; // 用于实现下拉刷新逻辑

    private ScrollView mWeatherLayout; // 天气信息布局

    private ImageView mCondIcon; // 实况天气图标

    private TextView mNowTmpDegree; // 实况温度

    private TextView mNowCond; // 实况天气状况

    private TextView mNowAirQlty; // 实况空气质量

    private TextView mNowAirAqi;  // 实况AQI指数

    private TextView mNowAirPm25; // 实况PM2.5

    private TextView mWindSc; // 风力

    private TextView mWindDir; // 风向

    private TextView mHum; // 空气湿度

    private TextView mFl; // 体感温度

    private TextView mPres; // 大气压强

    private LinearLayout mWeatherForecastLayout; // 天气预报布局

    private LinearLayout mWeatherLifestyleLayout; // 生活指数布局

    private Toolbar mToolbar; // 用于得到MainActivity（宿主）的ToolBar

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity) { // 得到MainActivity（宿主）中的ToolBar
            MainActivity activity = (MainActivity) context;
            mToolbar = (Toolbar) activity.findViewById(R.id.toolbar);
            mToolbar.getMenu().clear();
            mToolbar.inflateMenu(R.menu.toolbar_menu);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        /*
         * 初始化控件
         */
        mWeatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
        mCondIcon = (ImageView) view.findViewById(R.id.cond_icon);
        mNowTmpDegree = (TextView) view.findViewById(R.id.now_tmp_degree);
        mNowCond = (TextView) view.findViewById(R.id.now_cond);
        mNowAirQlty = (TextView) view.findViewById(R.id.now_air_qlty);
        mNowAirAqi = (TextView) view.findViewById(R.id.now_air_aqi);
        mNowAirPm25 = (TextView) view.findViewById(R.id.now_air_pm25);
        mWindSc = (TextView) view.findViewById(R.id.weather_wind_sc);
        mWindDir = (TextView) view.findViewById(R.id.weather_wind_dir);
        mHum = (TextView) view.findViewById(R.id.weather_hum);
        mFl = (TextView) view.findViewById(R.id.weather_fl);
        mPres = (TextView) view.findViewById(R.id.weather_pres);
        mWeatherForecastLayout = (LinearLayout) view.findViewById(R.id.weather_forecast_layout);
        mWeatherLifestyleLayout = (LinearLayout) view.findViewById(R.id.weather_lifestyle_layout);
        refresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        refresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        currentWeatherId = getArguments().getString("weather_id", null);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 动态注册广播接收器，用于接收定位服务推送的数据
        IntentFilter filterLocation = new IntentFilter();
        filterLocation.addAction(ACTION_UPDATE);
        mLocationChangeReceiver = new LocationChangeReceiver();
        getActivity().registerReceiver(mLocationChangeReceiver, filterLocation);

        // 动态注册广播接收器，用于接收定时刷新服务推送的数据
        IntentFilter filterRefresh = new IntentFilter();
        filterRefresh.addAction(ACTION_REFRESH);
        mAutoRefreshReceiver = new AutoRefreshReceiver();
        getActivity().registerReceiver(mAutoRefreshReceiver, filterRefresh);

        if(currentWeatherId != null) {
            requestWeatherNow(currentWeatherId);
            requestWeatherAirQuality(currentWeatherId);
            requestWeatherForecast(currentWeatherId);
            requestWeatherLifestyle(currentWeatherId);
        }

        /*
         * 下拉刷新逻辑
         */
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(currentWeatherId != null) {
                    requestWeatherNow(currentWeatherId); // 请求实况天气数据
                    requestWeatherAirQuality(currentWeatherId); // 请求空气质量数据
                    requestWeatherForecast(currentWeatherId); // 请求天气预报数据
                    requestWeatherLifestyle(currentWeatherId); // 请求生活指数数据
                    // 在最后执行的请求结束时，隐藏刷新进度
                }

            }
        });

        /*
         * 为Toolbar上的菜单设置点击事件
         */
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.change_city: // 切换城市
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.choose_area_fragment, new AreaChooseFragment());
                        transaction.commit();
                        drawerLayout.openDrawer(GravityCompat.END); // 打开滑动菜单
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String weatherId = getActivity().getIntent().getStringExtra("weather_id");
        if(weatherId != null) {
            requestWeatherNow(weatherId);
            requestWeatherAirQuality(weatherId);
            requestWeatherForecast(weatherId);
            requestWeatherLifestyle(weatherId);
            currentWeatherId = weatherId;
        }
    }

    ////////////////////////////////////////////
    // 以下方法用于向服务器请求某一项具体的天气情况//
    ////////////////////////////////////////////

    /***
     * 获取生活指数数据
     * @param weatherId
     */
    public void requestWeatherLifestyle(final String weatherId) {
        String url = "https://free-api.heweather.net/s6/weather/lifestyle?location=" +weatherId+ "&key=" + BaseConfigUtil.API_KEY;
        HttpUtil.sendHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取生活指数失败", Toast.LENGTH_SHORT).show();
                        refresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseStr = response.body().string();
                final HeWeatherLifestyle weatherLifestyle = JsonParser.parseWeatherLifestyleResponse(responseStr);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherLifestyle != null && BaseConfigUtil.API_STATUS_OK.equals(weatherLifestyle.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherFragment.this.getActivity()).edit();
                            editor.putString("weather_lifestyle", responseStr);
                            editor.apply(); // 更新缓存
                            showWeatherLifestyleInformation(weatherLifestyle); // 更新生活指数数据
                        } else {
                            Toast.makeText(getContext(), "获取生活指数失败", Toast.LENGTH_SHORT).show();
                        }
                        refresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /***
     * 请求实况空气质量数据
     */
    public void requestWeatherAirQuality(String weatherId) {
        String url = "https://free-api.heweather.net/s6/air/now?location=" + weatherId +"&key=" + BaseConfigUtil.API_KEY;
        HttpUtil.sendHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取实况空气质量数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseStr = response.body().string();
                final HeWeatherAirQuality weatherAirQuality = JsonParser.parseWeatherAirQuality(responseStr);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherAirQuality != null && BaseConfigUtil.API_STATUS_OK.equals(weatherAirQuality.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherFragment.this.getActivity()).edit();
                            editor.putString("weather_air_quality", responseStr);
                            editor.apply(); // 更新缓存
                            showWeatherAirQualityInformation(weatherAirQuality); // 更新实况空气数据
                        } else {
                            Toast.makeText(getContext(), "实况空气质量数据获取失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    /***
     * 请求三日天气预报
     * @param weatherId
     */
    public void requestWeatherForecast(final String weatherId) {
        String url = "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId + "&key=" + BaseConfigUtil.API_KEY;
        HttpUtil.sendHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取天气预报数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseStr = response.body().string();
                final HeWeatherForecast weatherForecast = JsonParser.parseWeatherForecastResponse(responseStr);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherForecast != null && BaseConfigUtil.API_STATUS_OK.equals(weatherForecast.status)) {
                            // 接口状态正常，更新数据
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherFragment.this.getActivity()).edit();
                            editor.putString("weather_forecast", responseStr);
                            editor.apply(); // 更新缓存
                            showWeatherForecastInformation(weatherForecast); // 更新天气预报信息
                        } else {
                            Toast.makeText(getContext(), "天气预报数据获取失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 请求实况天气信息
     * @param weatherId
     */
    public void requestWeatherNow(final String weatherId) {
        String url = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=" + BaseConfigUtil.API_KEY;
        HttpUtil.sendHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取实况天气失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseStr = response.body().string(); // 响应字符串
                final HeWeatherNow weatherNow = JsonParser.parseWeatherNowResponse(responseStr);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherNow != null && BaseConfigUtil.API_STATUS_OK.equals(weatherNow.status)) {
                            // 接口状态正常，更新数据
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherFragment.this.getActivity()).edit();
                            editor.putString("weather_now", responseStr);
                            editor.apply(); // 更新缓存
                            showWeatherNowInformation(weatherNow); // 更新实况天气信息
                        } else {
                            Toast.makeText(getContext(), "获取实况天气失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /////////////////////////////////////
    // 以下方法用于显示某一项具体的天气信息//
    ////////////////////////////////////

    /**
     * 显示生活指数信息
     * @param weatherLifestyle
     */
    private void showWeatherLifestyleInformation(HeWeatherLifestyle weatherLifestyle) {

        /*
         * 定义基本八项生活指数类型
         */
        Map<String, String> typeMap = new HashMap<>(8);
        typeMap.put("comf", "舒适度指数");
        typeMap.put("drsg", "穿衣指数");
        typeMap.put("flu", "感冒指数");
        typeMap.put("sport", "运动指数");
        typeMap.put("trav", "旅游指数");
        typeMap.put("uv", "紫外线指数");
        typeMap.put("cw", "洗车指数");
        typeMap.put("air", "空气污染扩散条件指数");

        mWeatherLifestyleLayout.removeAllViews();
        for(Lifestyle lifestyle : weatherLifestyle.lifestyles) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_lifestyle_weather, mWeatherLifestyleLayout, false);

            TextView weatherLifestyleType = view.findViewById(R.id.weather_lifestyle_type);
            TextView weatherLifestyleBrf = view.findViewById(R.id.weather_lifestyle_brf);
            TextView weatherLifestyleTxt = view.findViewById(R.id.weather_lifestyle_txt);
            Button weatherLifestyleIcon = view.findViewById(R.id.weather_lifestyle_icon);

            weatherLifestyleType.setText(typeMap.get(lifestyle.type));
            weatherLifestyleBrf.setText(lifestyle.brf);
            weatherLifestyleTxt.setText(lifestyle.txt);

            /*
             * 动态获取生活指数图标
             */
            String iconName = lifestyle.type;
            int iconId = getResources().getIdentifier(iconName, "drawable", "com.minimalistweather");
            weatherLifestyleIcon.setBackgroundResource(iconId);

            mWeatherLifestyleLayout.addView(view);
        }
    }

    /**
     * 显示实况空气质量数据
     * @param weatherAirQuality
     */
    private void showWeatherAirQualityInformation(HeWeatherAirQuality weatherAirQuality) {
        mNowAirQlty.setText(weatherAirQuality.airNowCity.qlty);
        mNowAirAqi.setText(weatherAirQuality.airNowCity.aqi);
        mNowAirPm25.setText(weatherAirQuality.airNowCity.pm25);
    }

    /**
     * 显示3日天气预报
     * @param weatherForecast
     */
    private void showWeatherForecastInformation(HeWeatherForecast weatherForecast) {
        mWeatherForecastLayout.removeAllViews();
        for(DailyForecast forecast : weatherForecast.dailyForecasts) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_forecast_weather, mWeatherForecastLayout, false);

            TextView weatherForecastDate = (TextView) view.findViewById(R.id.weather_forecast_date);
            TextView weatherForecastCondD = (TextView) view.findViewById(R.id.weather_forecast_cond_d);
            TextView weatherForecastTmpMin = (TextView) view.findViewById(R.id.weather_forecast_tmp_min);
            TextView weatherForecastTmpMax = (TextView) view.findViewById(R.id.weather_forecast_tmp_max);
            ImageView weatherForecastCondIcon = (ImageView) view.findViewById(R.id.weather_forecast_cond_icon);

            weatherForecastDate.setText(forecast.date);
            weatherForecastCondD.setText(forecast.cond_txt_d);
            weatherForecastTmpMin.setText(forecast.tmp_min + "℃");
            weatherForecastTmpMax.setText(forecast.tmp_max + "℃");

            /*
             * 动态获取天气图标
             */
            //String iconName = "he" + forecast.cond_code_d;
            String iconName = "icon_" + forecast.cond_code_d + "d"; // 白天图标
            int iconId = getResources().getIdentifier(iconName, "drawable", "com.minimalistweather");
            weatherForecastCondIcon.setImageResource(iconId);

            mWeatherForecastLayout.addView(view);
        }
        mWeatherForecastLayout.setVisibility(View.VISIBLE);
    }

    /***
     * 显示实况天气信息
     * @param weatherNow
     */
    private void showWeatherNowInformation(HeWeatherNow weatherNow) {
        String districtName = weatherNow.basic.location; // 获取地区名称
        String nowTmpDegree = weatherNow.now.tmp + "℃"; // 获取实况温度
        String nowCond = weatherNow.now.cond_txt; // 获取实况天气状况

        String windSc = weatherNow.now.wind_sc + "级"; // 获取风力
        String windDir = weatherNow.now.wind_dir; // 获取风向
        String hum = weatherNow.now.hum + "%"; // 获取空气湿度
        String fl = weatherNow.now.fl + "℃"; // 获取体感温度
        String pref = weatherNow.now.pres + "hPa"; // 获取大气压强

        mNowTmpDegree.setText(nowTmpDegree);
        mNowCond.setText(nowCond);

        mWindSc.setText(windSc);
        mWindDir.setText(windDir);
        mHum.setText(hum);
        mFl.setText(fl);
        mPres.setText(pref);

        mToolbar.setTitle(districtName);

        /*
         * 动态获取实况天气图标
         */
        //String iconName = "he" + weatherNow.now.cond_code;
        String iconName = "icon_" + weatherNow.now.cond_code + "d"; // 白天图标
        int iconCode = getResources().getIdentifier(iconName, "drawable", "com.minimalistweather");
        mCondIcon.setImageResource(iconCode);

        mWeatherLayout.setVisibility(View.VISIBLE);
    }

    // 接收定位服务传输的数据
    private class LocationChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "当前的weatherId = " + intent.getStringExtra("weather_id"));
            String weatherId = intent.getStringExtra("weather_id");
            if(weatherId != null) {
                requestWeatherNow(weatherId);
                requestWeatherAirQuality(weatherId);
                requestWeatherForecast(weatherId);
                requestWeatherLifestyle(weatherId);
                currentWeatherId = weatherId;
            }
        }
    }

    // 接收定时刷新服务传输额数据
    private class AutoRefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "当前刷新的weatherId = " + intent.getStringExtra("weather_id"));
            String weatherId = intent.getStringExtra("weather_id");
            if(weatherId != null) {
                requestWeatherNow(weatherId);
                requestWeatherAirQuality(weatherId);
                requestWeatherForecast(weatherId);
                requestWeatherLifestyle(weatherId);
                currentWeatherId = weatherId;
            }
        }
    }
}
