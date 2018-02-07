package com.shiming.pen.field_character;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.shiming.pen.R;
import static com.shiming.pen.field_character.DrawPenView.CANVAS_RESET;


/**
 * @author shiming
 * @version v1.0 create at 2017/8/24
 * @des DrawViewLayout的一些封装  后续优化的点是：页面不初始化，尽量等着用户来选择
 */
public class DrawViewLayout extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {

    private RelativeLayout mShowKeyboard;
    private RelativeLayout mGotoPreviousStep;
    private RelativeLayout mClearCanvas;
    private DrawPenView mDrawView;
    private RelativeLayout mSaveBitmap;
    private ViewStub mViewStub;
    private View mChild;
    private Context mContext;
    private ImageView mUpOrDownIcon;
    private LayoutInflater mInflater;
    private boolean mIsShowKeyB;

    public DrawViewLayout(@NonNull Context context) {
        this(context, null);
    }


    public DrawViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();

    }
    private void initView() {
        mInflater = LayoutInflater.from(getContext());
        mChild = mInflater.inflate(R.layout.brush_weight_layout, this, false);
        addView(mChild);
        mShowKeyboard = (RelativeLayout) findViewById(R.id.rll_show_keyb_container);
        mGotoPreviousStep = (RelativeLayout) findViewById(R.id.rll_show_space_container);//空格
        mClearCanvas = (RelativeLayout) findViewById(R.id.rll_show_newline_container);
        mSaveBitmap = (RelativeLayout) findViewById(R.id.rll_show_delete_container);
        mViewStub = (ViewStub) findViewById(R.id.draw_view);
        //需要关心的selector的id
        mUpOrDownIcon = (ImageView) findViewById(R.id.rll_show_keyb_container_icon);
        setOnClickListenerT();
    }


    private void setOnClickListenerT() {
        mShowKeyboard.setOnClickListener(this);
        mGotoPreviousStep.setOnClickListener(this);
        mClearCanvas.setOnClickListener(this);
        mSaveBitmap.setOnClickListener(this);
        mSaveBitmap.setOnLongClickListener(this);
       // showOrHideMySurfaceView();

    }

    private void setDrawViewConfig() {
        mDrawView = (DrawPenView) findViewById(R.id.myglsurface_view);
        mDrawView.setCurrentState(DrawPenView.CANVAS_NORMAL);
        mDrawView.setGetTimeListener(new DrawPenView.TimeListener() {
            @Override
            public void getTime(long l) {
                mIActionCallback.getUptime(l);
            }

            @Override
            public void stopTime() {
                mIActionCallback.stopTime();
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rll_show_keyb_container:
                Toast.makeText(mContext,"隐藏手写键盘",Toast.LENGTH_SHORT).show();
                showOrHideMySurfaceView();
                break;
            case R.id.rll_show_space_container:
                Toast.makeText(mContext,"增加空格",Toast.LENGTH_SHORT).show();
                mIActionCallback.needSpace();
                break;
            case R.id.rll_show_newline_container:
                Toast.makeText(mContext,"换行",Toast.LENGTH_SHORT).show();
                mIActionCallback.creatNewLine();
                break;
            case R.id.rll_show_delete_container:
                Toast.makeText(mContext,"删除，长按删除",Toast.LENGTH_SHORT).show();
                mIActionCallback.deleteOnClick();
                break;
        }
    }
    /**
     * 使用Viewstub的在不需要弹出键盘的时候，渲染不占内存不
     */
    private void showOrHideMySurfaceView() {
        if (mViewStub.getParent() != null) {
            mViewStub.inflate();
        }
        if (mDrawView == null) {
            setDrawViewConfig();
        }
        if (mDrawView.getVisibility() == GONE) {
            mIsShowKeyB=true;
            mViewStub.setVisibility(VISIBLE);
            mUpOrDownIcon.setSelected(true);
        //    mIActionCallback.showkeyB(true);
            mDrawView.setVisibility(VISIBLE);
        } else if (mDrawView.getVisibility() == VISIBLE) {
            mIsShowKeyB=false;
          //  mIActionCallback.showkeyB(false);
            mDrawView.setVisibility(GONE);
            mViewStub.setVisibility(GONE);
            mUpOrDownIcon.setSelected(false);
        }

    }


    public void clearScreen() {
        if (mDrawView==null)return;
        mDrawView.setCanvasCode(CANVAS_RESET);
    }



    /**
     * 必须显示键盘，必须哦
     *
     */
    public void showBk() {
        if (!getIsShowKeyB()){
            if (mViewStub.getParent() != null) {
                mViewStub.inflate();
            }
            if (mDrawView == null) {
                setDrawViewConfig();
            }
            mIsShowKeyB=true;
            mViewStub.setVisibility(VISIBLE);
            mUpOrDownIcon.setSelected(true);
            mIActionCallback.showkeyB(true);
            mDrawView.setVisibility(VISIBLE);
        }
    }


    public IActionCallback mIActionCallback;

    public void setActionCallback(IActionCallback a) {
        mIActionCallback = a;
    }

    public boolean getIsShowKeyB() {
        return mIsShowKeyB;
    }


    /**
     *  长按事件的启动定时器
     * @param v
     * @return
     */
    @Override
    public boolean onLongClick(View v) {
        Executor.INSTANCE.setCallback(mIActionCallback);
        Executor.INSTANCE.upData(v.getId());
        return true;
    }

    public DrawPenView getSaveBitmap() {
        return mDrawView;
    }

    public interface IActionCallback {


        void creatNewLine();

        void getUptime(long l);

        void stopTime();

        void needSpace();


        void deleteOnClick();

        void showkeyB(boolean flag);

        void deleteOnLongClick();

    }
}