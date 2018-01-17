package com.bmw.vlc.view.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bmw.vlc.R;
import com.bmw.vlc.model.AdvanceSetInfo;
import com.bmw.vlc.presenter.impl.ControlPresentImpl2;
import com.bmw.vlc.view.dialog.AdvancedSetDialog;
import com.bmw.vlc.model.Login_info;
import com.bmw.vlc.view.dialog.ScreenLightDialog;
import com.bmw.vlc.view.dialog.SettingDialog;
import com.bmw.vlc.view.dialog.SystemMsgDialog;
import com.bmw.vlc.presenter.impl.UpdatePresentImpl;
import com.bmw.vlc.utils.WifiAdmin;
import com.bmw.vlc.view.viewImpl.UpdateViewImpl;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;

public class SettingActivity extends BaseActivity implements UpdateViewImpl{

    @Bind(R.id.light_set)
    TextView light_set;
    @Bind(R.id.screen)
    LinearLayout main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        //默认初始化
        Bmob.initialize(this, "2733df1cda91841bbac921e164dc70d4");

        if(Login_info.getInstance().isWifi_auto()) {
            WifiAdmin wifiAdmin = new WifiAdmin(this);
            wifiAdmin.closeWifi();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            Log.d("wait", "onKeyDown: ");
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.sys_info,R.id.light_set, R.id.camera_set, R.id.sys_stat, R.id.sys_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.light_set:
                setScreenLight();
                break;
            case R.id.camera_set:
                setAdvance_set();
                break;
            case R.id.sys_stat:
                new SystemMsgDialog(this,getVersion());
                break;
            case R.id.sys_update:
//                toast("正在检查网络！");
                new UpdatePresentImpl(this,this,getVersion());
                break;
            case R.id.sys_info:
                SettingDialog settingDialog = new SettingDialog(this);
                settingDialog.setOnSettingChangeListener(new SettingDialog.OnSettingChangeListener() {
                    @Override
                    public void changeReporter(boolean isChange) {
                        if(isChange)
                            new ControlPresentImpl2(null,SettingActivity.this).resetSocket();
                    }
                });
                break;
        }
    }

    // 获取当前版本的版本号
    private String getVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "版本号未知";
        }
    }

    private void setAdvance_set(){

        AdvancedSetDialog dialog = new AdvancedSetDialog(this);
        final SharedPreferences sharendPreferences = getSharedPreferences(AdvanceSetInfo.ANVANCE_SHARE, Context.MODE_PRIVATE);
        dialog.setListening(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                SharedPreferences.Editor editor = sharendPreferences.edit();
                switch (compoundButton.getId()){
                    case R.id.fangdou_switch:
                        editor.putBoolean(AdvanceSetInfo.PIC_FANGDOU,b);
                        break;
                    case R.id.kuandongtai_switch:
                        editor.putBoolean(AdvanceSetInfo.KUANDONGTAI,b);
                        break;
                    case R.id.gaoganguang_switch:
                        editor.putBoolean(AdvanceSetInfo.GAOGANLIGHT,b);
                        break;
                    case R.id.qiangguangyizhi_switch:
                        editor.putBoolean(AdvanceSetInfo.LIGHT_YIZHI,b);
                        break;
                    case R.id.touwu_switch:
                        editor.putBoolean(AdvanceSetInfo.TOUWU,b);
                        break;
                }
                editor.putBoolean(AdvanceSetInfo.ISDONE,true);
                editor.commit();
                editor.clear();
            }
        });
    }


    private void setScreenLight(){
        //取得当前亮度
        int normal = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 255);
//        AdvancedSetDialog dialog = new AdvancedSetDialog(this,normal);
//        ScreenPopupWindow dialog = new ScreenPopupWindow(this,normal);
        ScreenLightDialog dialog = new ScreenLightDialog(this,normal);
//        dialog.showPopupWindow(main);
        dialog.setDialogSeeekbarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //取得当前进度
                int tmpInt = seekBar.getProgress();

                //当进度小于80时，设置成40，防止太黑看不见的后果。
                if (tmpInt < 40) {
                    tmpInt = 40;
                }

                //根据当前进度改变亮度
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, tmpInt);
                tmpInt = Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, -1);
                WindowManager.LayoutParams wl = getWindow()
                        .getAttributes();

                float tmpFloat = (float) tmpInt / 255;
                if (tmpFloat > 0 && tmpFloat <= 1) {
                    wl.screenBrightness = tmpFloat;
                }
                getWindow().setAttributes(wl);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void showError(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void update(String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), name)),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
