package com.shiming.pen.old_code;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.shiming.pen.new_code.IPenConfig;
import com.shiming.pen.new_code.BrushPen;

import static com.shiming.pen.new_code.IPenConfig.PEN_WIDTH;


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 * @des DrawPenView实现手写关键类，目前只提供了，手绘的功能和清除画布，后期根据业务逻辑可以动态的设置方法
 */
public class OldDrawPenView extends View {
    private static final String TAG = "DrawPenView";
    private Paint mPaint;//画笔
    private Canvas mCanvas;//画布
    private Bitmap mBitmap;
    private StrokePen mVisualStrokePen;
    private Context mContext;
    public static  int mCanvasCode= IPenConfig.STROKE_TYPE_PEN;
    private BrushPen mStokeBrushPen;

    public OldDrawPenView(Context context) {
        super(context);
        initParameter(context);
    }

    public OldDrawPenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParameter(context);
    }

    public OldDrawPenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParameter(context);
    }

    public void setCanvasCode(int canvasCode) {
        mCanvasCode = canvasCode;
        invalidate();
    }

    private void initParameter(Context context) {
        mContext = context;
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mBitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        //笔的控制类
        mVisualStrokePen=new StrokePen(mContext);
        initPaint(mContext);
        initCanvas();
    }


    private void initPaint(Context context) {
        mPaint = new Paint();
        mPaint.setColor(IPenConfig.PEN_CORLOUR);
        mPaint.setStrokeWidth(PEN_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//结束的笔画为圆心
        mPaint.setStrokeJoin(Paint.Join.ROUND);//连接处元
        mPaint.setAlpha(0xFF);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeMiter(1.0f);
        mStokeBrushPen.setPaint(mPaint);
        mVisualStrokePen.setPaint(mPaint);


    }
    private void initCanvas() {
        mCanvas = new Canvas(mBitmap);
        //设置画布的颜色的问题
        mCanvas.drawColor(Color.TRANSPARENT);
    }

    public void changePaintColor(int color) {
        mPaint.setColor(color);
    }

    public void changePaintSize(float width) {
        mPaint.setStrokeWidth(width);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        switch (mCanvasCode) {
            case IPenConfig.STROKE_TYPE_PEN:
                mVisualStrokePen.draw(canvas);
                break;
            case IPenConfig.STROKE_TYPE_ERASER:
                reset();
                break;
            case IPenConfig.STROKE_TYPE_BRUSH:
                mStokeBrushPen.draw(canvas);
                break;
            default:
                Log.e(TAG, "onDraw" + Integer.toString(mCanvasCode));
                break;
        }
        super.onDraw(canvas);
    }

    /**
     event.getAction() //获取触控动作比如ACTION_DOWN
     event.getPointerCount(); //获取触控点的数量，比如2则可能是两个手指同时按压屏幕
     event.getPointerId(nID); //对于每个触控的点的细节，我们可以通过一个循环执行getPointerId方法获取索引
     event.getX(nID); //获取第nID个触控点的x位置,记录的第一个点为getX，getY
     event.getY(nID); //获取第nID个点触控的y位置
     event.getPressure(nID); //LCD可以感应出用户的手指压力，当然具体的级别由驱动和物理硬件决定的
     event.getDownTime() //按下开始时间
     event.getEventTime() // 事件结束时间
     event.getEventTime()-event.getDownTime()); //总共按下时花费时间
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //event会被下一次事件重用，这里必须生成新的，否则会有问题
        MotionEvent event2 = MotionEvent.obtain(event);
        //getActionMask:触摸的动作,按下，抬起，滑动，多点按下，多点抬起
        switch (event2.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                setCanvasCode(IPenConfig.STROKE_TYPE_PEN);
                mVisualStrokePen.onDown(mVisualStrokePen.createMotionElement(event2));
                break;
            case MotionEvent.ACTION_MOVE:
                mVisualStrokePen.onMove(mVisualStrokePen.createMotionElement(event2));
                break;
            case MotionEvent.ACTION_UP:
               mVisualStrokePen.onUp(mVisualStrokePen.createMotionElement(event2),mCanvas);
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    /**
     *清除画布，记得清除点的集合
     */
    public void reset() {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(mPaint);
        mPaint.setXfermode(null);
        mVisualStrokePen.clear();
    }

    public void setCurrentState(int currentState) {
        mCanvasCode = currentState;
    }


    public Bitmap getBitmap(){
        return mBitmap;
    }

    public void onResume() {

    }
    public TimeListener mGetTimeListner;

    public void setGetTimeListener(TimeListener l) {
        mGetTimeListner = l;
    }

    public interface TimeListener {
        void getTime(long l);

        void stopTime();
    }
}
