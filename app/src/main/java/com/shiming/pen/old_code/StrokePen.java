package com.shiming.pen.old_code;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.shiming.pen.Bezier;
import com.shiming.pen.new_code.MotionElement;

import java.util.ArrayList;


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 * @des  画笔的类（）
 */
public class StrokePen {
    //这个控制笔锋的控制值
    protected float DIS_VEL_CAL_FACTOR = 0.02f;
//    protected float DIS_VEL_CAL_FACTOR =2000f;
    //手指在移动的控制笔的变化率
//    protected float WIDTH_THRES_MAX = 0.6f;
    //线的粗细的，这个值越大，线的粗细越加明显
    protected float WIDTH_THRES_MAX = 10f;
    //绘制计算的次数，数值越小计算的次数越多，需要折中
    protected int STEPFACTOR = 10;

    private ArrayList<ControllerPoint> mPointList;
    private ArrayList<ControllerPoint> mHWPointList;
    private Bezier mBezier;
    private ControllerPoint mLastPoint;
    //笔的宽度信息
    private double mBaseWidth;
    private double mLastVel;
    private double mLastWidth;
    private ControllerPoint curPoint;
    private Paint mPaint;

    public void clear() {
        mPointList.clear();
        mHWPointList.clear();

    }

    public StrokePen(Context context) {
        mPointList = new ArrayList<ControllerPoint>();
        mHWPointList = new ArrayList<ControllerPoint>();
        mBezier = new Bezier();
        mLastPoint = new ControllerPoint(0, 0);
    }

    /**
     * 早onDraw需要调用
     * @param canvas 画布
     */
    public void draw(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        //点的集合少 不去绘制
        if (mHWPointList == null || mHWPointList.size() < 1)
            return;
        //当控制点的集合很少的时候，需要画个小圆，但是需要算法
        if (mHWPointList.size() < 2) {
            ControllerPoint point = mHWPointList.get(0);
            //由于此问题在算法上还没有实现，所以暂时不给他画圆圈
            //canvas.drawCircle(point.x, point.y, point.width, mPaint);
        } else {
            curPoint = mHWPointList.get(0);
            for (int i = 1; i < mHWPointList.size(); i++) {
                ControllerPoint point = mHWPointList.get(i);
                drawToPoint(canvas, point, mPaint);
                curPoint = point;
            }
        }
    }


    /**
     * event.getPressure(); //LCD可以感应出用户的手指压力，当然具体的级别由驱动和物理硬件决定的,我的手机上为1
     * @param motionEvent
     * @return
     */
    public MotionElement createMotionElement(MotionEvent motionEvent) {
        System.out.println("shiming== 0000=="+motionEvent.getToolType(0));
        System.out.println("shiming=="+motionEvent.getPressure());
        System.out.println("shiming=="+motionEvent.getEventTime());
        MotionElement motionElement = new MotionElement(motionEvent.getX(), motionEvent.getY(),
                motionEvent.getPressure(), motionEvent.getToolType(0));
        return motionElement;
    }

