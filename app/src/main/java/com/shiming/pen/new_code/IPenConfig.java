package com.shiming.pen.new_code;

import android.graphics.Color;

/**
 * @author shiming
 * @version v1.0 create at 2017/10/10
 * @des  笔的设置，但是有些笔的设置最好不要放在这里，不要笔的颜色和宽度
 */
public interface IPenConfig {

    /**
     * 清除画布
     */
    int STROKE_TYPE_ERASER = 0;

    /**
     * 钢笔
     */
     int STROKE_TYPE_PEN = 1;// 钢笔
    /**
     * 毛笔
     */
    int STROKE_TYPE_BRUSH = 2;// 毛笔

    //设置笔的宽度
    int PEN_WIDTH=60;
    //笔的颜色
    int PEN_CORLOUR= Color.parseColor("#FF4081");

    //这个控制笔锋的控制值
     float DIS_VEL_CAL_FACTOR = 0.02f;
    //手指在移动的控制笔的变化率  这个值越大，线条的粗细越加明显
    //float WIDTH_THRES_MAX = 0.6f;
     float WIDTH_THRES_MAX = 0.6f;
    //绘制计算的次数，数值越小计算的次数越多，需要折中
     int STEPFACTOR = 10;
}
