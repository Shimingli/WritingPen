package com.shiming.pen.new_code;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.shiming.pen.Bezier;
import com.shiming.pen.old_code.ControllerPoint;

import java.util.ArrayList;

/**
 * @author shiming
 * @version v1.0 create at 2017/10/10
 * @des
 */
public abstract class BasePenExtend extends BasePen {

    public ArrayList<ControllerPoint> mHWPointList=new ArrayList<>();

    public ArrayList<ControllerPoint>   mPointList = new ArrayList<ControllerPoint>();

    public ControllerPoint   mLastPoint = new ControllerPoint(0, 0);

    public Paint mPaint;
    //笔的宽度信息
    public double mBaseWidth;

    public double mLastVel;

    public double mLastWidth;

    public Bezier mBezier = new Bezier();

    protected ControllerPoint mCurPoint;
    protected Context mContext;

    public BasePenExtend(Context context){
        mContext = context;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
        mBaseWidth = paint.getStrokeWidth();
    }

    @Override
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
            mCurPoint = mHWPointList.get(0);
            drawNeetToDo(canvas);
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event,Canvas canvas) {
        // event会被下一次事件重用，这里必须生成新的，否则会有问题
        MotionEvent event2 = MotionEvent.obtain(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onDown(createMotionElement(event2));
                return true;
            case MotionEvent.ACTION_MOVE:
                onMove(createMotionElement(event2));
                return true;
            case MotionEvent.ACTION_UP:
                onUp(createMotionElement(event2),canvas);
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event,canvas);
    }

    /**
     * 按下的事件
     * @param mElement
     */
    public void onDown(MotionElement mElement){
        if (mPaint==null){
            throw new NullPointerException("paint 笔不可能为null哦");
        }
        if (getNewPaint(mPaint)!=null){
            Paint paint=getNewPaint(mPaint);
            mPaint=paint;
            //当然了，不要因为担心内存泄漏，在每个变量使用完成后都添加xxx=null，
            // 对于消除过期引用的最好方法，就是让包含该引用的变量结束生命周期，而不是显示的清空
            paint=null;
            System.out.println("shiming 当绘制的时候是否为新的paint"+mPaint+"原来的对象是否销毁了paint=="+paint);
        }
        mPointList.clear();
        //如果在brush字体这里接受到down的事件，把下面的这个集合清空的话，那么绘制的内容会发生改变
        //不清空的话，也不可能
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

    protected  Paint getNewPaint(Paint paint){
        return null;
    }

    /**
     * 手指移动的事件
     * @param mElement
     */
    public void onMove(MotionElement mElement){

        ControllerPoint curPoint = new ControllerPoint(mElement.x, mElement.y);
        double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        //deltaX和deltay平方和的二次方根 想象一个例子 1+1的平方根为1.4 （x²+y²）开根号
        //同理，当滑动的越快的话，deltaX+deltaY的值越大，这个越大的话，curDis也越大
        double curDis = Math.hypot(deltaX, deltaY);
        //我们求出的这个值越小，画的点或者是绘制椭圆形越多，这个值越大的话，绘制的越少，笔就越细，宽度越小
        double curVel = curDis * IPenConfig.DIS_VEL_CAL_FACTOR;
        double curWidth;
        //点的集合少，我们得必须改变宽度,每次点击的down的时候，这个事件
        if (mPointList.size() < 2) {
            if (mElement.tooltype == MotionEvent.TOOL_TYPE_STYLUS) {
                curWidth = mElement.pressure * mBaseWidth;
            } else {
                curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5,
                        mLastWidth);
            }
            curPoint.width = (float) curWidth;
            mBezier.init(mLastPoint, curPoint);
        } else {
            mLastVel = curVel;
            if (mElement.tooltype == MotionEvent.TOOL_TYPE_STYLUS) {
                curWidth = mElement.pressure * mBaseWidth;
            } else {
                //由于我们手机是触屏的手机，滑动的速度也不慢，所以，一般会走到这里来
                //阐明一点，当滑动的速度很快的时候，这个值就越小，越慢就越大，依靠着mlastWidth不断的变换
                curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5,
                        mLastWidth);
            }
            curPoint.width = (float) curWidth;
            mBezier.addNode(curPoint);
        }
        //每次移动的话，这里赋值新的值
        mLastWidth = curWidth;
        mPointList.add(curPoint);
        moveNeetToDo(curDis);
        mLastPoint = curPoint;
    }


    /**
     * 手指抬起来的事件
     * @param mElement
     * @param canvas
     */
    public void onUp(MotionElement mElement, Canvas canvas){

        mCurPoint = new ControllerPoint(mElement.x, mElement.y);
        double deltaX = mCurPoint.x - mLastPoint.x;
        double deltaY = mCurPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);
        //如果用笔画的画我的屏幕，记录他宽度的和压力值的乘，但是哇，这个是不会变的
        if (mElement.tooltype == MotionEvent.TOOL_TYPE_STYLUS) {
            mCurPoint.width = (float) (mElement.pressure * mBaseWidth);
        } else {
            mCurPoint.width = 0;
        }

        mPointList.add(mCurPoint);

        mBezier.addNode(mCurPoint);

        int steps = 1 + (int) curDis / IPenConfig.STEPFACTOR;
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
        //每次抬起手来，就把集合清空，在水彩笔的那个地方，如果啊，我说如果不清空的话，每次抬起手来，
        // 在onDown下去的话，最近画的线的透明度有改变，所以这里clear下线的集合
        clear();
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
    public double calcNewWidth(double curVel, double lastVel, double curDis,
                                double factor, double lastWidth) {
        double calVel = curVel * 0.6 + lastVel * (1 - 0.6);
        //返回指定数字的自然对数
        //手指滑动的越快，这个值越小，为负数
        double vfac = Math.log(factor * 2.0f) * (-calVel);
        //此方法返回值e，其中e是自然对数的基数。
        //Math.exp(vfac) 变化范围为0 到1 当手指没有滑动的时候 这个值为1 当滑动很快的时候无线趋近于0
        //在次说明下，当手指抬起来，这个值会变大，这也就说明，抬起手太慢的话，笔锋效果不太明显
        //这就说明为什么笔锋的效果不太明显
        double calWidth = mBaseWidth * Math.exp(vfac);
        //滑动的速度越快的话，mMoveThres也越大
        double mMoveThres = curDis * 0.01f;
        //对之值最大的地方进行控制
        if (mMoveThres > IPenConfig.WIDTH_THRES_MAX) {
            mMoveThres = IPenConfig.WIDTH_THRES_MAX;
        }
        //滑动的越快的话，第一个判断会走
        if (Math.abs(calWidth - mBaseWidth) / mBaseWidth > mMoveThres) {

            if (calWidth > mBaseWidth) {
                calWidth = mBaseWidth * (1 + mMoveThres);
            } else {
                calWidth = mBaseWidth * (1 - mMoveThres);
            }
            //滑动的越慢的话，第二个判断会走
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
     * event.getPressure(); //LCD可以感应出用户的手指压力，当然具体的级别由驱动和物理硬件决定的,我的手机上为1
     * @param motionEvent
     * @return
     */
    public MotionElement createMotionElement(MotionEvent motionEvent) {
        MotionElement motionElement = new MotionElement(motionEvent.getX(), motionEvent.getY(),
                motionEvent.getPressure(), motionEvent.getToolType(0));
        return motionElement;
    }

    public void clear() {
        mPointList.clear();
        mHWPointList.clear();
    }
    /**
     * 当现在的点和触摸点的位置在一起的时候不用去绘制
     * 但是这里也可以优化，当一直处于onDown事件的时候，其实这个方法一只在走
     * @param canvas
     * @param point
     * @param paint
     */
    // TODO: 2017/10/18  这里可以优化 当一直处于onDown事件的时候，其实这个方法一直在走，优化的点是，处于down事件，这里不需要走
    protected void drawToPoint(Canvas canvas, ControllerPoint point, Paint paint) {
        if ((mCurPoint.x == point.x) && (mCurPoint.y == point.y)) {
            return;
        }
        //水彩笔的效果和钢笔的不太一样，交给自己去实现
        doNeetToDo(canvas,point,paint);
    }

    /**
     * 判断笔是否为空 节约性能，每次切换笔的时候就不用重复设置了
     * @return
     */
    public boolean isNull() {
        return mPaint == null;
    }

    /**
     * 移动的时候，这里由于需要透明度的处理，交给子类
     * @param
     */
    protected abstract void moveNeetToDo(double f);

    /**
     * 这里交给子类，一个是绘制椭圆，一个是绘制bitmap
     * @param canvas
     * @param point
     * @param paint
     */
    protected abstract void doNeetToDo(Canvas canvas, ControllerPoint point, Paint paint);

    /**
     * 这里由于在设置笔的透明度，会导致整个线，或者说整个画布的的颜透明度随着整个笔的透明度而变化，
     * 所以在这里考虑是不是说，绘制毛笔的时候，每次都给它new 一个paint ，但是这里我还没有找到更好的办法
     *
     * @param canvas
     */
    // TODO: 2017/10/17  这个问题  待解决
    protected abstract void drawNeetToDo(Canvas canvas);
}
