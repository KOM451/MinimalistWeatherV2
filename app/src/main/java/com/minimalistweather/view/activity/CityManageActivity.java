package com.minimalistweather.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.minimalistweather.R;
import com.minimalistweather.entity.database_entity.ManagedCity;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class CityManageActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mCityManageLayout;

    private ManagedCityAdapter mManagedCityAdapter;

    private List<ManagedCity> mCities = new ArrayList<>();

    private Button mAddCityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manage);
        initToolbar();

        mCityManageLayout = (RecyclerView) findViewById(R.id.city_manage_layout);
        mCityManageLayout.setLayoutManager(new LinearLayoutManager(CityManageActivity.this));


        mAddCityButton = (Button) findViewById(R.id.add_city);
        mAddCityButton.setOnClickListener(view -> {
            // 添加城市
            Intent intent = new Intent(CityManageActivity.this, CitySearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCities = LitePal.findAll(ManagedCity.class);
        mManagedCityAdapter = new ManagedCityAdapter(mCities);
        mCityManageLayout.setAdapter(mManagedCityAdapter);
        mManagedCityAdapter.notifyDataSetChanged();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.city_manage_toolbar);
        mToolbar.setTitle("城市管理");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        mToolbar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    public class ManagedCityAdapter extends RecyclerView.Adapter<ManagedCityHolder> {

        private List<ManagedCity> mCities;

        ManagedCityAdapter(List<ManagedCity> cities) {
            mCities = cities;
        }

        @NonNull
        @Override
        public ManagedCityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(CityManageActivity.this);
            return new ManagedCityHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ManagedCityHolder holder, int position) {
            holder.bind(mCities.get(position));
        }

        @Override
        public int getItemCount() {
            return mCities.size();
        }
    }

    public class ManagedCityHolder extends RecyclerView.ViewHolder {

        private ManagedCity mCity;

        private TextView mCityNameText;

        ManagedCityHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_city_manage, parent, false));
            mCityNameText = (TextView) itemView.findViewById(R.id.manage_city_name);
            Button deleteCityButton = (Button) itemView.findViewById(R.id.delete_city);
            deleteCityButton.setOnClickListener(view -> {
                // 删除城市
                mCities.remove(getAdapterPosition());
                mManagedCityAdapter.notifyDataSetChanged();
                LitePal.delete(ManagedCity.class, mCity.getId());
            });
            itemView.setOnClickListener(view -> {
                // 点击跳转到天气显示
                Intent intent = new Intent(CityManageActivity.this, MainActivity.class);
                intent.putExtra("weather_id", mCity.getCid());
                startActivity(intent);
                finish();
            });
        }

        void bind(ManagedCity city) {
            mCity = city;
            mCityNameText.setText(mCity.getCityName());
        }
    }
}
