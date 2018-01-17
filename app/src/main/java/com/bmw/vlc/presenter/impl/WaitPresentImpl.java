package com.bmw.vlc.presenter.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.bmw.vlc.model.Login_info;
import com.bmw.vlc.presenter.WaitPresenter;
import com.bmw.vlc.view.viewImpl.WaitViewImpl;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Util;

/**
 * Created by admin on 2016/9/1.
 */
public class WaitPresentImpl implements WaitPresenter {

    private final static String TAG = "main/WaitPresentImpl";
    private WaitViewImpl view;
    private LibVLC mLibVLC;
    String videoUrl2 = "rtsp://admin:bmw12345@172.169.10.65:554/IP/main";

    public WaitPresentImpl(WaitViewImpl view, Context context) {
        this.view = view;
    }

    @Override
    public void initPlay(int i) {
        String pathUri = Login_info.getInstance().BASE_URL_START +
                Login_info.getInstance().getVideo_account() + ":" +
                Login_info.getInstance().getVideo_password() + "@" +
                Login_info.getInstance().getVideo_ip() + ":" +
                Login_info.getInstance().getVideo_port() +
                Login_info.getInstance().BASE_URL_END;
        Log.i(TAG, "aaaaa initPlay: "+ pathUri);
//        String pathUri = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
//        String pathUri = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
        /*switch (i){
            case 0:
                pathUri = All_Url_info.getVideoUrl1();
                break;
            case 1:
                pathUri = All_Url_info.getVideoUrl2();
                break;
        }*/
        if (mLibVLC != null && pathUri != null) {
            if (!mLibVLC.isPlaying()) {
                mLibVLC.playMyMRL(pathUri);
            }
        }
    }

    @Override
    public void release() {
        if (mLibVLC != null) {
//            mLibVLC.stop();
        }
        EventHandler em = EventHandler.getInstance();
        em.removeHandler(handler);
    }

    @Override
    public void initLibVlc() {
        try {
//             LibVLC.init(getApplicationContext());
            mLibVLC = Util.getLibVlcInstance();
            if (!mLibVLC.isPlaying()) {
                EventHandler em = EventHandler.getInstance();
                em.addHandler(handler);
            } else {
                Log.d(TAG, "initLibVlc: isplaying");
                view.startActivity();
            }
        } catch (LibVlcException e) {
            e.printStackTrace();
        }
    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "Event = " + msg.getData().getInt("event"));
            switch (msg.getData().getInt("event")) {
                case EventHandler.MediaPlayerPlaying: //260

                case EventHandler.MediaPlayerPaused: //261

                    break;
                case EventHandler.MediaPlayerStopped: //262

                    break;
                case EventHandler.MediaPlayerEndReached:  //265

                    break;
                case EventHandler.MediaPlayerVout: //274
                    if (msg.getData().getInt("data") > 0) {
                        view.startActivity();
                    }
                    break;
                case EventHandler.MediaPlayerPositionChanged:  //268
                    break;
                case EventHandler.MediaPlayerEncounteredError:  //266
                    view.showError("无法连接到摄像头，请确保手机已经连接到摄像头所在的wifi热点");
                    break;
                default:
                    Log.d(TAG, "Event not handled ");
                    break;
            }
        }
    };

}
