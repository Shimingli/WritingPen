package com.shiming.pen;

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


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 * @des DrawPenView实现手写关键类，目前只提供了，手绘的功能和清除画布，后期根据业务逻辑可以动态的设置方法
 */
public class DrawPenView extends View {
    private static final String TAG = "DrawPenView";
    private Paint mPaint;//画笔
    private Canvas mCanvas;//画布
    private Bitmap mBitmap;
    public static final int CANVAS_NORMAL = 0;
    public static final int CANVAS_RESET = 1;//全部清除
    private VisualStrokePen mVisualStrokePen;
    private Context mContext;
    public static  int mCanvasCode=CANVAS_NORMAL;

    public DrawPenView(Context context) {
        super(context);
        initParameter(context);
    }

    public DrawPenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParameter(context);
    }

    public DrawPenView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mVisualStrokePen=new VisualStrokePen(mContext);
        initPaint(mContext);
        initCanvas();
    }

    //**将rgb色彩值转成16进制代码**
    public String convertRGBToHex(int r, int g, int b) {
        String rFString, rSString, gFString, gSString,
                bFString, bSString, result;
        int red, green, blue;
        int rred, rgreen, rblue;
        red = r / 16;
        rred = r % 16;
        if (red == 10)
            rFString = "A";
        else if (red == 11)
            rFString = "B";
        else if (red == 12)
            rFString = "C";
        else if (red == 13)
            rFString = "D";
        else if (red == 14)
            rFString = "E";
        else if (red == 15)
            rFString = "F";
        else
            rFString = String.valueOf(red);

        if (rred == 10)
            rSString = "A";
        else if (rred == 11)
            rSString = "B";
        else if (rred == 12)
            rSString = "C";
        else if (rred == 13)
            rSString = "D";
        else if (rred == 14)
            rSString = "E";
        else if (rred == 15)
            rSString = "F";
        else
            rSString = String.valueOf(rred);

        rFString = rFString + rSString;

        green = g / 16;
        rgreen = g % 16;

        if (green == 10)
            gFString = "A";
        else if (green == 11)
            gFString = "B";
        else if (green == 12)
            gFString = "C";
        else if (green == 13)
            gFString = "D";
        else if (green == 14)
            gFString = "E";
        else if (green == 15)
            gFString = "F";
        else
            gFString = String.valueOf(green);

        if (rgreen == 10)
            gSString = "A";
        else if (rgreen == 11)
            gSString = "B";
        else if (rgreen == 12)
            gSString = "C";
        else if (rgreen == 13)
            gSString = "D";
        else if (rgreen == 14)
            gSString = "E";
        else if (rgreen == 15)
            gSString = "F";
        else
            gSString = String.valueOf(rgreen);

        gFString = gFString + gSString;

        blue = b / 16;
        rblue = b % 16;

        if (blue == 10)
            bFString = "A";
        else if (blue == 11)
            bFString = "B";
        else if (blue == 12)
            bFString = "C";
        else if (blue == 13)
            bFString = "D";
        else if (blue == 14)
            bFString = "E";
        else if (blue == 15)
            bFString = "F";
        else
            bFString = String.valueOf(blue);

        if (rblue == 10)
            bSString = "A";
        else if (rblue == 11)
            bSString = "B";
        else if (rblue == 12)
            bSString = "C";
        else if (rblue == 13)
            bSString = "D";
        else if (rblue == 14)
            bSString = "E";
        else if (rblue == 15)
            bSString = "F";
        else
            bSString = String.valueOf(rblue);
        bFString = bFString + bSString;
        result = "#" + rFString + gFString + bFString;
        return result;
    }

    private void initPaint(Context context) {

        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#FF4081"));
        mPaint.setStrokeWidth(14);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//结束的笔画为圆心
        mPaint.setStrokeJoin(Paint.Join.ROUND);//连接处元
        mPaint.setAlpha(0xFF);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeMiter(1.0f);

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
            case CANVAS_NORMAL:
                mVisualStrokePen.draw(canvas);
                break;
            case CANVAS_RESET:
                reset();
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
        //测试过程中，当使用到event的时候，产生了没有收到事件的问题，所以在这里需要obtian的一下
        MotionEvent event2 = MotionEvent.obtain(event);
        switch (event2.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                setCanvasCode(CANVAS_NORMAL);
                mVisualStrokePen.onDown(mVisualStrokePen.createMotionElement(event2));
//                mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_MOVE:
                mVisualStrokePen.onMove(mVisualStrokePen.createMotionElement(event2));
//                mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_UP:
                long time = System.currentTimeMillis();
//                mGetTimeListner.getTime(time);
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

    private int mBackColor = Color.TRANSPARENT;

    /**
     * 逐行扫描 清楚边界空白。功能是生成一张bitmap位于正中间，不是位于顶部，此关键的是我们画布需要
     * 成透明色才能生效
     * @param blank 边距留多少个像素
     * @return tks github E-signature
     */
    public Bitmap clearBlank(int blank) {
        if (mBitmap != null) {
            int HEIGHT = mBitmap.getHeight();
            int WIDTH = mBitmap.getWidth();
            int top = 0, left = 0, right = 0, bottom = 0;
            int[] pixs = new int[WIDTH];
            boolean isStop;
            for (int y = 0; y < HEIGHT; y++) {
                mBitmap.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {

                        top = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int y = HEIGHT - 1; y >= 0; y--) {
                mBitmap.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        bottom = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            pixs = new int[HEIGHT];
            for (int x = 0; x < WIDTH; x++) {
                mBitmap.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        left = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int x = WIDTH - 1; x > 0; x--) {
                mBitmap.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        right = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            if (blank < 0) {
                blank = 0;
            }
            left = left - blank > 0 ? left - blank : 0;
            top = top - blank > 0 ? top - blank : 0;
            right = right + blank > WIDTH - 1 ? WIDTH - 1 : right + blank;
            bottom = bottom + blank > HEIGHT - 1 ? HEIGHT - 1 : bottom + blank;
            return Bitmap.createBitmap(mBitmap, left, top, right - left, bottom - top);
        } else {
            return null;
        }
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
