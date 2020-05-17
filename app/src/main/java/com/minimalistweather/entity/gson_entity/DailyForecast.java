package com.minimalistweather.entity.gson_entity;

/**
 * 天气预报
 */
public class DailyForecast {

    public String date; // 预报日期

    public String sr; // 日出时间

    public String ss; // 日落时间

    public String mr; // 月升时间

    public String ms; // 月落时间

    public String tmp_max; // 最高温度

    public String tmp_min; // 最低温度

    public String cond_code_d; // 白天天气状况代码

    public String cond_code_n; // 夜间天气状况代码

    public String cond_txt_d; // 白天天气状况描述

    public String cond_txt_n; // 晚间天气状况描述

    public String wind_dir; // 风向

    public String wind_sc; // 风力

    public String hum; // 相对湿度

    public String uv_index; // 紫外线强度指数

    public String vis; // 能见度
}