    /**
     * 手指的down事件
     * @param mElement
     */
    public void onDown(MotionElement mElement) {
        mPaint.setXfermode(null);
        mPointList.clear();
        mHWPointList.clear();
        //记录down的控制点的信息
        ControllerPoint curPoint = new ControllerPoint(mElement.x, mElement.y);
        //如果用笔画的画我的屏幕，记录他宽度的和压力值的乘，但是哇，
        if (mElement.tooltype == MotionEvent.TOOL_TYPE_STYLUS) {
            mLastWidth = mElement.pressure * mBaseWidth;
        } else {
            //如果是手指画的，我们取他的0.8
            mLastWidth = 0.8 * mBaseWidth;
        }
        //down下的点的宽度
        curPoint.width = (float) mLastWidth;
        mLastVel = 0;

        mPointList.add(curPoint);
        //记录当前的点
        mLastPoint = curPoint;
    }
    public void onMove(MotionElement mElement) {
        ControllerPoint curPoint = new ControllerPoint(mElement.x, mElement.y);
        double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        //deltaX和deltay平方和的二次方根 想象一个例子 1+1的平方根为1.4 （x²+y²）开根号
        double curDis = Math.hypot(deltaX, deltaY);
        //我们求出的这个值越小，画的点或者是绘制椭圆形越多，这个值越大的话，绘制的越少，笔就越细，宽度越小
        double curVel = curDis * DIS_VEL_CAL_FACTOR;
        System.out.println("shiming==="+curDis+" "+curVel+" "+deltaX+" "+deltaY);
        double curWidth;
        //点的集合少，我们得必须改变宽度,每次点击的down的时候，这个事件
        if (mPointList.size() < 2) {
            System.out.println("shiming==dian shao");
            if (mElement.tooltype == MotionEvent.TOOL_TYPE_STYLUS) {
                curWidth = mElement.pressure * mBaseWidth;
            } else {
                curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5,
                        mLastWidth);
            }
            curPoint.width = (float) curWidth;
            mBezier.init(mLastPoint, curPoint);
        } else {
            System.out.println("shiming==dian duo");
            mLastVel = curVel;
            if (mElement.tooltype == MotionEvent.TOOL_TYPE_STYLUS) {
                curWidth = mElement.pressure * mBaseWidth;
            } else {
                //由于我们手机是触屏的手机，滑动的速度也不慢，所以，一般会走到这里来
                //阐明一点，当滑动的速度很快的时候，这个值就越小，越慢就越大，依靠着mlastWidth不断的变换
                curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5,
                        mLastWidth);
                System.out.println("shiming=="+curVel+" "+mLastVel+" "+curDis+" " +mLastWidth);
                System.out.println("shiming==dian duo"+curWidth);
            }
            curPoint.width = (float) curWidth;
            mBezier.addNode(curPoint);
        }
        //每次移动的话，这里赋值新的值
        mLastWidth = curWidth;

        mPointList.add(curPoint);

        int steps = 1 + (int) curDis / STEPFACTOR;
        System.out.println("shiming-- steps"+steps);
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            mHWPointList.add(point);
        }

        mLastPoint = curPoint;
    }

    public void onUp(MotionElement mElement, Canvas canvas) {
        ControllerPoint curPoint = new ControllerPoint(mElement.x, mElement.y);
        double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);

        if (mElement.tooltype == MotionEvent.TOOL_TYPE_STYLUS) {
            curPoint.width = (float) (mElement.pressure * mBaseWidth);
        } else {
            curPoint.width = 0;
        }

        mPointList.add(curPoint);

        mBezier.addNode(curPoint);

        int steps = 1 + (int) curDis / STEPFACTOR;
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            mHWPointList.add(point);
        }
        //
        mBezier.end();
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            mHWPointList.add(point);
        }

       // 手指up 我画到纸上上
        draw(canvas);

    }

    /**
     * 通过点去绘制一条线，当现在的点和触摸点的位置在一起的时候不用去绘制
     * @param canvas
     * @param point
     * @param paint
     */
    private void drawToPoint(Canvas canvas, ControllerPoint point, Paint paint) {
        if ((curPoint.x == point.x) && (curPoint.y == point.y)) {
            return;
        }
        drawLine(canvas, curPoint.x, curPoint.y, curPoint.width, point.x,
                point.y, point.width, paint);
    }

    /**
     *
     * @param curVel
     * @param lastVel
     * @param curDis
     * @param factor
     * @param lastWidth
     * @return
     */
    private double calcNewWidth(double curVel, double lastVel, double curDis,
                                  double factor, double lastWidth) {
        double calVel = curVel * 0.6 + lastVel * (1 - 0.6);
        //返回指定数字的自然对数
        double vfac = Math.log(factor * 2.0f) * (-calVel);
        //此方法返回值e，其中e是自然对数的基数。
        double calWidth = mBaseWidth * Math.exp(vfac);

        double mMoveThres = curDis * 0.01f;
        if (mMoveThres > WIDTH_THRES_MAX) {
            mMoveThres = WIDTH_THRES_MAX;
        }
        if (Math.abs(calWidth - mBaseWidth) / mBaseWidth > mMoveThres) {
            if (calWidth > mBaseWidth) {
                calWidth = mBaseWidth * (1 + mMoveThres);
            } else {
                calWidth = mBaseWidth * (1 - mMoveThres);
            }
        } else if (Math.abs(calWidth - lastWidth) / lastWidth > mMoveThres) {
            if (calWidth > lastWidth) {
                calWidth = lastWidth * (1 + mMoveThres);
            } else {
                calWidth = lastWidth * (1 - mMoveThres);
            }
        }
        return calWidth;
    }

    /**
     * 其实这里才是关键的地方，通过画布画椭圆，每一个点都是一个椭圆，这个椭圆的所有细节，逐渐构建出一个完美的笔尖
     * 和笔锋的效果,我觉得在这里需要大量的测试，其实就对低端手机进行排查，看我们绘制的笔的宽度是多少，绘制多少个椭圆
     * 然后在低端手机上不会那么卡，当然你哪一个N年前的手机给我，那也的卡，只不过需要适中的范围里面
     * @param canvas
     * @param x0
     * @param y0
     * @param w0
     * @param x1
     * @param y1
     * @param w1
     * @param paint
     */
    private void drawLine(Canvas canvas, double x0, double y0, double w0, double x1, double y1, double w1, Paint paint){
         //求两个数字的平方根 x的平方+y的平方在开方记得X的平方+y的平方=1，这就是一个园
        double curDis = Math.hypot(x0-x1, y0-y1);
        int steps = 1;
        if(paint.getStrokeWidth() < 6){
            steps = 1+(int)(curDis/2);
        }else if(paint.getStrokeWidth() > 60){
            steps = 1+(int)(curDis/4);
        }else{
            steps = 1+(int)(curDis/3);
        }
        double deltaX=(x1-x0)/steps;
        double deltaY=(y1-y0)/steps;
        double deltaW=(w1-w0)/steps;
        double x=x0;
        double y=y0;
        double w=w0;

        for(int i=0;i<steps;i++){
            //都是用于表示坐标系中的一块矩形区域，并可以对其做一些简单操作
            //精度不一样。Rect是使用int类型作为数值，RectF是使用float类型作为数值。
//            Rect rect = new Rect();
            RectF oval = new RectF();
            oval.set((float)(x-w/4.0f), (float)(y-w/2.0f), (float)(x+w/4.0f), (float)(y+w/2.0f));
//            oval.set((float)(x+w/4.0f), (float)(y+w/4.0f), (float)(x-w/4.0f), (float)(y-w/4.0f));
            //最基本的实现，通过点控制线，绘制椭圆
            canvas.drawOval(oval, paint);
            x+=deltaX;
            y+=deltaY;
            w+=deltaW;
        }
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
        mBaseWidth = paint.getStrokeWidth();
    }

}

