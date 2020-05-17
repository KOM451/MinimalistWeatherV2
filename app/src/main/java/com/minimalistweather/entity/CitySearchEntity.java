package com.minimalistweather.entity;

public class CitySearchEntity {
    private String mCityName;
    private String mCityId;
    private String mCnty;
    private String mLocation;
    private String mParentCity;
    private String mAdminArea;
    private boolean mIsFavorite;

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }

    public String getCityId() {
        return mCityId;
    }

    public void setCityId(String cityId) {
        mCityId = cityId;
    }

    public String getCnty() {
        return mCnty;
    }

    public void setCnty(String cnty) {
        mCnty = cnty;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getParentCity() {
        return mParentCity;
    }

    public void setParentCity(String parentCity) {
        mParentCity = parentCity;
    }

    public String getAdminArea() {
        return mAdminArea;
    }

    public void setAdminArea(String adminArea) {
        mAdminArea = adminArea;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean favorite) {
        mIsFavorite = favorite;
    }
}
