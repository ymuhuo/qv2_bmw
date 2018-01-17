package com.bmw.vlc.utils;

import android.content.Context;
import android.util.Log;

import com.bmw.vlc.model.Login_info;
import com.bmw.vlc.utils.singleThreadUtil.FinalizableDelegatedExecutorService;
import com.bmw.vlc.utils.singleThreadUtil.RunnablePriority;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2016/9/19.
 */
public class SocketUtilNew {

    //    private PreViewImpl preview;
    private Socket socket;
    private OutputStream socketWriter;
    private InputStream socketReader;
    private boolean isFinish;
    private int digui_num;
    private Login_info loginInfo;
    public ExecutorService singleThreadExecutor = new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>()));
    private long current_time;
    private String lastActionName;
    private byte[] lastCommand;
    private static String TAG = "socket";
    private static SocketUtilNew instance;
    private boolean isOutOfTime;
    ExecutorService fixedThreadPool;

//    private int all_reader_num;
//    private ExecutorService singleThreadExecutor = null;

    public static SocketUtilNew getInstance(Context c){
        if(instance == null){
            synchronized (Login_info.class){
                if(instance == null)
                    instance = new SocketUtilNew(c);
            }
        }
        return instance;
    }

    public boolean isSocketNull() {
        if (socket == null)
            return true;
        else
            return false;
    }

    private SocketUtilNew(Context context) {
        loginInfo = Login_info.getInstance();
        fixedThreadPool = Executors.newFixedThreadPool(6);
        initSocket();
//        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //初始化socket
    public void initSocket() {

        if(fixedThreadPool == null || fixedThreadPool.isShutdown()){
            fixedThreadPool = Executors.newFixedThreadPool(4);
        }

        if(singleThreadExecutor == null || singleThreadExecutor.isShutdown()){
            singleThreadExecutor =  new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>()));
        }
        isFinish = true;
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                int i = 1;
                while (isFinish && socket == null) {
                    try {

                        socket = new Socket(loginInfo.getSocket_ip(), loginInfo.getSocket_port());
                        Log.i(TAG, "run: connect: " + socket.isBound());
                        socket.setSoTimeout(100);
                        socketWriter = socket.getOutputStream();
                        socketReader = socket.getInputStream();
                        Log.i(TAG, "run: socket 连接成功!");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (socket == null)
                        try {
                            Log.e(TAG, "run: 第 " + i + " 次socket连接失败");
                            Thread.sleep(1000 * 10);
                            i++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            }

        };
        thread.start();
    }

    public void setTimeOut(int time){
        try {
            socket.setSoTimeout(time);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    //发送命令（控制方向、速度的命令）
    public void sendcmd(byte[] commands, String action_name) {
        if (socket == null) {
            Log.e(TAG, "发送" + action_name + "命令失败：sendcmd: socket is null");
            return;
        }
        if (socketWriter == null)
            try {
                socketWriter = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            socketWriter.write(commands);
            socketWriter.flush();
            Log.i(TAG, "sendcmd: 已经发送 " + action_name + " 命令！");
        } catch (IOException e) {
            if (socket != null && !socket.isClosed()) {
                Log.e(TAG, "socketService is already closed!  ");
                socket = null;
                initSocket();
            }
            e.printStackTrace();
        }
    }

    public void release() {
        isFinish = false;
        if (socket != null) {
            try {
                singleThreadExecutor.shutdownNow();
                fixedThreadPool.shutdownNow();
                socket.shutdownInput();
                socket.shutdownOutput();
                socketReader.close();
                socketWriter.close();
                if (socket != null)
                    socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "release: Socket已经释放内存！");
    }

    public void threadShutdown() {
        singleThreadExecutor.shutdownNow();
    }

    public void getReader(final byte[] commands,
                          final int priority,
                          final String action_name,
                          final int which) {
        if (socket == null) {
            Log.e(TAG, "发送" + action_name + "命令失败：sendcmd: socket is null");
            return;
        }
        if (socketReader == null)
            try {
                socketReader = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (socket != null)
            singleThreadExecutor.execute(new RunnablePriority(priority) {
                @Override
                public void run() {

                    while ((action_name.contains("停止") ) && System.currentTimeMillis() - current_time <= 150) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
//                    if (which == 1 && isOutOfTime) {
//                        readAll();
////                        try {
////                            socketReader.read(new byte[215]);
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
//                    }
                    sendcmd(commands, action_name);
                    if (action_name.contains("停止")) {
                        sleep(50);
                    }

                    if (which == 1) {
//                        if (lastActionName != null && lastActionName.contains("停止")) {
//                            sendcmd(lastCommand, lastActionName);
//                        }
                        read(action_name);
//                        try {
//                            socketReader.read(result);
//                            listener.Result(result);
//                            isOutOfTime = false;
//                            Log.i(TAG, "run: 已经接收 " + action_name + " 命令： " + Integer.toHexString(result[1] & 0xff)+"  电量= "+result[3]);
//                            digui_num = 0;
//                        } catch (SocketTimeoutException e) {
//                            isOutOfTime = true;
//                            Log.e(TAG, "接收" + action_name + "数据超时！");
////                            sleep(100);
//                            e.printStackTrace();
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                    current_time = System.currentTimeMillis();
                    lastActionName = action_name;
                    lastCommand = commands;
                }
            });
    }


    public void readAll() {
        int len;
        byte[] bytes = new byte[1];
        try {
            int i = 0;
            while ((len = socketReader.read(bytes)) != -1) {
                Log.i(TAG, "正在清空流数据：byte[" + i + "] = " + Integer.toHexString(bytes[0] & 0xff));
                i++;
            }
        } catch (SocketTimeoutException e) {
            Log.i(TAG, "流数据清空完成！");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void read(String actionName){
        if (socketReader != null) {
            List<Integer> list = new ArrayList<>();
            int i = 0;
            try {

                int len = 0;
                byte[] bs = new byte[1];
                while ((len = socketReader.read(bs)) != -1) {
                   list.add(bs[0] & 0xff);
                    Log.i(TAG,"run: 正在接收"+actionName+"数据: byte[" + i + "] = " + Integer.toHexString(list.get(i)));
                    i++;
                }
            } catch (SocketTimeoutException e) {

                if (i != 0) {
                    parse(list);
                    isOutOfTime = false;
                }else {
                    Log.e(TAG,"接收 "+actionName+" 数据失败！");
                    isOutOfTime = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    public void parse(final List<Integer> list) {
        isFinish = true;
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: 正在解析数据");
                SocketReaderParse.parse(list, new SocketReaderParse.OnSocketReaderParseFinishListener() {
                    @Override
                    public void afterParse(byte[] bytes) {
                        Log.i(TAG, "afterParse: 解析完成");
                        if(listener != null){
                            listener.result(bytes);
                        }
                    }
                });
            }
        });
    }


    public interface OnDataReaderListener {
        void result(byte[] bytes);
    }

    public OnDataReaderListener listener;


    public void setOnDataReaderListener(OnDataReaderListener listener) {
        this.listener = listener;
    }

}

