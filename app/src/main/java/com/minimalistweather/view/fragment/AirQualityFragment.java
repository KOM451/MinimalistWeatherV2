package com.minimalistweather.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.minimalistweather.R;
import com.minimalistweather.entity.gson_entity.HeWeatherAirQuality;
import com.minimalistweather.util.JsonParser;

public class AirQualityFragment extends Fragment {

    private Button mBackWeatherButton; // 返回天气信息界面

    private TextView mAirQualityArea; // 地区名

    private TextView mAirQualityAqi; // aqi指数

    private TextView mAirQualityQlty; // 空气质量

    private TextView mAirQualitySo2; // SO2

    private TextView mAirQualityO3; // O3

    private TextView mAirQualityCo; // CO

    private TextView mAirQualityNo2; // NO2

    private TextView mAirQualityPm10; // PM10

    private TextView mAirQualityPm25; // PM25

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_quality, container, false);

        mBackWeatherButton = (Button) view.findViewById(R.id.back_weather_button);
        mAirQualityArea = (TextView) view.findViewById(R.id.air_quality_area);
        mAirQualityAqi = (TextView) view.findViewById(R.id.air_quality_aqi);
        mAirQualityQlty = (TextView) view.findViewById(R.id.air_quality_qlty);
        mAirQualitySo2 = (TextView) view.findViewById(R.id.air_quality_so2);
        mAirQualityO3 = (TextView) view.findViewById(R.id.air_quality_o3);
        mAirQualityCo = (TextView) view.findViewById(R.id.air_quality_co);
        mAirQualityNo2 = (TextView) view.findViewById(R.id.air_quality_no2);
        mAirQualityPm10 = (TextView) view.findViewById(R.id.air_quality_pm10);
        mAirQualityPm25 = (TextView) view.findViewById(R.id.air_quality_pm25);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String weatherAirQualityStr = preferences.getString("weather_air_quality", null);
        HeWeatherAirQuality weatherAirQuality = JsonParser.parseWeatherAirQuality(weatherAirQualityStr);
        String airQualityArea = weatherAirQuality.basic.location + "空气质量";
        String airQualityAqi = weatherAirQuality.airNowCity.aqi;
        String airQualityQlty = weatherAirQuality.airNowCity.qlty;
        String airQualitySo2 = weatherAirQuality.airNowCity.so2;
        String airQualityO3 = weatherAirQuality.airNowCity.o3;
        String airQualityCo = weatherAirQuality.airNowCity.co;
        String airQualityNo2 = weatherAirQuality.airNowCity.no2;
        String airQualityPm10 = weatherAirQuality.airNowCity.pm10;
        String airQualityPm25 = weatherAirQuality.airNowCity.pm25;
        mAirQualityArea.setText(airQualityArea);
        mAirQualityAqi.setText(airQualityAqi);
        mAirQualityQlty.setText(airQualityQlty);
        mAirQualitySo2.setText(airQualitySo2);
        mAirQualityO3.setText(airQualityO3);
        mAirQualityCo.setText(airQualityCo);
        mAirQualityNo2.setText(airQualityNo2);
        mAirQualityPm10.setText(airQualityPm10);
        mAirQualityPm25.setText(airQualityPm25);

        mBackWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherFragment fragment = (WeatherFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                fragment.drawerLayout.closeDrawers();
            }
        });
    }
}
