package com.shiming.pen.new_code;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.shiming.pen.R;
import com.shiming.pen.old_code.ControllerPoint;


/**
 * @author shiming
 * @version v1.0 create at 2017/10/10
 * @des 水彩笔
 */
public class BrushPen extends BasePenExtend {

    private Bitmap mBitmap;
    //第一个Rect 代表要绘制的bitmap 区域，
    protected Rect mOldRect = new Rect();
    //第二个 Rect 代表的是要将bitmap 绘制在屏幕的什么地方
    protected RectF mNeedDrawRect = new RectF();
    protected Bitmap mOriginBitmap;

    public BrushPen(Context context) {
        super(context);
        initTexture();
    }
    /**
     * 由于需要画笔piant中的一些信息，就不能让paint为null，所以setBitmap需要有paint的时候设置
     * @param paint
     */
    @Override
    public void setPaint(Paint paint) {
        super.setPaint(paint);
        setBitmap(mOriginBitmap);
    }

    /**
     * 感谢公司的ui大哥  小伍哥 免费给的切图
     * R.mipmap.tranglie 设置的时候有点像三角形的笔锋
     * R.mipmap.cicrle    圆形的笔锋效果
     * R.mipmap.six        六边形有点怪怪的，可以测试一下
     * R.drawable.brush  这个才是用起来比较舒服，如果你的笔锋要很尖的话，叫ui爸爸给你裁剪这种图 越尖越好
     */
    private void initTexture() {
        //通过资源文件生成的原始的bitmap区域 后面的资源图有些更加有意识的东西
        mOriginBitmap = BitmapFactory.decodeResource(
                mContext.getResources(), R.mipmap.brush);
    }

    /**
     * 主要是得到需要绘制的rect的区域
     * @param bitmap
     */
    private void setBitmap(Bitmap bitmap) {
        Canvas canvas = new Canvas();
        mBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        //用指定的方式填充位图的像素。
        mBitmap.eraseColor(Color.rgb(Color.red(mPaint.getColor()),
                Color.green(mPaint.getColor()), Color.blue(mPaint.getColor())));
        //用画布制定位图绘制
        canvas.setBitmap(mBitmap);
        Paint paint = new Paint();
        // 设置混合模式   （只在源图像和目标图像相交的地方绘制目标图像）
        //最常见的应用就是蒙板绘制，利用源图作为蒙板“抠出”目标图上的图像。
        //如果把这行代码注释掉这里生成的东西更加有意思
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        //src 代表需要绘制的区域
        mOldRect.set(0, 0, mBitmap.getWidth()/4, mBitmap.getHeight()/4);
    }



    /**
     * 更具笔的宽度的变化，笔的透明度要和发生变化
     * @param point
     * @return
     */
    private ControllerPoint getWithPointAlphaPoint(ControllerPoint point) {
        ControllerPoint nPoint = new ControllerPoint();
        nPoint.x = point.x;
        nPoint.y = point.y;
        nPoint.width = point.width;
        int alpha = (int) (255 * point.width / mBaseWidth / 2);
        if (alpha < 10) {
            alpha = 10;
        } else if (alpha > 255) {
            alpha = 255;
        }
        nPoint.alpha = alpha;
        return nPoint;
    }


    @Override
    protected void doNeetToDo(Canvas canvas, ControllerPoint point, Paint paint) {
        drawLine(canvas, mCurPoint.x, mCurPoint.y, mCurPoint.width,
                mCurPoint.alpha, point.x, point.y, point.width, point.alpha,
                paint);
    }
    protected void drawLine(Canvas canvas, double x0, double y0, double w0,
                            int a0, double x1, double y1, double w1, int a1, Paint paint) {
        double curDis = Math.hypot(x0 - x1, y0 - y1);
        int factor = 2;
        if (paint.getStrokeWidth() < 6) {
            factor = 1;
        } else if (paint.getStrokeWidth() > 60) {
            factor = 3;
        }
        int steps = 1 + (int) (curDis / factor);
        double deltaX = (x1 - x0) / steps;
        double deltaY = (y1 - y0) / steps;
        double deltaW = (w1 - w0) / steps;
        double deltaA = (a1 - a0) / steps;
        double x = x0;
        double y = y0;
        double w = w0;
        double a = a0;

        for (int i = 0; i < steps; i++) {
            if (w < 1.5)
                w = 1.5;
            //根据点的信息计算出需要把bitmap绘制在什么地方
            mNeedDrawRect.set((float) (x - w / 2.0f), (float) (y - w / 2.0f),
                    (float) (x + w / 2.0f), (float) (y + w / 2.0f));
            //每次到这里来的话，这个笔的透明度就会发生改变，但是呢，这个笔不用同一个的话，有点麻烦
            //我在这里做了个不是办法的办法，每次呢？我都从新new了一个新的笔，每次循环就new一个，内存就有很多的笔了
            //这里new 新的笔  我放到外面去做了
            //Paint newPaint = new Paint(paint);
            //当这里很小的时候，透明度就会很小，个人测试在3.0左右比较靠谱
            paint.setAlpha((int) (a / 3.0f));
            //第一个Rect 代表要绘制的bitmap 区域，第二个 Rect 代表的是要将bitmap 绘制在屏幕的什么地方
            canvas.drawBitmap(mBitmap, mOldRect, mNeedDrawRect, paint);
            x += deltaX;
            y += deltaY;
            w += deltaW;
            a += deltaA;
        }
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
        //水彩笔的效果
        int steps = 1 + (int) curDis / IPenConfig.STEPFACTOR;
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            point = getWithPointAlphaPoint(point);
            mHWPointList.add(point);
        }

    }
    //对每个笔设置了透明度 如果这里不设置一个新的笔的话，每次down事件发生了，就会把一起的绘制完成的东西，透明度也发生改变，
    //这里还有想到更好的方法，
    // TODO: 2017/10/18
    //虽然这样设置了，但是还是有问题，每次down的时候，虽然是一根新的笔，但是原来的笔始终有点小小的问题
    @Override
    protected Paint getNewPaint(Paint paint) {
        return new Paint(paint);
    }
}
