package com.bmw.vlc.model.model;

import com.bmw.vlc.presenter.EnvironmentListener;

/**
 * Created by admin on 2016/9/19.
 */
public interface EnvironmentMode {
    void getDatas(EnvironmentListener environmentListener);
    void realese();
}
