package com.minimalistweather.entity.gson_entity;

import com.google.gson.annotations.SerializedName;

/**
 * 空气质量（城区实况）
 */
public class HeWeatherAirQuality {

    public Basic basic;

    public Update update;

    public String status;

    @SerializedName("air_now_city")
    public AirNowCity airNowCity;
}
