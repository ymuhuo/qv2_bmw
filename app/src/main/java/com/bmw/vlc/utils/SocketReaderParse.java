package com.bmw.vlc.utils;

import android.util.Log;

import java.util.List;

/**
 * Created by yMuhuo on 2016/12/22.
 */
public class SocketReaderParse {

    public static String TAG = "socket";

    public static void parse(List<Integer> list, OnSocketReaderParseFinishListener listener) {
        int start = 2;
        while (true) {
            if (start >= list.size()) {
                Log.i(TAG, "parse: break " + start);
                break;
            }

            int arg_length = list.get(start);


            int end = start+arg_length;
            if(arg_length>=list.size() || end >= list.size() || end < 0 || arg_length==0){
                start++;
                continue;
            }

            int sum_args = list.get(start-1)+list.get(start-2);


            for(int i= start;i<end;i++){
                sum_args+=list.get(i);
            }

            if((sum_args%0x100) == list.get(end) && list.get(start-2) == 0x01){
                if (listener != null) {
                    listener.afterParse(ByteUtil.listToBytes(list.subList(start-2,end+1)));
                }
                start = end + 3;
            }else{
                start++;
            }

        }
    }

    public interface OnSocketReaderParseFinishListener {
        void afterParse(byte[] bytes);
    }
}
