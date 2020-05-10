package com.minimalistweather.gson_entity;

import java.util.List;

public class Location {

    public String status;

    public List<Basic> basic;

    public static class Basic {

        public String cid;

        public String location;
    }
}
