package com.minimalistweather.view.activity;

import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.minimalistweather.view.fragment.AreaChooseFragment;

public class AreaChooseActivity extends BaseFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new AreaChooseFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
