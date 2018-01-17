package com.bmw.vlc.view.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bmw.vlc.R;
import com.bmw.vlc.model.AdvanceSetInfo;
import com.bmw.vlc.presenter.ControlPresenter;
import com.bmw.vlc.presenter.VideoPresenter;
import com.bmw.vlc.presenter.impl.ControlPresentImpl2;
import com.bmw.vlc.presenter.impl.VideoPresentImpl;
import com.bmw.vlc.view.view.Vertical_seekbar;
import com.bmw.vlc.view.viewImpl.PreViewImpl;

import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Util;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class PreviewActivity extends BaseActivity implements PreViewImpl, IVideoPlayer {

    private static final String TAG = "main/PreviewActivity";

    @Bind(R.id.main_surface)
    SurfaceView surfaceView;
    @Bind(R.id.battery)
    ImageView battery;//电池电量显示
    @Bind(R.id.lights_adjust)
    Vertical_seekbar lightAdjust;//灯光调节
    @Bind(R.id.lights_show)
    TextView light_text;
    @Bind(R.id.Records)
    ImageView recordBtn;
    private VideoPresenter vPresenter; //录像及截图执行者
    private LibVLC mLibVLC = null;
    private ControlPresenter cPresenter;//socket控制执行者
    private SurfaceHolder surfaceHolder = null;
    private boolean isHighBeam;
    private boolean isGetBattery;
    private int high_num; //记录远光灯
    private int low_num;  //记录近光灯
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.RGBX_8888);
        surfaceHolder.addCallback(mSurfaceCallback);
        initVlc();
        vPresenter = new VideoPresentImpl(mLibVLC, this,this);
        cPresenter = new ControlPresentImpl2(this,this);
        cPresenter.startThread();
        sharedPreferences = getSharedPreferences(AdvanceSetInfo.ANVANCE_SHARE, Context.MODE_PRIVATE);
        high_num = sharedPreferences.getInt(AdvanceSetInfo.HIGH_LIGHT,0);
        low_num = sharedPreferences.getInt(AdvanceSetInfo.LOW_LIGHT,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        batteryListening();
        lightAdjust.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d(TAG, "onProgressChanged: running");
                if (isHighBeam) {
                    high_num = i;
                    cPresenter.high_beam_open(high_num);
                } else {
                    low_num = i;
                    cPresenter.low_beam_open(low_num);
                }
                if (i == 0) {
                    if (isHighBeam)
                        cPresenter.high_beam_close();
                    else
                        cPresenter.low_beam_close();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch: ");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: ");
            }
        });
    }

    //初始化LibVLC对象
    public void initVlc() {

        try {
            mLibVLC = Util.getLibVlcInstance();
            mLibVLC.setNetworkCaching(800);
        } catch (LibVlcException e) {
            e.printStackTrace();
        }
//        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
//        am.getMemoryInfo(mi);
//
//        Log.d(TAG, "initVlc: "+mi.availMem/(1024*1024));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @OnClick({R.id.lights_switch, R.id.Records, R.id.screenShot, R.id.autoHorizontal})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lights_switch:
                if (isHighBeam) {
                    light_text.setText("近光灯");
                    isHighBeam = false;
                    lightAdjust.setProgress(low_num);
                } else {
                    light_text.setText("远光灯");
                    isHighBeam = true;
                    lightAdjust.setProgress(high_num);
                }
                break;
            case R.id.Records: //录像
                vPresenter.records();
                break;
            case R.id.screenShot://截图
                vPresenter.picShot();
                break;
            case R.id.autoHorizontal://自动水平
                cPresenter.autoHorizontal();
                break;
        }
    }

    @OnTouch({R.id.zoom_add, R.id.zoom_sub, R.id.size_add, R.id.size_sub, R.id.lights_switch, R.id.up, R.id.down})
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.zoom_add: //聚焦近

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cPresenter.zoom_add();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    cPresenter.stop();
                }
                break;
            case R.id.zoom_sub://聚焦远

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cPresenter.zoom_sub();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    cPresenter.stop();
                }
                break;
            case R.id.size_add://变倍变长

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cPresenter.size_add();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    cPresenter.stop();
                }
                break;
            case R.id.size_sub://变倍变短

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cPresenter.size_sub();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    cPresenter.stop();
                }
                break;
            case R.id.up://控制摄像头向上

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cPresenter.up();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    cPresenter.stop();
                }
                break;
            case R.id.down://控制摄像头向下

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    cPresenter.down();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    cPresenter.stop();
                }
                break;
        }
        return false;
    }


    //设置surface大小
    @Override
    public void setSurfaceSize(final int width, final int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        Log.d(TAG, "setSurfaceSize: ");
        final int dw = getWindow().getDecorView().getWidth();
        final int dh = getWindow().getDecorView().getHeight();
        surfaceHandler.post(new Runnable() {
            @Override
            public void run() {
                surfaceHolder.setFixedSize(width, height);
                ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
                lp.width = dw;
                lp.height = dh;
                surfaceView.setLayoutParams(lp);
                surfaceView.invalidate();
            }
        });
    }
    /**
     * attach and disattach surface to the lib
     */
    private final SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.d(TAG, "surfaceChanged: ");
            mLibVLC.attachSurface(holder.getSurface(), PreviewActivity.this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated: ");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed: stop - detachsurface");
            mLibVLC.stop();
            mLibVLC.detachSurface();
        }
    };

    @Override//回调改变提示信息
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //回调结束activity

    @Override
    protected void onStop() {
//        lightAdjust.setProgress(0);
//        cPresenter.low_beam_close();
//        cPresenter.high_beam_close();
//        cPresenter.stop();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(AdvanceSetInfo.LOW_LIGHT,low_num);
        editor.putInt(AdvanceSetInfo.HIGH_LIGHT,high_num);
        editor.commit();
        editor.clear();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    //释放资源
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy:start ");
        isGetBattery = false;
        cPresenter.release();
        vPresenter.release();
        Log.d(TAG, "onDestroy:finish ");
        vPresenter = null;
        cPresenter = null;
        super.onDestroy();
    }



    private Handler surfaceHandler = new Handler();

    //设置返回控制，解决返回后不能进入bug，界面隐藏暂停播放
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            if (mLibVLC != null)
                mLibVLC.stop();
        }
        if (mLibVLC != null) {
            mLibVLC.pause();
        }
        return super.onKeyDown(keyCode, event);
    }


    //界面回归自动播放
    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart: ");
        if (mLibVLC != null)
            mLibVLC.play();
        super.onRestart();
    }

    @Override
    public void iToast(final String msg) {
        batterayHandler.post(new Runnable() {
            @Override
            public void run() {
                toast(msg);
            }
        });
    }

    @Override
    public void setBattery(int pic) {
        battery.setImageResource(pic);
    }

    @Override
    public void ierror(String msg) {
        error(msg);
    }

    @Override
    public void ilog(String msg) {
        log(msg);
    }

    @Override
    public void setRecordBtn(int i) {
        recordBtn.setImageResource(i);
    }

    private void batteryListening() {
        isGetBattery = true;
        new Thread() {
            @Override
            public void run() {
                while (isGetBattery) {
                    batterayHandler.sendEmptyMessage(0);
                    try {
                        sleep(1000 * 2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                super.run();
            }
        }.start();
    }

    private Handler batterayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (cPresenter != null)
                cPresenter.getBattery();
        }
    };
}
