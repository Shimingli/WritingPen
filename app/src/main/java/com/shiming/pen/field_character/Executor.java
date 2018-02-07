package com.shiming.pen.field_character;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.shiming.pen.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author shiming
 * @version v1.0 create at 2017/9/13
 * @des 使用模式为enum的单利模式
 */
public enum Executor {
    INSTANCE;
    private ScheduledExecutorService mScheduledExecutorService;
    private DrawViewLayout.IActionCallback mCallback;

    public void setCallback(DrawViewLayout.IActionCallback callback) {
        mCallback = callback;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int viewId = msg.what;
            switch (viewId) {
                case R.id.rll_show_delete_container:
                    if (mCallback == null)
                        return;
                    mCallback.deleteOnLongClick();
                    break;
            }
        }
    };

    public void upData(int id) {
        final int vid = id;
        //只有一个线程，用来调度执行将来的任务
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //多少时间执行一次
        mScheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = vid;
                handler.sendMessage(msg);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);    //每间隔100ms发送Message
    }

    public void stop() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdownNow();
            mScheduledExecutorService = null;
        }
    }
}
