package com.minimalistweather.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.android.material.navigation.NavigationView;
import com.minimalistweather.R;
import com.minimalistweather.entity.database_entity.ManagedCity;
import com.minimalistweather.entity.gson_entity.Location;
import com.minimalistweather.util.BaseConfigUtil;
import com.minimalistweather.util.ExampleUtil;
import com.minimalistweather.util.HttpUtil;
import com.minimalistweather.util.JsonParser;
import com.minimalistweather.view.fragment.WeatherFragment;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

  public static final String TAG = "MainActivity";

  /** 定位相关 */
  public AMapLocationClient mLocationClient = null; // 声明定位客户端

  public AMapLocationClientOption mLocationClientOption = null; // 用于设置定位的模式和相关参数
  private AMapLocationListener mLocationListener; // 声明回调监听器

  private DrawerLayout mDrawerLayout;
  private Toolbar mToolbar;
  private FragmentManager mFragmentManager;
  private String mWeatherId;

  // for receive customer msg from jpush server
  private MessageReceiver mMessageReceiver;
  public static final String MESSAGE_RECEIVED_ACTION =
      "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
  public static final String KEY_TITLE = "title";
  public static final String KEY_MESSAGE = "message";
  public static final String KEY_EXTRAS = "extras";
  private EditText msgText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Window window = getWindow();
    // 透明状态栏
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    initNavigation();
    initToolBar();

    JPushInterface.init(getApplicationContext()); // 极光接口初始化，否则用不了
    registerMessageReceiver(); // 注册消息接收器
    /*
     * 动态申请定位所需要的权限
     */
    List<String> permissionList = new ArrayList<>();
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      // 申请网络定位权限
      permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      // 申请GPS定位权限
      permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      // 申请写入缓存数据的权限
      permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      // 申请读取缓存数据的权限
      permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED) {
      // 申请读取手机当前状态的权限
      permissionList.add(Manifest.permission.READ_PHONE_STATE);
    }
    if (!permissionList.isEmpty()) {
      String[] permissions = permissionList.toArray(new String[permissionList.size()]);
      ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
    } else {
      initLocationListener();
      initLocation();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    String weatherId = getIntent().getStringExtra("weather_id");
    if (weatherId != null) {
      WeatherFragment weatherFragment = new WeatherFragment();
      Bundle bundle = new Bundle();
      bundle.putString("weather_id", weatherId);
      weatherFragment.setArguments(bundle);
      mFragmentManager = getSupportFragmentManager();
      FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
      fragmentTransaction.replace(R.id.coordinator_layout, weatherFragment).commit();
    }
  }

  private void initToolBar() {
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(mToolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(true);
    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(
            this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
    toggle.syncState();
    mDrawerLayout.addDrawerListener(toggle);
  }

  private void initNavigation() {
    mDrawerLayout = findViewById(R.id.main_drawer_layout);
    NavigationView navigationView = findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(
        menuItem -> {
          switch (menuItem.getItemId()) {
            case R.id.choose_city:
              Intent intentChooseCity = new Intent(MainActivity.this, AreaChooseActivity.class);
              startActivity(intentChooseCity);
              break;
            case R.id.manage_city:
              Intent intentManageCity = new Intent(MainActivity.this, CityManageActivity.class);
              startActivity(intentManageCity);
              break;
            case R.id.relocate:
              initLocationListener();
              initLocation();
              break;
            case R.id.about:
              startActivity(new Intent(MainActivity.this, AboutActivity.class));
              break;
            case R.id.config:
              startActivity(new Intent(MainActivity.this, SettingsActivity.class));
              break;
            case R.id.app_exit:
              finish();
            default:
              break;
          }
          return false;
        });
  }

  /**
   * 根据AdCode（区域编码）查询城市ID，并加载城市（编码转换）
   * @return
   */
  private void loadWeatherByAdCode(String adCode) {
    String findLocationUrl = "https://search.heweather.net/find?location=" + adCode + "&key=1f973beb7602432bb31cdceb9da27525";
    HttpUtil.sendHttpRequest(findLocationUrl, new Callback() {
          @Override
          public void onFailure(@NotNull Call call, @NotNull IOException e) {
            runOnUiThread(
                () -> Toast.makeText(MainActivity.this, "城市ID查询失败", Toast.LENGTH_SHORT).show());
          }

          @Override
          public void onResponse(@NotNull Call call, @NotNull Response response)
              throws IOException {
            String responseStr = response.body().string();
            Location location = JsonParser.parseLocation(responseStr);
            if (location != null && BaseConfigUtil.API_STATUS_OK.equals(location.status)) {
              // 得到父级城市
              BaseConfigUtil.PARENT_CITY = location.basic.get(0).parent_city;
              // 定位成功，将定位的城市信息加入城市管理列表
              String cid = location.basic.get(0).cid; // 根据高德地图定位SDK获取的AdCode，使用城市搜索API得到的天气API所需cid
              String districtName = location.basic.get(0).location;
              if (LitePal.where("cid = ?", String.valueOf(cid)).find(ManagedCity.class) == null) {
                ManagedCity city = new ManagedCity();
                city.setCid(cid);
                city.setCityName(districtName);
                city.save();
              }
              WeatherFragment weatherFragment = new WeatherFragment();
              Bundle bundle = new Bundle();
              bundle.putString("weather_id", cid);
              weatherFragment.setArguments(bundle);
              mFragmentManager = getSupportFragmentManager();
              FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
              fragmentTransaction.replace(R.id.coordinator_layout, weatherFragment).commit();
            }
          }
        });
  }

  /** 初始化监听器，获取定位的城市 */
  private void initLocationListener() {
    mLocationListener =
        aMapLocation -> {
          if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
              String adCode = aMapLocation.getAdCode(); // 获取区域编码
              if (!TextUtils.isEmpty(adCode)) {
                BaseConfigUtil.AD_CODE = adCode;
                loadWeatherByAdCode(adCode);
              }
            } else {
              Log.e(
                  "AmapError",
                  "location Error, ErrCode:"
                      + aMapLocation.getErrorCode()
                      + ", errInfo:"
                      + aMapLocation.getErrorInfo());
              Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
              // 定位失败显示北京朝阳区
              String adCode = "110105";
              BaseConfigUtil.AD_CODE = adCode;
              loadWeatherByAdCode(adCode);
            }
            mLocationClient.stopLocation(); // 停止定位
            mLocationClient.onDestroy(); // 销毁定位客户端
          }
        };
  }

  /** 初始化定位 */
  private void initLocation() {
    mLocationClient = new AMapLocationClient(getApplicationContext());
    mLocationClient.setLocationListener(mLocationListener);
    mLocationClientOption = new AMapLocationClientOption(); // 初始化定位参数
    mLocationClientOption.setLocationMode(
        AMapLocationClientOption.AMapLocationMode.Hight_Accuracy); // 高精度模式
    mLocationClientOption.setNeedAddress(true); // 返回地址信息
    mLocationClientOption.setOnceLocation(true); // 单次定位
    mLocationClientOption.setWifiScan(true); // 强制刷新wifi
    mLocationClientOption.setMockEnable(false); // 禁止模拟定位
    // mLocationClientOption.setInterval(2000); // 设置定位时间间隔
    mLocationClient.setLocationOption(mLocationClientOption); // 设置定位参数
    mLocationClient.startLocation(); // 启动定位
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case 1:
        if (grantResults.length > 0) {
          for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
              Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
              finish();
              return;
            }
          }
          initLocationListener();
          initLocation();
        } else {
          Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
          finish();
        }
        break;
      default:
        break;
    }
  }

  public void registerMessageReceiver() {
    mMessageReceiver = new MessageReceiver();
    IntentFilter filter = new IntentFilter();
    filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
    filter.addAction(MESSAGE_RECEIVED_ACTION);
    LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
  }

  public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      try {
        if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
          String messge = intent.getStringExtra(KEY_MESSAGE);
          String extras = intent.getStringExtra(KEY_EXTRAS);
          StringBuilder showMsg = new StringBuilder();
          showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
          if (!ExampleUtil.isEmpty(extras)) {
            showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
          }
          setCostomMsg(showMsg.toString());
        }
      } catch (Exception e) {
      }
    }
  }

  // 设置自定义消息
  private void setCostomMsg(String msg) {
    if (null != msgText) {
      msgText.setText(msg);
      msgText.setVisibility(View.VISIBLE);
    }
  }
}
