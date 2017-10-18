package com.shiming.pen.new_code;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * @author shiming
 * @version v1.0 create at 2017/10/17
 * @des 处理draw和touch事件的基类
 */
public abstract class BasePen {

    /**
     * 绘制
     *
     * @param canvas
     */
    public abstract  void draw(Canvas canvas);

    /**
     * 接受并处理onTouchEvent
     *
     * @param event
     * @return
     */
    public  boolean onTouchEvent(MotionEvent event,Canvas canvas){
         return false;
     }


}
