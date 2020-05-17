package com.minimalistweather.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.minimalistweather.R;
import com.minimalistweather.adapter.CitySearchAdapter;
import com.minimalistweather.entity.CitySearchEntity;
import com.minimalistweather.entity.gson_entity.Basic;
import com.minimalistweather.entity.gson_entity.Location;
import com.minimalistweather.util.HttpUtil;
import com.minimalistweather.util.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CitySearchActivity extends AppCompatActivity implements View.OnClickListener {

    private AutoCompleteTextView mActSearch;

    private LinearLayout mSearchHistoryLayout;

    private RecyclerView mSearchRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);
        initView();
        initCitySearch();
    }

    private void initView() {
        ImageView searchBack = findViewById(R.id.search_back);
        mActSearch = findViewById(R.id.act_search);
        mSearchHistoryLayout = findViewById(R.id.search_history_layout);
        mSearchRecyclerView = findViewById(R.id.search_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mSearchRecyclerView.setLayoutManager(linearLayoutManager);
        searchBack.setOnClickListener(this);
    }

    private void initCitySearch() {
        mActSearch.setThreshold(1);
        mActSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = mActSearch.getText().toString();
                if(!TextUtils.isEmpty(searchText)) {
                    mSearchHistoryLayout.setVisibility(View.GONE);
                    mSearchRecyclerView.setVisibility(View.VISIBLE);
                    searchCityFromServer(searchText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchCityFromServer(String location) {
        String requestUrl = "https://search.heweather.net/find?location=" + location + "&mode=match&group=cn&number=10&key=1f973beb7602432bb31cdceb9da27525";
        HttpUtil.sendHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Location locations = new Location();
                locations.status = "noData";
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = response.body().string();
                Location locations = JsonParser.parseLocation(responseStr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(locations != null && locations.status.equals("ok")) {
                            final List<Basic> basic = locations.basic;
                            List<CitySearchEntity> data = new ArrayList<>();
                            if(basic != null && basic.size() > 0) {
                                if(data.size() > 0) {
                                    data.clear();
                                }
                                for(int i = 0; i < basic.size(); i++) {
                                    Basic basicData = basic.get(i);
                                    String parentCity = basicData.parent_city;
                                    String adminArea = basicData.admin_area;
                                    String cnty = basicData.cnty;
                                    if(TextUtils.isEmpty(parentCity)) {
                                        parentCity = adminArea;
                                    }
                                    if(TextUtils.isEmpty(adminArea)) {
                                        parentCity = cnty;
                                    }
                                    CitySearchEntity citySearchEntity = new CitySearchEntity();
                                    citySearchEntity.setCityName(parentCity + "-" + basicData.location);
                                    citySearchEntity.setCityId(basicData.cid);
                                    citySearchEntity.setCnty(cnty);
                                    citySearchEntity.setAdminArea(adminArea);
                                    data.add(citySearchEntity);
                                }
                                CitySearchAdapter citySearchAdapter = new CitySearchAdapter(data, CitySearchActivity.this, mActSearch.getText().toString(), true);
                                mSearchRecyclerView.setAdapter(citySearchAdapter);
                                citySearchAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_back:
                onBackPressed();
                break;
            default:
                break;
        }

    }
}
