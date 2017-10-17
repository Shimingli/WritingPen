package com.shiming.pen.new_code;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.shiming.pen.old_code.ControllerPoint;

import static com.shiming.pen.new_code.IPenConfig.STEPFACTOR;

/**
 * @author shiming
 * @version v1.0 create at 2017/10/17
 * @des 钢笔
 */
public class SteelPen extends BasePenExtend {

    public SteelPen(Context context) {
        super(context);
    }

    @Override
    protected void drawNeetToDo(Canvas canvas) {
        for (int i = 1; i < mHWPointList.size(); i++) {
            ControllerPoint point = mHWPointList.get(i);
            drawToPoint(canvas, point, mPaint);
            mCurPoint = point;
        }
    }

    @Override
    protected void moveNeetToDo(double curDis) {
        int steps = 1 + (int) curDis / STEPFACTOR;
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            mHWPointList.add(point);
        }
    }

    @Override
    protected void doNeetToDo(Canvas canvas, ControllerPoint point, Paint paint) {
        drawLine(canvas, mCurPoint.x, mCurPoint.y, mCurPoint.width, point.x,
                point.y, point.width, paint);
    }

    /**
     * 其实这里才是关键的地方，通过画布画椭圆，每一个点都是一个椭圆，这个椭圆的所有细节，逐渐构建出一个完美的笔尖
     * 和笔锋的效果,我觉得在这里需要大量的测试，其实就对低端手机进行排查，看我们绘制的笔的宽度是多少，绘制多少个椭圆
     * 然后在低端手机上不会那么卡，当然你哪一个N年前的手机给我，那也的卡，只不过需要适中的范围里面
     *
     * @param canvas
     * @param x0
     * @param y0
     * @param w0
     * @param x1
     * @param y1
     * @param w1
     * @param paint
     */
    private void drawLine(Canvas canvas, double x0, double y0, double w0, double x1, double y1, double w1, Paint paint) {
        //求两个数字的平方根 x的平方+y的平方在开方记得X的平方+y的平方=1，这就是一个园
        double curDis = Math.hypot(x0 - x1, y0 - y1);
        int steps = 1;
        if (paint.getStrokeWidth() < 6) {
            steps = 1 + (int) (curDis / 2);
        } else if (paint.getStrokeWidth() > 60) {
            steps = 1 + (int) (curDis / 4);
        } else {
            steps = 1 + (int) (curDis / 3);
        }
        double deltaX = (x1 - x0) / steps;
        double deltaY = (y1 - y0) / steps;
        double deltaW = (w1 - w0) / steps;
        double x = x0;
        double y = y0;
        double w = w0;

        for (int i = 0; i < steps; i++) {
            //都是用于表示坐标系中的一块矩形区域，并可以对其做一些简单操作
            //精度不一样。Rect是使用int类型作为数值，RectF是使用float类型作为数值。
            //            Rect rect = new Rect();
            RectF oval = new RectF();
            oval.set((float) (x - w / 4.0f), (float) (y - w / 2.0f), (float) (x + w / 4.0f), (float) (y + w / 2.0f));
            //            oval.set((float)(x+w/4.0f), (float)(y+w/4.0f), (float)(x-w/4.0f), (float)(y-w/4.0f));
            //最基本的实现，通过点控制线，绘制椭圆
            canvas.drawOval(oval, paint);
            x += deltaX;
            y += deltaY;
            w += deltaW;
        }
    }
}
