package com.minimalistweather.database_entity;

import org.litepal.crud.LitePalSupport;

public class City extends LitePalSupport {

    private int id;

    private int cityCode; // 城市编码

    private String cityName; // 城市名称

    private int provinceId; // 所属省的id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
