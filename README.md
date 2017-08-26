# WritingPen
钢笔效果的实现
##不逼逼，看效果，感觉我的书法还阔以，哈哈


![image.png](http://upload-images.jianshu.io/upload_images/5363507-633fbf6aacb2c741.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](http://upload-images.jianshu.io/upload_images/5363507-8b622187caa4fca5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
####实现这个效果，大体用了40个小时，熬了3天夜，我未来的女朋友给我作证，看了无数的文档，在git上有个哥们用opengGl3.0实现比我这个更牛逼的效果，但是发现在低端手机上会报错，原因是不支持openGL3.0，导致Apk装入失败，1.0的api有看不懂，你说我能怎么办，我也很绝望啊！同时感觉opengl更加节手机性能，but我错了，在低端手机上使用opengl简直就是噩梦，卡的一逼，算了不提了，此功能的实现还是基于安卓的Piant，通过事件去绘制路径。

##1.创建DrawPenView类继承View

![image.png](http://upload-images.jianshu.io/upload_images/5363507-7094aaa5fa65811f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
####初始化笔,笔锋的效果，我个人尝试了使用三个笔，每次绘制的时候，三个笔一起绘制，根据手指的滑动速率的快慢去使其中的某个笔不用绘制，但是这个效果稀烂，所以view的还是用一只笔即可，
```
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#FF4081"));
        mPaint.setStrokeWidth(14);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//结束的笔画为圆心
        mPaint.setStrokeJoin(Paint.Join.ROUND);//连接处元
        mPaint.setAlpha(0xFF);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeMiter(1.0f);
```
####初始化bitmap，和画布，画布在这里主要是生成一张bitmap的
```
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

  private void initCanvas() {
        mCanvas = new Canvas(mBitmap);
        //设置画布的颜色的问题
        mCanvas.drawColor(Color.TRANSPARENT);
    }
```
####重写onDraw（）方法:由于项目需要，在这里我仅仅提供了两个方法：清除画布和绘制。扩展的功能有：返回上一步的绘制步骤，设置画笔的属性，mark笔，毛笔，钢笔，圆珠笔，铅笔等一切的控制都在这里进行
```
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
```
##2.认识MotionEvent对象
####当用户触摸屏幕时，将创建一个MontionEvent对象。MotionEvent包含了关于发生触摸的位置和时间的信息，以及触摸事件的其他细节。
```
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
                break;
            case MotionEvent.ACTION_MOVE:
                mVisualStrokePen.onMove(mVisualStrokePen.createMotionElement(event2));
                break;
            case MotionEvent.ACTION_UP:
                long time = System.currentTimeMillis();
                mVisualStrokePen.onUp(mVisualStrokePen.createMotionElement(event2),mCanvas);
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

```
####在这里我需要提到一个motionEvent的api：：：motionEvent.getToolType(0);返回的以下四种的值，TOOL_TYPE_UNKNOWN ：不知道什么画的
TOOL_TYPE_FINGER ：手指
TOOL_TYPE_STYLUS ：笔画的
TOOL_TYPE_MOUSE ：该工具是一个鼠标或触控板
TOOL_TYPE_ERASER ：工具是一块橡皮或一笔用于倒立的姿势
看见没，卧槽，以前都不知道，这个类知道我们用什么属性在写字，
event.getPressure(); //可以感应出用户的手指压力，当然具体的级别由驱动和物理硬件决定的,我的手机上为1
motionEvent.getEventTime()：事件发生的事件，在我此时的事件是shiming==8359650，而且是跟随着系统的时间而定
···

     /**
      * Tool type constant: Unknown tool type.
     * This constant is used when the tool type is not known or is not relevant,
     * such as for a trackball or other non-pointing device.
     *
     * @see #getToolType
     */
    public static final int TOOL_TYPE_UNKNOWN = 0;

    /**
     * Tool type constant: The tool is a finger.
     *
     * @see #getToolType
     */
    public static final int TOOL_TYPE_FINGER = 1;

    /**
     * Tool type constant: The tool is a stylus.
     *
     * @see #getToolType
     */
    public static final int TOOL_TYPE_STYLUS = 2;

    /**
     * Tool type constant: The tool is a mouse or trackpad.
     *
     * @see #getToolType
     */
    public static final int TOOL_TYPE_MOUSE = 3;

    /**
     * Tool type constant: The tool is an eraser or a stylus being used in an inverted posture.
     *
     * @see #getToolType
     */
    public static final int TOOL_TYPE_ERASER = 4:
####关于MotionElement 类：记录下五个参数：坐标x y，压力值，什么在屏幕上写的，还有事件发生的时间。
```
  public static class MotionElement {

        public float x;
        public float y;
        public float pressure;
        public int tooltype;
        public long timestamp;

        public MotionElement(float mx, float my, float mp, int ttype, long mt) {
            x = mx;
            y = my;
            pressure = mp;
            tooltype = ttype;
            timestamp = mt;
        }

    }
```
```
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
                motionEvent.getPressure(), motionEvent.getToolType(0),
                motionEvent.getEventTime());
        return motionElement;
    }
```
##3.清除画布
这个功能简单，把paint的模式设置为clear，使用我们新建的
canvas去drapaint这个笔，记得清除完了，要把mode设置为null
```
  /**
     *清除画布，记得清除点的集合
     */
    public void reset() {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(mPaint);
        mPaint.setXfermode(null);
        mVisualStrokePen.clear();
    }
```
##4.关于Bezier曲线
先发个图，嘿嘿，我自己手画的，看不清没关系，只需知道4个点的关系，想象一下曲线就行


![微信图片_20170826183403.jpg](http://upload-images.jianshu.io/upload_images/5363507-7dd06af8c72132e2.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](http://upload-images.jianshu.io/upload_images/5363507-df08ff751b79f650.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
####知道两点连接起来是直线，当我们不断的求出两个点的控制点，把无数的控制点绘制在一起就是一条完美的曲线，反正我这样子理解的，当然我在这里也做了一个width的控制，和这种的原理差不多。
```
  public void Init(float lastx, float lasty, float lastWidth, float x, float y, float width)
    {
        //资源点设置，最后的点的为资源点
        mSource.Set(lastx, lasty, lastWidth);
        float xmid = GetMid(lastx, x);
        float ymid = GetMid(lasty, y);
        float wmid = GetMid(lastWidth, width);
        //距离点为平均点
        mDestination.Set(xmid, ymid, wmid);
        //控制点为当前的距离点
        mControl.Set(GetMid(lastx,xmid),GetMid(lasty,ymid),GetMid(lastWidth,wmid));
        //下个控制点为当前点
        mNextControl.Set(x, y, width);
    }
 /**
     *
     * @param x1 一个点的x
     * @param x2 一个点的x
     * @return
     */
    private float GetMid(float x1, float x2) {
        return (float)((x1 + x2) / 2.0);
    }

    private double GetWidth(double w0, double w1, double t){
        return w0 + (w1 - w0) * t;
    }
```
####以上记得知道个步骤，才能方便理解，当这个点是我们资源点的时候，或者是当前点，那么它下一步就会成为一个新的资源点，需要不断的替换当前的起点和终点，那么才可以形成一个曲线
```
/**
     * 替换就的点，原来的距离点变换为资源点，控制点变为原来的下一个控制点，距离点取原来控制点的和新的的一半
     * 下个控制点为新的点
     * @param x 新的点的坐标
     * @param y 新的点的坐标
     * @param width
     */
    public void AddNode(float x, float y, float width){
        mSource.Set(mDestination);
        mControl.Set(mNextControl);
        mDestination.Set(GetMid(mNextControl.x, x), GetMid(mNextControl.y, y), GetMid(mNextControl.width, width));
        mNextControl.Set(x, y, width);
    }
```
####是不是看不懂，对，看不懂就对了，去下面看代码，记得在本子上多画几个点，想象一下这样变换的位置，然后就会明白了这真的是一个美妙的曲线，比女朋友还漂亮，哈哈，扯皮了
####关于手指抬起来的时候的方法: 结合手指抬起来的动作，告诉现在的曲线控制点也必须变化，其实在这里也不需要结合着up事件使用因为在down的事件中，所有点都会被重置，然后设置这个没有多少意义，但是可以改变下个事件的朝向改变先留着，因为后面如果需要控制整个颜色的改变的话，我的依靠这个方法，还有按压的时间的变化
```
  /**
     * 结合手指抬起来的动作，告诉现在的曲线控制点也必须变化，其实在这里也不需要结合着up事件使用
     * 因为在down的事件中，所有点都会被重置，然后设置这个没有多少意义，但是可以改变下个事件的朝向改变
     * 先留着，因为后面如果需要控制整个颜色的改变的话，我的依靠这个方法，还有按压的时间的变化
     */
    public void End() {
        mSource.Set(mDestination);
        float x = GetMid(mNextControl.x, mSource.x);
        float y = GetMid(mNextControl.y, mSource.y);
        float w = GetMid(mNextControl.width, mSource.width);
        mControl.Set(x, y, w);
        mDestination.Set(mNextControl);
    }
```
####还有个方法：我的提一句，是不是想一个一元二次的方程，哈哈！这个不是我写的，这个是基于git上开源的写的，是不是有点高中数学的影响了，哈哈，对就是这样的，

![image.png](http://upload-images.jianshu.io/upload_images/5363507-502f1a6068d1fc73.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

     * 三阶曲线的控制点
     * @param p0
     * @param p1
     * @param p2
     * @param t
     * @return
     */
    private double GetValue(double p0, double p1, double p2, double t){
        double A = p2 - 2 * p1 + p0;
        double B = 2 * (p1 - p0);
        double C = p0;
        return A * t * t + B * t + C;
    }

##5.关于pen，这个类才是所有的关键，这里需要在view中的onDraw中调用，本来我开始是想说能不能再一开始的时候，down事件的时候，给他画个园，但是这个园的半径我控制不好，所以在代码中我留下这个问题，以后需要做更难的效果的时候，我来把这个开始的步骤补上。
```
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
```
####down事件处理
```
     * 手指的down事件
     * @param mElement
     */
    public void onDown(MotionElement mElement) {
        mPaint.setXfermode(null);
        mPath = new Path();
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
        //绘制起点
        mPath.moveTo(mElement.x, mElement.y);
    }
···
```


####move事件的处理
```
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
            mBezier.Init(mLastPoint, curPoint);
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
            mBezier.AddNode(curPoint);
        }
        //每次移动的话，这里赋值新的值
        mLastWidth = curWidth;

        mPointList.add(curPoint);

        int steps = 1 + (int) curDis / STEPFACTOR;
        System.out.println("shiming-- steps"+steps);
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.GetPoint(t);
            mHWPointList.add(point);
        }

        mPath.quadTo(mLastPoint.x, mLastPoint.y,
                (mElement.x + mLastPoint.x) / 2,
                (mElement.y + mLastPoint.y) / 2);

        mLastPoint = curPoint;
    }
```

##up事件的处理:当需要关心我们画的这个bitmap的时候，记得在up结束的时候，需要把这个绘制的东西需要重新绘制到我们自定义View的画布上，这个画笔是自己定义的，而不是View里面onDraw(cavns)里面的画布

```
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

        mBezier.AddNode(curPoint);

        int steps = 1 + (int) curDis / STEPFACTOR;
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.GetPoint(t);
            mHWPointList.add(point);
        }
        //
        mBezier.End();
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.GetPoint(t);
            mHWPointList.add(point);
        }

        mPath.quadTo(mLastPoint.x, mLastPoint.y,
                (mElement.x + mLastPoint.x) / 2,
                (mElement.y + mLastPoint.y) / 2);
        mPath.lineTo(mElement.x, mElement.y);
       // 手指up 我画到纸上上
        draw(canvas);

    }
```
####其实这里才是关键的地方，通过画布画椭圆，每一个点都是一个椭圆，这个椭圆的所有细节，逐渐构建出一个完美的笔尖 和笔锋的效果,我觉得在这里需要大量的测试，其实就对低端手机进行排查，看我们绘制的笔的宽度是多少，绘制多少个椭圆然后在低端手机上不会那么卡，当然你哪一个N年前的手机给我，那也的卡，只不过需要适中的范围里面
```
   private void drawLine(Canvas canvas, double x0, double y0, double w0, double x1, double y1, double w1, Paint paint){
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
            RectF oval = new RectF();
            oval.set((float)(x-w/4.0f), (float)(y-w/2.0f), (float)(x+w/4.0f), (float)(y+w/2.0f));
            //最基本的实现，通过点控制线，绘制椭圆
            canvas.drawOval(oval, paint);
            x+=deltaX;
            y+=deltaY;
            w+=deltaW;
        }
    }
```
##最后来张自画像,可以，帅的一比！

![image.png](http://upload-images.jianshu.io/upload_images/5363507-e5217194937430b4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##git地址，tks！
https://github.com/Shimingli/WritingPen