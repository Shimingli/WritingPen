package com.shiming.pen.field_character;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.shiming.pen.old_code.StrokePen;


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 * @des DrawPenView实现手写关键类，目前只提供了，手绘的功能和清除画布，后期根据业务逻辑可以动态的设置方法
 */
public class DrawPenView extends View  {
    private static final String TAG = "DrawPenView";
    private Paint mPaint;//画笔
    private Canvas mCanvas;//画布
    private Bitmap mBitmap;
    public static final int CANVAS_NORMAL = 0;
    public static final int CANVAS_RESET = 1;//全部清除
    private StrokePen mVisualStrokePen;
    private Context mContext;
    public static  int mCanvasCode=CANVAS_NORMAL;
    private String mPaintColor;
    private int mSize;
    private boolean mIsCanvasDraw;

    public DrawPenView(Context context) {
        this(context,null);
    }

    public DrawPenView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
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
        int i = dp2px(mContext, 40);
        int i1 = dp2px(mContext,280);
        mBitmap = Bitmap.createBitmap(dm.widthPixels-i-i, i1, Bitmap.Config.ARGB_8888);
        mVisualStrokePen=new StrokePen(mContext);
        initPaint();
        initCanvas();
    }
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    private void initPaint() {
        mSize = 65;
        mPaintColor="#3B3635";
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor(mPaintColor));
        mPaint.setStrokeWidth(mSize);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//结束的笔画为圆心
        mPaint.setStrokeJoin(Paint.Join.ROUND);//连接处元
        mPaint.setAlpha(0xFF);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeMiter(1.0f);
        mPaint.setFilterBitmap(true);
        mVisualStrokePen.setPaint(mPaint);
    }
    private void initCanvas() {
        mCanvas = new Canvas(mBitmap);
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        //设置画布的颜色的问题
        mCanvas.drawColor(Color.TRANSPARENT);
    }

    public void changePaintColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * 当设置完成了，需要告诉其他的控件我们的笔的宽度发生了改变
     * @param paintColor 颜色
     * @param width 宽度
     */
    public void changePaintSize(String paintColor, float width) {
        mPaint.setStrokeWidth(width);
        mPaint.setColor(Color.parseColor(paintColor));
        mVisualStrokePen.setPaint(mPaint);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mIsCanvasDraw = true;
        //测试过程中，当使用到event的时候，产生了没有收到事件的问题，所以在这里需要obtian的一下
        MotionEvent event2 = MotionEvent.obtain(event);
        switch (event2.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //每次都是这个笔，因为项目里面就只有这个笔，如果多了，这里需要改动
                setCanvasCode(CANVAS_NORMAL);
                mVisualStrokePen.onDown(mVisualStrokePen.createMotionElement(event2));
                mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_MOVE:
                mVisualStrokePen.onMove(mVisualStrokePen.createMotionElement(event2));
                mGetTimeListner.stopTime();
                break;
            case MotionEvent.ACTION_UP:
                long time = System.currentTimeMillis();
                mGetTimeListner.getTime(time);
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
        mIsCanvasDraw=false;
        mVisualStrokePen.clear();
    }

    /**
     *
     * @return 判断是否有绘制内容在画布上
     */
    public boolean getHasDraw(){
        return mIsCanvasDraw;
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
    public Bitmap getBitmap(){
        return mBitmap;
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
