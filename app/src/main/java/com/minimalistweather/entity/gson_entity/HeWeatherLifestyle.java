package com.minimalistweather.entity.gson_entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 生活指数（基本八项）
 */
public class HeWeatherLifestyle {

    public Basic basic;

    public Update update;

    public String status;

    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyles;
}
