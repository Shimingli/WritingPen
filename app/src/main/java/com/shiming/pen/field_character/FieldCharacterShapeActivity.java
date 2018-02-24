package com.shiming.pen.field_character;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.shiming.pen.R;
import com.shiming.pen.new_code.IPenConfig;
import com.shiming.pen.new_code.NewDrawPenView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;


/**
 * author： Created by shiming on 2018/1/20 15:08
 * mailbox：lamshiming@sina.com
 * 田字格的Demo
 */
public class FieldCharacterShapeActivity extends AppCompatActivity implements DrawViewLayout.IActionCallback, View.OnClickListener {
    private DrawViewLayout mDrawViewLayout;
    private Bitmap mBitmap;
    private Bitmap mBitmapResize;
    private HandRichTextEditor mRetContent;
    private long mOldTime;
    /**
     * 数据库Id
     */
    private long draftId = 0L;
    /**
     * 图片命名
     */
    private static String full_name = "";
    private static final String LAST_NAME = "word_";
    /**
     * 文件保存的路径
     */
    private String mPath = null;
    private boolean mIsCreateBitmap = false;
    private Bitmap mCreatBimap;
    /**
     * 自动保存Timer
     */
    private Timer mTimerSave;
    /**
     * add shiming  手写体的生成图片的时间
     */
    public static final int HADN_DRAW_TIME = 700;
    public static final String FONT_NAME_HEAD = "[font]";
    public static final String FONT_NAME_TAIL = "[/font]";
    public static int mAllHandDrawSize;
    public static int mEmotionSize;
    private Button mChangePen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_character_shape_layout);
        //这里两个值关系到手写的所有的一切
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        mAllHandDrawSize = (int) (37.0 * dm.density);
        mEmotionSize = (int)(dm.density * 27.0);
        findViews();
        mDrawViewLayout.setActionCallback(this);
        mDrawViewLayout.showBk();
        initData();
        audioSave();


    }


    private void initData() {
        try {
            mPath = getHandPath(draftId);
            mRetContent.setPath(mPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static final String ROOT_PATH =  File.separator+"cn.shiming.fieldcharactershap";
    /**当前操作文件的保存路径*/
    public  String getHandPath(long draftId){
        String path = Environment.getExternalStorageDirectory().getPath()  + ROOT_PATH  + File.separator + "handdraw" + File.separator +"shiming" + File.separator + draftId + File.separator;
        return path;
    }
    public void audioSave() {
        mTimerSave = new Timer();
        mTimerSave.schedule(task, 60000, 20000);
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Message msg = mHandler.obtainMessage();
            msg.obj = false;
            mHandler.sendMessage(msg);
        }
    };
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long l1 = System.currentTimeMillis();
            if ((l1 - mOldTime) > HADN_DRAW_TIME) {
                mHandler.removeCallbacks(runnable);
                Message msg = mHandler.obtainMessage();
                msg.obj = true;
                msg.what = 0x123;
                mHandler.sendMessage(msg);
            } else {
                mHandler.postDelayed(this, 100);
            }

        }
    };
    @SuppressLint("HandlerLeak")//麻痹
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 0x123:
                    try {
                        boolean obj = (boolean) msg.obj;
                        if (obj) {
                            NewDrawPenView view = mDrawViewLayout.getSaveBitmap();
                            if (view != null) {
                                //边距强行扫描
                                mBitmap = view.clearBlank(100);
                                mHandler.post(runnableUi);
                            }
                        }
                    } catch (Exception e) {

                    } finally {
                        mHandler.removeCallbacks(runnable);
                    }
                    break;
                case 0x124:
                    mRetContent.setVisibilityEdit(View.VISIBLE);
                    mRetContent.setVisibilityClose(View.VISIBLE);
                    mRetContent.getLastFocusEdit().setCursorVisible(true);
                    mRetContent.getLastFocusEdit().requestFocus();
                    break;
                case 0x125:
                    break;
            }
        }
    };

    private void findViews() {
        mChangePen = (Button) findViewById(R.id.btn_change_pen);
        mRetContent = (HandRichTextEditor)findViewById(R.id.et_handdraw_content);
        mDrawViewLayout = (DrawViewLayout)findViewById(R.id.brush_weight);
        testStorage();
        mRetContent.setOnHandRichEditTextHasFocus(new HandRichTextEditor.onHandRichEditTextHasFocus() {
            @Override
            public void hasFocus(View view) {
                mDrawViewLayout.showBk();
            }

            @Override
            public void onClickChange(View v) {
                mDrawViewLayout.showBk();
            }
        });
        mChangePen.setOnClickListener(this);
    }

    private Bitmap creatBimap() {
        ColorDrawable drawable = new ColorDrawable(Color.TRANSPARENT);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap bitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }

    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            if (mIsCreateBitmap) {
                //110
                mBitmapResize = BitmapDrawUtils.resizeImage(mCreatBimap, mAllHandDrawSize, mAllHandDrawSize);
                mIsCreateBitmap = false;
            } else {
                mBitmapResize = BitmapDrawUtils.resizeImage(mBitmap, mAllHandDrawSize, mAllHandDrawSize);
            }
            if (mBitmapResize != null) {
                //根据Bitmap对象创建ImageSpan对象
                ImageSpan imageSpan = new ImageSpan(FieldCharacterShapeActivity.this, mBitmapResize);
                //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
                full_name = LAST_NAME + System.currentTimeMillis();
                String s =FONT_NAME_HEAD + full_name + FONT_NAME_TAIL;
                SpannableString spannableString = new SpannableString(s);
                //  用ImageSpan对象替换face
                spannableString.setSpan(imageSpan, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //将选择的图片追加到EditText中光标所在位置
                //                EditText ed = mSvContent.getFocusEditText();
                EditText ed = mRetContent.getLastFocusEdit();
                int index = ed.getSelectionStart(); //获取光标所在位置
                Editable edit_text = ed.getEditableText();
                if (index < 0 || index >= edit_text.length()) {
                    edit_text.append(spannableString);
                } else {
                    edit_text.insert(index, spannableString);
                }
                testStorage();
            }
            mDrawViewLayout.clearScreen();
        }

    };

    /**
     * 测试是否有储存权限
     */
    public void testStorage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText ed = mRetContent.getLastFocusEdit();
        ed.requestFocus();
        ed.setCursorVisible(true);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 这里是换行的需要
     */
    @Override
    public void creatNewLine() {
        EditText ed = mRetContent.getLastFocusEdit();
        int index = ed.getSelectionStart();
        Editable editable = ed.getText();
        editable.insert(index, "\n");
    }

    @Override
    public void getUptime(long l) {
        mOldTime = l;
        mHandler.postDelayed(runnable, 100);
    }

    @Override
    public void stopTime() {
        mHandler.removeCallbacks(runnable);
    }

    /**
     * 需要空格
     */
    @Override
    public void needSpace() {
        NewDrawPenView view = mDrawViewLayout.getSaveBitmap();
        if (view != null) {
            if (view.getHasDraw()) {
                mBitmap = view.getBitmap();
                mHandler.post(runnableUi);
                //保持一个联系
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsCreateBitmap = true;
                        if (mCreatBimap == null) {
                            mCreatBimap = creatBimap();
                        }
                        mHandler.post(runnableUi);
                    }
                }, 100);
            } else {
                mIsCreateBitmap = true;
                if (mCreatBimap == null) {
                    mCreatBimap = creatBimap();
                }
                mHandler.post(runnableUi);
            }
        }
        mHandler.removeCallbacks(runnable);
    }


    @Override
    public void deleteOnClick() {
        if (mRetContent.getLastFocusEdit().getSelectionStart() == 0) {
            mRetContent.onBackspacePress(mRetContent.getLastFocusEdit());
            mHandler.removeCallbacks(runnable);
        } else {
            SystemUtils.sendKeyCode(67);
        }

    }

    @Override
    public void deleteOnLongClick() {
        if (mRetContent.getLastFocusEdit().getSelectionStart() == 0) {
            mRetContent.onBackspacePress(mRetContent.getLastFocusEdit());
            if (mHandler!=null) {
                mHandler.removeCallbacks(runnable);
            }
        } else {
            SystemUtils.sendKeyCode(67);
        }
    }

    /**
     * @param flag 下面的键盘是否在显示了
     */
    @Override
    public void showkeyB(boolean flag) {
        if (flag) {
            mRetContent.getLastFocusEdit().requestFocus();
        }
        mRetContent.getLastFocusEdit().setCursorVisible(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mHandler) {
            mHandler = null;
        }
        mTimerSave.cancel();
    }

    @Override
    public void onClick(View v) {
        int penConfig = mDrawViewLayout.getPenConfig();
        switch (v.getId()){
            case R.id.btn_change_pen:
                if (penConfig== IPenConfig.STROKE_TYPE_PEN){
                    penConfig=IPenConfig.STROKE_TYPE_BRUSH;
                }else {
                    penConfig=IPenConfig.STROKE_TYPE_PEN;
                }
                mDrawViewLayout.setPenConfig(penConfig);
                break;
        }
    }
}
