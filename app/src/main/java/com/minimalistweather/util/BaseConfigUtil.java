package com.minimalistweather.util;

public class BaseConfigUtil {

    // 和风天气接口状态
    public static final String API_STATUS_OK = "ok";

    // 和风天气 WebAPI 用户 key
    public static final String API_KEY = "&key=1f973beb7602432bb31cdceb9da27525";

    // 区域编码，默认为北京市朝阳区
    public static String AD_CODE = "110105";

    // 地区ID，默认为北京市朝阳区
    public static String CID = "CN101010300";

    // 缓存数据 key
    public static final String PREFERENCE_WEATHER_NOW = "weather_now";
    public static final String PREFERENCE_WEATHER_FORECAST = "weather_forecast";
    public static final String PREFERENCE_WEATHER_AQI = "weather_air_quality";
    public static final String PREFERENCE_WEATHER_LIFESTYLE = "weather_lifestyle";

    // 请求天气数据的接口地址
    public static final String API_NOW = "https://free-api.heweather.net/s6/weather/now?location="; // 实况天气地址
    public static final String API_FORECAST = "https://free-api.heweather.net/s6/weather/forecast?location="; // 三日天气预报地址
    public static final String API_AQI = "https://free-api.heweather.net/s6/air/now?location="; // 实况空气质量地址
    public static final String API_LIFESTYLE = "https://free-api.heweather.net/s6/weather/lifestyle?location="; // 生活指数地址

    // 城市搜索接口地址
    public static final String API_CITY_SEARCH = "https://search.heweather.net/find?location=";
}
