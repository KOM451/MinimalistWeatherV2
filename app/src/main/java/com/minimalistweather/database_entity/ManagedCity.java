package com.minimalistweather.database_entity;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * 被管理的城市
 */
public class ManagedCity extends LitePalSupport {

    private int id;

    @Column(unique = true, defaultValue = "Unknown")
    private String cid;

    private String cityName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
