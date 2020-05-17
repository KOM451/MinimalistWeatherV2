package com.minimalistweather.view.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minimalistweather.R;
import com.minimalistweather.entity.database_entity.City;
import com.minimalistweather.entity.database_entity.District;
import com.minimalistweather.entity.database_entity.ManagedCity;
import com.minimalistweather.entity.database_entity.Province;
import com.minimalistweather.util.HttpUtil;
import com.minimalistweather.util.JsonParser;
import com.minimalistweather.view.activity.AreaChooseActivity;
import com.minimalistweather.view.activity.MainActivity;
import com.minimalistweather.view.activity.WeatherActivity;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AreaChooseFragment extends Fragment {

    // 城市数据接口
    public static final String baseAreaUrl = "http://guolin.tech/api/china/";

    /**
     * 定义地区查询类型
     */
    public static final String TYPE_PROVINCE = "province";
    public static final String TYPE_CITY = "city";
    public static final String TYPE_DISTRICT = "district";

    /**
     * 定义地区选择等级
     */
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_DISTRICT = 2;

    /**
     * 声明布局中的各个组件
     */
    private Toolbar mToolbar;

    /**
     * RecyclerView相关
     */
    private RecyclerView mRecyclerView;
    private AreaAdapter mAreaAdapter;

    /**
     * 数据相关
     */
    private List<Province> mProvinces;
    private List<City> mCities;
    private List<District> mDistricts;
    private List<String> mAreaData = new ArrayList<>();

    private Province mSelectedProvince; // 选中的省级规划
    private City mSelectedCity; // 选中的市级规划
    private int mCurrentLevel; // 当前选中地区等级

    private ProgressDialog mProgressDialog; // 查询进度框

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);

        /*
         * 初始化布局中的各个组件
         */
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        /*
         * 初始化RecyclerView
         */
        mRecyclerView = (RecyclerView) view.findViewById(R.id.area_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAreaAdapter = new AreaAdapter(mAreaData);
        mRecyclerView.setAdapter(mAreaAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initToolBar();
        updateProvinces();
    }

    private void initToolBar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);
        if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // 设置导航按钮侦听器
        mToolbar.setNavigationOnClickListener(view -> {
            if(mCurrentLevel == LEVEL_PROVINCE) { // 当前选择等级为省
                if(getActivity() instanceof MainActivity) { // 宿主为MainActivity，通过R.id.coordinator_layout获取WeatherFragment，并关闭抽屉视图
                    WeatherFragment fragment = (WeatherFragment) getActivity()
                            .getSupportFragmentManager().findFragmentById(R.id.coordinator_layout);
                    fragment.drawerLayout.closeDrawers();
                } else if(getActivity() instanceof AreaChooseActivity) {  // 宿主为AreaActivity，销毁该Activity
                    getActivity().finish();
                } else if (getActivity() instanceof WeatherActivity) { // 宿主为WeatherActivity，通过R.id.fragment_container获取WeatherFragment，并关闭抽屉视图
                    WeatherFragment fragment = (WeatherFragment) getActivity()
                            .getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    fragment.drawerLayout.closeDrawers();
                }
            } else if(mCurrentLevel == LEVEL_CITY) { // 当前选择为市，查询更新省列表
                updateProvinces();
            } else if(mCurrentLevel == LEVEL_DISTRICT) { // 当前选择为地区，查询更新市列表
                updateCities();
            }
        });
    }

    /**
     * 更新省份数据
     */
    private void updateProvinces() {
        mToolbar.setTitle("中国");
        mProvinces = LitePal.findAll(Province.class); // 从数据库查询省份数据
        if(mProvinces.size() > 0) {
            // 数据库有数据，直接更新
            mAreaData.clear();
            for(Province province : mProvinces) {
                mAreaData.add(province.getProvinceName());
            }
            mAreaAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(0);
            mCurrentLevel = LEVEL_PROVINCE;
        } else {
            queryAreaFromServer(baseAreaUrl, TYPE_PROVINCE);
        }
    }

    /**
     * 更新市级数据
     */
    private void updateCities() {
        mToolbar.setTitle(mSelectedProvince.getProvinceName());
        mCities = LitePal.where("provinceid = ?", String.valueOf(mSelectedProvince.getId())).find(City.class);
        if(mCities.size() > 0) {
            mAreaData.clear();
            for(City city : mCities) {
                mAreaData.add(city.getCityName());
            }
            mAreaAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(0);
            mCurrentLevel = LEVEL_CITY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            String url = baseAreaUrl + provinceCode;
            queryAreaFromServer(url, TYPE_CITY);
        }
    }

    /**
     * 更新区县数据
     */
    private void updateDistrict() {
        mToolbar.setTitle(mSelectedCity.getCityName());
        mDistricts = LitePal.where("cityid = ?", String.valueOf(mSelectedCity.getId())).find(District.class);
        if(mDistricts.size() > 0) {
            mAreaData.clear();
            for(District district : mDistricts) {
                mAreaData.add(district.getDistrictName());
            }
            mAreaAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(0);
            mCurrentLevel = LEVEL_DISTRICT;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String url = baseAreaUrl + provinceCode + "/" + cityCode;
            queryAreaFromServer(url, TYPE_DISTRICT);
        }
    }

    /**
     * 从服务器上查询area数据
     * @param url 请求url
     * @param type 查询类型
     */
    private void queryAreaFromServer(String url, final String type) {
        showProgress();
        HttpUtil.sendHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgress();
                        Toast.makeText(getContext(), "load failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = response.body().string(); // 响应字符串
                boolean isParseSuccess = false; // 解析是否成功
                /*
                 * 根据type解析相应的数据
                 */
                if(TYPE_PROVINCE.equals(type)) {
                    isParseSuccess = JsonParser.parseProvinceResponse(responseStr);
                } else if(TYPE_CITY.equals(type)) {
                    isParseSuccess = JsonParser.parseCityResponse(responseStr, mSelectedProvince.getId());
                } else if(TYPE_DISTRICT.equals(type)) {
                    isParseSuccess = JsonParser.parseDistrictResponse(responseStr, mSelectedCity.getId());
                }
                if(isParseSuccess) {
                    // 如果解析成功，切换到主线程，更新数据
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgress();
                            switch (type) {
                                case TYPE_PROVINCE:
                                    updateProvinces();
                                    break;
                                case TYPE_CITY:
                                    updateCities();
                                    break;
                                case TYPE_DISTRICT:
                                    updateDistrict();
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示查询进度
     */
    private void showProgress() {
        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("loading...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    /**
     * 关闭进度框
     */
    private void closeProgress() {
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * RecyclerView相关
     */
    private class AreaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String mAreaItemName;

        private TextView mAreaItemText;

        public AreaViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_area, parent, false));

            mAreaItemText = (TextView) itemView.findViewById(R.id.area_item_text);
            itemView.setOnClickListener(this);
        }

        public void bind(String areaItemName) {
            mAreaItemName = areaItemName;
            mAreaItemText.setText(mAreaItemName);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if(mCurrentLevel == LEVEL_PROVINCE) { // 当前选中等级为省，查询更新市列表
                mSelectedProvince = mProvinces.get(position);
                updateCities();
            } else if(mCurrentLevel == LEVEL_CITY) { // 当前选中等级为市，查询更新区县列表
                mSelectedCity = mCities.get(position);
                updateDistrict();
            } else if(mCurrentLevel == LEVEL_DISTRICT) { // 当选中项为区，跳转到天气显示
                String weatherId = mDistricts.get(position).getWeatherId();
                if(getActivity() instanceof AreaChooseActivity) { // 宿主为AreaChooseActivity，启动MainActivity

                    ManagedCity city = new ManagedCity();
                    city.setCid(weatherId);
                    city.setCityName(mDistricts.get(position).getDistrictName());
                    city.save();

                    /*Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    getActivity().startActivity(intent);*/

                    getActivity().finish();
                } else if(getActivity() instanceof WeatherActivity) { // 宿主为WeatherActivity，关闭抽屉视图，请求并显示天气信息
                    WeatherFragment fragment = (WeatherFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    fragment.drawerLayout.closeDrawers();
                    fragment.refresh.setRefreshing(false);
                    fragment.currentWeatherId = weatherId;
                    fragment.requestWeatherNow(weatherId);
                    fragment.requestWeatherAirQuality(weatherId);
                    fragment.requestWeatherForecast(weatherId);
                    fragment.requestWeatherLifestyle(weatherId);
                } else if(getActivity() instanceof MainActivity) { // 宿主市MainActivity，关闭抽屉视图，请求并显示天气信息
                    WeatherFragment fragment = (WeatherFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.coordinator_layout);
                    fragment.drawerLayout.closeDrawers();
                    fragment.refresh.setRefreshing(false);
                    fragment.currentWeatherId = weatherId;
                    fragment.requestWeatherNow(weatherId);
                    fragment.requestWeatherAirQuality(weatherId);
                    fragment.requestWeatherForecast(weatherId);
                    fragment.requestWeatherLifestyle(weatherId);
                }

            }
        }
    }
    private class AreaAdapter extends RecyclerView.Adapter<AreaViewHolder> {

        private List<String> mAreas;

        public AreaAdapter(List<String> areas) {
            mAreas = areas;
        }

        @NonNull
        @Override
        public AreaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new AreaViewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull AreaViewHolder holder, int position) {
            String areaItemName = mAreas.get(position);
            holder.bind(areaItemName);
        }

        @Override
        public int getItemCount() {
            return mAreas.size();
        }
    }
}
