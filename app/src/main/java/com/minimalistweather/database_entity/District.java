package com.minimalistweather.database_entity;

import org.litepal.crud.LitePalSupport;

public class District extends LitePalSupport {

    private int id;

    private int districtCode; // 地区编码

    private String districtName; // 地区名称

    private String weatherId; // 和风天气API城市ID（cid）

    private int cityId; // 所属市id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(int districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
