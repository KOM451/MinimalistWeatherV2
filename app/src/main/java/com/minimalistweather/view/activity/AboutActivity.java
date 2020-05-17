package com.minimalistweather.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.minimalistweather.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        findViewById(R.id.iv_about_back).setOnClickListener(view -> {
            onBackPressed();
        });
        findViewById(R.id.read_source_code).setOnClickListener(view -> {
            Uri uri = Uri.parse("https://github.com/KOM451/MinimalistWeatherV2");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
        });
        TextView tvVersion = findViewById(R.id.tv_version_num);
        tvVersion.setText(getVersionName(this));
    }

    private static String getVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "V" + versionName;
    }
}
