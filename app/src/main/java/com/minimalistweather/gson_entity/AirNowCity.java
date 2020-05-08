package com.minimalistweather.gson_entity;

/**
 * 空气质量实况（城区）
 */
public class AirNowCity {

    public String pub_time; // 数据发布时间

    public String aqi; // 空气质量指数

    public String main; // 主要污染物

    public String qlty; // 空气质量，取值范围:优，良，轻度污染，中度污染，重度污染，严重污染

    public String so2; // 二氧化硫

    public String o3; // 臭氧

    public String co; // 一氧化碳

    public String no2; // 二氧化氮

    public String pm10; // pm10

    public String pm25; // PM2.5指数
}
