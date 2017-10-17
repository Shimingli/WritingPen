package com.shiming.pen.old_code;


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 * @des 每个点的控制，关心三个因素：笔的宽度，坐标,透明数值
 */
public class ControllerPoint {
    public float x;
    public float y;

    public float width;
    public int alpha = 255;
    public ControllerPoint() {
    }

    public ControllerPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }


    public void set(float x, float y, float w) {
        this.x = x;
        this.y = y;
        this.width = w;
    }


    public void set(ControllerPoint point) {
        this.x = point.x;
        this.y = point.y;
        this.width = point.width;
    }


    public String toString() {
        String str = "X = " + x + "; Y = " + y + "; W = " + width;
        return str;
    }


}
