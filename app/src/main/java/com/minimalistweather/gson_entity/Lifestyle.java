package com.minimalistweather.gson_entity;

/**
 * 生活指数
 */
public class Lifestyle {

    public String brf; // 生活指数简介

    public String txt; // 生活指数详细描述

    /*
     * 基础八项：
     * comf：舒适度指数、drsg：穿衣指数、flu：感冒指数、sport：运动指数、
     * trav：旅游指数、uv：紫外线指数、cw：洗车指数、air：空气污染扩散条件指数
     */
    public String type; // 生活指数类型

}
