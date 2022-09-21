package com.shiming.pen.new_code;

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

import com.shiming.pen.field_character.DrawPenView;

import static com.shiming.pen.new_code.IPenConfig.PEN_WIDTH;


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 * @des DrawPenView实现手写关键类，目前只提供了，手绘的功能和清除画布，后期根据业务逻辑可以动态的设置方法
 */
public class NewDrawPenView extends View {
    private static final String TAG = "DrawPenView";
    private Paint mPaint;//画笔
    private Canvas mCanvas;//画布
    private Bitmap mBitmap;
    private Context mContext;
    public static int mCanvasCode = IPenConfig.STROKE_TYPE_PEN;
    private BasePenExtend mStokeBrushPen;
    private boolean mIsCanvasDraw;
    private int mPenconfig;

    public NewDrawPenView(Context context) {
        super(context);
        initParameter(context);
    }

    public NewDrawPenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParameter(context);
    }

    public NewDrawPenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParameter(context);
    }

    private void initParameter(Context context) {
        mContext = context;
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mBitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        mStokeBrushPen = new SteelPen(context);
        initPaint();
        initCanvas();
    }


    private void initPaint() {
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
    }

    private void initCanvas() {
        mCanvas = new Canvas(mBitmap);
        //设置画布的颜色的问题
        mCanvas.drawColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        switch (mCanvasCode) {
            case IPenConfig.STROKE_TYPE_PEN:
            case IPenConfig.STROKE_TYPE_BRUSH:
                mStokeBrushPen.draw(canvas);
                break;
            case IPenConfig.STROKE_TYPE_ERASER:
                reset();
                break;
            default:
                Log.e(TAG, "onDraw" + Integer.toString(mCanvasCode));
                break;
        }
        super.onDraw(canvas);
    }


    public void setCanvasCode(int canvasCode) {
        mCanvasCode = canvasCode;
        switch (mCanvasCode) {
            case IPenConfig.STROKE_TYPE_PEN:
                mStokeBrushPen = new SteelPen(mContext);
                break;
            case IPenConfig.STROKE_TYPE_BRUSH:
                mStokeBrushPen = new BrushPen(mContext);
                break;

        }
        //设置
        if (mStokeBrushPen.isNull()) {
            mStokeBrushPen.setPaint(mPaint);
        }
        invalidate();
    }

    /**
     * event.getAction() //获取触控动作比如ACTION_DOWN
     * event.getPointerCount(); //获取触控点的数量，比如2则可能是两个手指同时按压屏幕
     * event.getPointerId(nID); //对于每个触控的点的细节，我们可以通过一个循环执行getPointerId方法获取索引
     * event.getX(nID); //获取第nID个触控点的x位置,记录的第一个点为getX，getY
     * event.getY(nID); //获取第nID个点触控的y位置
     * event.getPressure(nID); //LCD可以感应出用户的手指压力，当然具体的级别由驱动和物理硬件决定的
     * event.getDownTime() //按下开始时间
     * event.getEventTime() // 事件结束时间
     * event.getEventTime()-event.getDownTime()); //总共按下时花费时间
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mIsCanvasDraw = true;
        MotionEvent event2 = MotionEvent.obtain(event);
        mStokeBrushPen.onTouchEvent(event2, mCanvas);
        //event会被下一次事件重用，这里必须生成新的，否则会有问题
        //getActionMask:触摸的动作,按下，抬起，滑动，多点按下，多点抬起
        switch (event2.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mGetTimeListner != null)
                    mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mGetTimeListner != null)
                    mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_UP:
                long time = System.currentTimeMillis();
                if (mGetTimeListner != null)
                    mGetTimeListner.getTime(time);
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    /**
     * @return 判断是否有绘制内容在画布上
     */
    public boolean getHasDraw() {
        return mIsCanvasDraw;
    }

    /**
     * 清除画布，记得清除点的集合
     */
    public void reset() {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(mPaint);
        mPaint.setXfermode(null);
        mIsCanvasDraw = false;
        mStokeBrushPen.clear();
        //这里处理的不太好 需要优化
        mCanvasCode = mPenconfig;

    }

    public TimeListener mGetTimeListner;

    public void setGetTimeListener(TimeListener l) {
        mGetTimeListner = l;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setPenconfig(int penconfig) {
        mPenconfig = penconfig;

    }

    public interface TimeListener {
        void getTime(long l);

        void stopTime();
    }

    private int mBackColor = Color.TRANSPARENT;

    /**
     * 逐行扫描 清楚边界空白。功能是生成一张bitmap位于正中间，不是位于顶部，此关键的是我们画布需要
     * 成透明色才能生效
     *
     * @param blank 边距留多少个像素
     * @return tks github E-signature
     */
    public Bitmap clearBlank(int blank) {
        if (mBitmap != null) {
            int HEIGHT = mBitmap.getHeight();//1794
            int WIDTH = mBitmap.getWidth();//1080
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
}
