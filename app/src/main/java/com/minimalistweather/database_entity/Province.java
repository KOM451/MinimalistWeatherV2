package com.minimalistweather.database_entity;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {

    private int id;

    private int provinceCode; // 省编码

    private String provinceName; // 省的名称

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}
