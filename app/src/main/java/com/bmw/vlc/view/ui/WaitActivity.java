package com.bmw.vlc.view.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ProgressBar;


import com.bmw.vlc.R;
import com.bmw.vlc.presenter.WaitPresenter;
import com.bmw.vlc.presenter.impl.WaitPresentImpl;
import com.bmw.vlc.utils.PhoneUtil;
import com.bmw.vlc.view.viewImpl.WaitViewImpl;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WaitActivity extends BaseActivity implements WaitViewImpl {

    @Bind(R.id.load_progressBar)
    ProgressBar pro;
    private WaitPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        ButterKnife.bind(this);
        presenter = new WaitPresentImpl(this,this);

        presenter.initLibVlc();
        if (PhoneUtil.getmem_TOLAL() >= 0.6) {
            presenter.initPlay(0);
        } else {
            presenter.initPlay(1);
        }

    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    @Override
    public void startActivity() {

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),
                PreviewActivity.class);
        startActivity(intent);

        this.finish();
    }

    @Override
    public void showError(String str) {
        AlertDialog dialog = new AlertDialog.Builder(WaitActivity.this)
                .setTitle("提示信息")
                .setMessage("无法连接到摄像头，请确保手机已经连接到摄像头所在的wifi热点")
                .setNegativeButton("知道了",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                WaitActivity.this.finish();
                            }
                        }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        presenter.release();
        super.onDestroy();
        Log.d("wait", "onDestroy: ");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            Log.d("wait", "onKeyDown: ");
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
