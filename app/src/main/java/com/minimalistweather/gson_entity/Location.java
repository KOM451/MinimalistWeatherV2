package com.minimalistweather.gson_entity;

import java.util.List;

/**
 * 和风天气城市搜索接口对应实体类
 */
public class Location {

    public String status;

    public List<Basic> basic;

    public static class Basic {

        public String cid;

        public String location;
    }
}
