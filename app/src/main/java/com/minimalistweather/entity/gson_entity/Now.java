package com.minimalistweather.entity.gson_entity;

/**
 * 实况天气
 */
public class Now {

    public String fl; // 体感温度，默认单位：摄氏度

    public String tmp; // 温度，默认单位：摄氏度

    public String cond_code; // 实况天气状态代码

    public String cond_txt; // 实况天气状况描述，如：晴

    public String wind_dir; // 风向

    public String wind_sc; // 风力

    public String hum; // 相对湿度

    public String vis; // 能见度

    public String pres; // 大气压强
}
