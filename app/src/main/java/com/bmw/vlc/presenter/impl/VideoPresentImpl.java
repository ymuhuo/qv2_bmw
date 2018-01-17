package com.bmw.vlc.presenter.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.bmw.vlc.R;
import com.bmw.vlc.model.AdvanceSetInfo;
import com.bmw.vlc.model.Login_info;
import com.bmw.vlc.presenter.VideoPresenter;
import com.bmw.vlc.utils.BitmapUtils;
import com.bmw.vlc.view.viewImpl.PreViewImpl;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.LibVLC;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by admin on 2016/8/29.
 */
public class VideoPresentImpl implements VideoPresenter {

    private static final String TAG = "main/PreviewActivity/vp";

    private LibVLC mLibVLC;
    private boolean isFirst = true;
    private PreViewImpl view;
    private boolean isRunRecording;
    private boolean isBugDone;
    private SharedPreferences sharedPreferences;


    public VideoPresentImpl(LibVLC mLibVLC, PreViewImpl view, Context context) {
        this.mLibVLC = mLibVLC;
        this.view = view;
        pathIsExist();
        if (mLibVLC != null) {
            EventHandler em = EventHandler.getInstance();
            em.addHandler(evenHandler);
        }
        firstRecordListening();
        sharedPreferences = context.getSharedPreferences(AdvanceSetInfo.ANVANCE_SHARE, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(AdvanceSetInfo.ISDONE, false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(AdvanceSetInfo.ISDONE,false);
            editor.commit();
            editor.clear();
        }

    }

    @Override
    public void records() {
        videoRecord();
    }

    @Override
    public void picShot() {
        snapShot();
    }

    @Override
    public void pause() {
        if (mLibVLC != null)
            if (mLibVLC.isPlaying()) {
                mLibVLC.pause();
            } else {
                mLibVLC.play();
            }
    }

    @Override
    public void release() {

        isRunRecording = false;
        isBugDone = false;
        EventHandler em = EventHandler.getInstance();
        em.removeHandler(evenHandler);
        em = null;

        Log.d(TAG, "release: VideoPresentImpl");
    }


    private Handler evenHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.getData().getInt("event")) {
                case EventHandler.MediaPlayerPlaying:
//                    Log.i(TAG, "MediaPlayerPlaying");
                    break;
                case EventHandler.MediaPlayerPaused:
//                    Log.i(TAG, "MediaPlayerPaused");
                    break;
                case EventHandler.MediaPlayerStopped:
//                    Log.i(TAG, "MediaPlayerStopped");
                    break;
                case EventHandler.MediaPlayerEndReached:
//                    Log.i(TAG, "MediaPlayerEndReached");
                    break;
                case EventHandler.MediaPlayerVout:
                    break;
                default:
//                    Log.d(TAG, "Event not handled");
                    break;
            }
        }
    };

    /**
     * 路径是否存在，不能存在则创建
     */
    private void pathIsExist() {
        File file = new File(BitmapUtils.getSDPath() + Login_info.local_picture_path);
        if (!file.exists())
            file.mkdirs();

        File file1 = new File(BitmapUtils.getSDPath() + Login_info.local_video_path);
        if (!file1.exists())
            file1.mkdirs();
    }


    /**
     * 截图
     */
    private void snapShot() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            String name = df.format(new Date());
            name = BitmapUtils.getSDPath() + Login_info.local_picture_path + name + ".png";
            File file = new File(name);
            if (!file.exists())
                file.createNewFile();
            if (mLibVLC.takeSnapShot(name, 640, 360)) {
                view.iToast("已保存");
            } else {
                view.iToast("截图失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 录像和停止录像
     */
    private void videoRecord() {
        try {

            if (mLibVLC.videoIsRecording()) {
                if (mLibVLC.videoRecordStop()) {

                    view.iToast("停止录像");
                } else {
                    view.iToast("停止录像失败");
                }
            } else {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                String name = df.format(new Date());
                if (mLibVLC.videoRecordStart(BitmapUtils.getSDPath() + Login_info.local_video_path + name)) {
                    if (!isFirst)
                        view.iToast("开始录像");
                } else {
                    view.iToast("开始录像失败");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 录像监听
     */
    protected void ListenRecording() {
        isRunRecording = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (isRunRecording)
                    try {

                        if (mLibVLC.videoIsRecording()) {
                            handlerRecord.sendEmptyMessage(0);
                        } else {
                            handlerRecord.sendEmptyMessage(1);
                        }
                        Thread.sleep(1 * 1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }).start();
    }

    Handler handlerRecord = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    view.setRecordBtn(R.mipmap.recordpress);
                    //开始录像
                    break;
                case 1:
                    //停止录像
                    view.setRecordBtn(R.mipmap.record);
                    break;
                case 2:
                    ListenRecording();
                    break;

            }
            super.handleMessage(msg);
        }

    };

    //第一次录像bug修复
    public void firstRecordListening() {
        isBugDone = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isBugDone) {
                    records();
                    isFirst = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!isFirst) {
                        handlerRecord.sendEmptyMessage(2);
                        isBugDone = false;
                    }
                }
            }
        }).start();
    }


}
