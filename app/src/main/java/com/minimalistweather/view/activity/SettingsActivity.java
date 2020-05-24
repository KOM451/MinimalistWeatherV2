package com.minimalistweather.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.minimalistweather.R;
import com.minimalistweather.service.AMapLocationService;
import com.minimalistweather.service.RegularRefreshService;

import org.jetbrains.annotations.NotNull;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Toolbar mToolbar;
    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getDelegate();
        initView();
        initToolbar();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    @NotNull
    public AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    private void initView() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        mDelegate.getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDelegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void initToolbar() {
        mToolbar.setTitle("设置");
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        mDelegate.setSupportActionBar(toolbar);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Preference mCheckUpdate;
        private SwitchPreference mSwitchLocation;
        private SwitchPreference mSwitchRegularRefresh;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            initLocationService();
            initRegularRefreshService();
            initCheckUpdate();
        }

        private void initCheckUpdate() {
            mCheckUpdate = findPreference("update_version");
            assert mCheckUpdate != null;
            mCheckUpdate.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getActivity(), "当前已经市最新版本", Toast.LENGTH_SHORT).show();
                return false;
            });
        }

        private void initLocationService() {
            mSwitchLocation = findPreference("switch_location");
            mSwitchLocation.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean checkStatus = (boolean)newValue;
                mSwitchLocation.setChecked(checkStatus);
                Context context = getActivity();
                Intent intentLocationService = new Intent(context, AMapLocationService.class);
                if (checkStatus) {
                    // 开启定位服务
                    context.startService(intentLocationService);
                } else {
                    // 关闭定位服务
                    context.stopService(intentLocationService);
                }
                return false;
            });
        }

        private void initRegularRefreshService() {
            mSwitchRegularRefresh = findPreference("switch_regular_refresh");
            mSwitchRegularRefresh.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean checkStatus = (boolean) newValue;
                mSwitchRegularRefresh.setChecked(checkStatus);
                Context context = getActivity();
                Intent intentRegularRefreshService = new Intent(context, RegularRefreshService.class);
                if (checkStatus) {
                    // 开启定时刷新服务
                    context.startService(intentRegularRefreshService);
                } else {
                    // 关闭定时刷新服务
                    context.stopService(intentRegularRefreshService);
                }
                return false;
            });
        }
    }
}