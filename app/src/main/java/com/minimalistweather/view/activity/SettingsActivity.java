package com.minimalistweather.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.minimalistweather.R;

import org.jetbrains.annotations.NotNull;

public class SettingsActivity extends AppCompatActivity {

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

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
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
    }
}