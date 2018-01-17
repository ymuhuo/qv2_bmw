package com.bmw.vlc.view.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bmw.vlc.R;
import com.bmw.vlc.model.AdvanceSetInfo;
import com.bmw.vlc.model.Login_info;
import com.bmw.vlc.utils.SocketUtilNew;
import com.bmw.vlc.utils.WifiAdmin;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private final static String TAG = "mainActivity";
    private Login_info login_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate: mainActivity");
        initAdvanceSet(); //初始化高级设置
        login_info =  Login_info.getInstance();
       login_info.initLoginInfo(this);
        SocketUtilNew.getInstance(this);

    }

    @Override
    protected void onResume() {
        log("onresume");
        wifiAuto();
        super.onResume();
    }

    private void wifiAuto() {
        if(login_info.isWifi_auto()){
            WifiAdmin wifiAdmin = new WifiAdmin(this);
            String ssid = login_info.getWifi_SSID();
            System.out.println("aaaaa a= "+ssid+"   p= "+login_info.getWifi_Password());
            wifiAdmin.openWifi();
            wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(ssid, login_info.getWifi_Password(), 3));

//            if (wifiAdmin != null && wifiAdmin.getNetworkId()!= -1 && wifiAdmin.getSSID() != null && (wifiAdmin.getSSID().equals("\"" + ssid + "\"") || wifiAdmin.getSSID().equals(ssid))) {
//              return;
//            }
        }
    }

    private void initAdvanceSet() {
        SharedPreferences sharedPreferences = getSharedPreferences(AdvanceSetInfo.ANVANCE_SHARE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AdvanceSetInfo.GAOGANLIGHT, false);
        editor.putBoolean(AdvanceSetInfo.KUANDONGTAI, false);
        editor.putBoolean(AdvanceSetInfo.LIGHT_YIZHI, false);
        editor.putBoolean(AdvanceSetInfo.PIC_FANGDOU, false);
        editor.putBoolean(AdvanceSetInfo.TOUWU, false);
        editor.putBoolean(AdvanceSetInfo.ISDONE, true);
        editor.putInt(AdvanceSetInfo.LOW_LIGHT,0);
        editor.putInt(AdvanceSetInfo.HIGH_LIGHT,0);
        editor.commit();
        editor.clear();
    }

    @OnClick({R.id.pre_view, R.id.play_back, R.id.picture, R.id.environment_stat, R.id.setting})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.pre_view:
                    intent = new Intent(this, WaitActivity.class);
//                setBtnNoUse();
                break;
            case R.id.play_back:
                intent = new Intent(this, FileActivity.class);
                intent.putExtra("picture", false);
                break;
            case R.id.picture:
                intent = new Intent(this, FileActivity.class);
//                startActivity(intent);
                break;
            case R.id.environment_stat:
                    intent = new Intent(this, EnvironmentActivity.class);
                break;
            case R.id.setting:
                intent = new Intent(this, SettingActivity.class);
//                startActivity(intent);
                break;
        }

        if (intent != null)
            startActivity(intent);


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        SocketUtilNew.getInstance(this).release();

        super.onDestroy();

        ButterKnife.unbind(this);
    }

}
