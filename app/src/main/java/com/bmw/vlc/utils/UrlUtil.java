package com.bmw.vlc.utils;

import android.os.Environment;

import java.text.SimpleDateFormat;

/**
 * Created by admin on 2016/9/30.
 */
public class UrlUtil {


    public static String getFileName() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd-hhmmss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    public static String getSDPath() {
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) {
            return Environment.getExternalStorageDirectory().toString();
        } else
            return Environment.getDownloadCacheDirectory().toString();
    }
}
