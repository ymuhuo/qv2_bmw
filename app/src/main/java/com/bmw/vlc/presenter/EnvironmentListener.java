package com.bmw.vlc.presenter;

import com.bmw.vlc.model.Environment;

import java.util.List;

/**
 * Created by admin on 2016/9/19.
 */
public interface EnvironmentListener {
    void success(List<Environment> list);
    void failure();

}
