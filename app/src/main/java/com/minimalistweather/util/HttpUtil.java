package com.minimalistweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void sendHttpRequest(String url, okhttp3.Callback callback) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        httpClient.newCall(request).enqueue(callback);
    }
}
