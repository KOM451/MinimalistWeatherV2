package com.minimalistweather.adapter;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.minimalistweather.util.BaseConfigUtil;
import com.minimalistweather.view.activity.CitySearchActivity;
import com.minimalistweather.R;
import com.minimalistweather.entity.CitySearchEntity;
import com.minimalistweather.entity.CitySearchListEntity;
import com.minimalistweather.entity.database_entity.ManagedCity;
import com.minimalistweather.entity.gson_entity.Basic;
import com.minimalistweather.entity.gson_entity.Location;
import com.minimalistweather.util.HttpUtil;
import com.minimalistweather.util.JsonParser;
import com.minimalistweather.util.SpUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CitySearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CitySearchEntity> mData;

    private CitySearchActivity mCitySearchActivity;

    private String mSearchText;

    private CitySearchListEntity mCitySearchListEntity = new CitySearchListEntity();

    private boolean mIsSearching;

    public CitySearchAdapter(List<CitySearchEntity> data, CitySearchActivity citySearchActivity, String searchText, boolean isSearching) {
        mData = data;
        mCitySearchActivity = citySearchActivity;
        mSearchText = searchText;
        mIsSearching = isSearching;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(mIsSearching) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_search, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_search_history, parent, false);
        }
        return new CitySearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CitySearchViewHolder citySearchViewHolder = (CitySearchViewHolder) holder;
        View itemView = citySearchViewHolder.itemView;
        String name = mData.get(position).getCityName();
        int index = name.indexOf("-");
        String parentCity = name.substring(0, index);
        String location = name.substring(index + 1);
        // 对搜索结果做格式化处理
        String cityName = location + ", " + parentCity + ", " + mData.get(position).getAdminArea()
                + ", " + mData.get(position).getCnty();
        if(TextUtils.isEmpty(mData.get(position).getAdminArea())) {
            cityName = location + ", " + parentCity + ", " + mData.get(position).getCnty();
        }
        if(!TextUtils.isEmpty(cityName)) {
            citySearchViewHolder.mHistoryCity.setText(cityName);
            if(cityName.contains(mSearchText)) { // 对命中的文本做处理
                int i = cityName.indexOf(mSearchText); // 获取与搜索内容匹配的文本的起始位置
                SpannableString spannableString = new SpannableString(cityName);
                // 将搜索结果中与搜索内容相匹配的字符串高亮显示
                spannableString.setSpan(new ForegroundColorSpan(mCitySearchActivity.getResources()
                        .getColor(R.color.light_text_color)), i, i + mSearchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                citySearchViewHolder.mHistoryCity.setText(spannableString);
            }
        }

        itemView.setOnClickListener(view -> {
            final String cid = mData.get(position).getCityId();
            saveData("cityBeanCN", cid);
            saveEntity("cityBean", cid, position);

            ManagedCity city = new ManagedCity();
            city.setCid(cid);
            city.setCityName(location);
            city.save();

            mCitySearchActivity.onBackPressed();
        });
    }

    private void saveEntity(final String key, String cid, int index) {
        List<CitySearchEntity> citySearchEntities = new ArrayList<>();
        mCitySearchListEntity = SpUtils.getBean(mCitySearchActivity, key, CitySearchListEntity.class);
        if(mCitySearchListEntity != null && mCitySearchListEntity.getCitySearchEntities() != null) {
            citySearchEntities = mCitySearchListEntity.getCitySearchEntities();
        }
        for(int i = 0; i < citySearchEntities.size(); i++) {
            if(citySearchEntities.get(i).getCityId().equals(cid)) {
                citySearchEntities.remove(i);
            }
        }
        if(citySearchEntities.size() == 10) {
            citySearchEntities.remove(9);
        }
        citySearchEntities.add(0, mData.get(index));
        CitySearchListEntity citySearchListEntity = new CitySearchListEntity();
        citySearchListEntity.setCitySearchEntities(citySearchEntities);
        SpUtils.saveBean(mCitySearchActivity, key, citySearchListEntity);
    }

    private void saveData(final String key, final String cid) {
        String requestUrl = "https://search.heweather.net/find?location=" + cid + "&group=cn&number=1&key=1f973beb7602432bb31cdceb9da27525";
        HttpUtil.sendHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i("CitySearchAdapter", "请求失败");
                mCitySearchActivity.onBackPressed();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = response.body().string();
                Location search = JsonParser.parseLocation(responseStr);
                List<CitySearchEntity> citySearchEntities = new ArrayList<>();
                if(search != null && BaseConfigUtil.API_STATUS_OK.equals(search.status)) {
                    List<Basic> basic = search.basic;
                    Basic basicData = basic.get(0);
                    String parentCity = basicData.parent_city;
                    String adminArea = basicData.admin_area;
                    String cnty = basicData.cnty;
                    if (TextUtils.isEmpty(parentCity)) {
                        parentCity = adminArea;
                    }
                    if (TextUtils.isEmpty(adminArea)) {
                        parentCity = cnty;
                    }
                    CitySearchEntity citySearchEntity = new CitySearchEntity();
                    citySearchEntity.setCityName(parentCity + "-" + basicData.location);
                    citySearchEntity.setCityId(basicData.cid);
                    citySearchEntity.setCnty(cnty);
                    citySearchEntity.setAdminArea(adminArea);

                    mCitySearchListEntity = SpUtils.getBean(mCitySearchActivity, key, CitySearchListEntity.class);
                    if(mCitySearchListEntity != null && mCitySearchListEntity.getCitySearchEntities() != null) {
                        citySearchEntities =  mCitySearchListEntity.getCitySearchEntities();
                    }
                    for(int i = 0; i < citySearchEntities.size(); i++) {
                        if(citySearchEntities.get(i).getCityId().equals(cid)) {
                            citySearchEntities.remove(i);
                        }
                    }
                    if(citySearchEntities.size() == 10) {
                        citySearchEntities.remove(9);
                    }
                    citySearchEntities.add(0, citySearchEntity);
                    CitySearchListEntity citySearchListEntity = new CitySearchListEntity();
                    citySearchListEntity.setCitySearchEntities(citySearchEntities);
                    SpUtils.saveBean(mCitySearchActivity, key, citySearchListEntity);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class CitySearchViewHolder extends RecyclerView.ViewHolder {

        private final TextView mHistoryCity;

        public CitySearchViewHolder(@NonNull View itemView) {
            super(itemView);
            mHistoryCity = itemView.findViewById(R.id.item_city_history);
        }
    }
}
