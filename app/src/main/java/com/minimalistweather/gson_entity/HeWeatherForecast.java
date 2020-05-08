package com.minimalistweather.gson_entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 3日天气预报
 */
public class HeWeatherForecast {

    public Basic basic;

    public Update update;

    public String status;

    @SerializedName("daily_forecast")
    public List<DailyForecast> dailyForecasts;
}
