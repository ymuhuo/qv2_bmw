package com.bmw.vlc.utils;

import android.util.Log;

import java.util.List;

/**
 * Created by yMuhuo on 2016/12/22.
 */
public class ByteUtil {

    public static int sum_bytes(byte[] bytes, int start, int end) {

        if (bytes == null || end >= bytes.length || start < 0 || end < start)
            return -1;

        int sum = 0;
        for (int i = start; i <= end; i++) {
            sum += (bytes[i] & 0xff);
        }
        Log.i("socket", "sum_bytes: "+ Integer.toHexString(sum)+ " sum% = "+ Integer.toHexString(sum % 0x100));
        return (sum % 0x100);
    }

    public static byte[] cut_bytes(byte[] bytes,int start,int end){
        if (bytes == null || end >= bytes.length || start < 0 || end < start)
            return null;
        byte[] reBytes = new byte[end+1];
        int sum = end-start+1;
        int reStart = start;
        for(int i=0;i<sum;i++){
            reBytes[i] = bytes[reStart];
            reStart++;
        }

        return reBytes;
    }

    public static byte[] listToBytes(List<Integer> list){
        if(list == null)
            return null;
        int count = list.size();
        byte[] bytes = new byte[count];
        for(int i = 0;i<count;i++){
            int n = list.get(i);
            bytes[i] = (byte) n;
        }
        return bytes;
    }
}
