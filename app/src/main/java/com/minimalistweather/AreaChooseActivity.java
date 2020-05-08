package com.minimalistweather;

import androidx.fragment.app.Fragment;

import android.os.Bundle;

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
