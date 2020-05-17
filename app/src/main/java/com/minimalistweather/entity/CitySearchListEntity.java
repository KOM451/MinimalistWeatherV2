package com.minimalistweather.entity;

import java.util.List;

public class CitySearchListEntity {

    private List<CitySearchEntity> mCitySearchEntities;

    public List<CitySearchEntity> getCitySearchEntities() {
        return mCitySearchEntities;
    }

    public void setCitySearchEntities(List<CitySearchEntity> citySearchEntities) {
        mCitySearchEntities = citySearchEntities;
    }
}
