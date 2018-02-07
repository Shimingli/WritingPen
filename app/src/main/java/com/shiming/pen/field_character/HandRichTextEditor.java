package com.shiming.pen.field_character;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.shiming.pen.R;

import java.util.ArrayList;
import java.util.List;

/**
 * author： Created by shiming on 2018/1/20 15:08
 * mailbox：lamshiming@sina.com
 * 可编辑富文本
 */
public class HandRichTextEditor extends ScrollView {
    private static final int EDIT_PADDING = 0; // edittext常规padding是10dp

    private String mPATH = null;  // 保存的路径，需要每次从外面传进来
    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private OnKeyListener keyListener; // 所有EditText的软键盘监听器
    private ArrayList<View> btnCloseList;  //所有关闭按钮的集合
    private ArrayList<View> viewCloseList;  //所有空白View，需要和关闭按钮一起隐藏
    private ArrayList<EditText> editViewList;  //用来收集所有edit
    private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
    private EditText lastFocusEdit; // 最近被聚焦的EditText
    private LayoutTransition mTransitioner; // 只在图片View添加或remove时，触发transition动画
    private int editNormalPadding = 5; //


    public HandRichTextEditor(Context context) {
        this(context, null);
    }

    public HandRichTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HandRichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = LayoutInflater.from(context);
        btnCloseList = new ArrayList<View>();
        viewCloseList = new ArrayList<View>();
        editViewList = new ArrayList<EditText>();
        init();
    }

    /**
     * 设置空白EditText属性
     * */
    public void setVisibilityEdit(int visibility){
        for (EditText view : editViewList){
            if(view != null && TextUtils.isEmpty(view.getText())){
                view.setVisibility(visibility);
            }
        }
    }
    public void setVisibilityClose(int visibility){
        for (View view : btnCloseList){
            view.setVisibility(visibility);
        }
        for(View view:viewCloseList){
            view.setVisibility(visibility);
        }
    }

    public void setPath(String path){
        mPATH = path;
    }

    private void init(){
        // 1. 初始化allLayout
        allLayout = new LinearLayout(getContext());
        allLayout.setOrientation(LinearLayout.VERTICAL);
        setupLayoutTransitions();
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        allLayout.setPadding(5, 15, 5, 15);//设置间距，防止生成图片时文字太靠边，不能用margin，否则有黑边
        addView(allLayout, layoutParams);
        // 2. 初始化键盘退格监听
        // 主要用来处理点击回删按钮时，view的一些列合并操作
        keyListener = new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                }
                return false;
            }
        };

        focusListener = new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lastFocusEdit = (EditText) v;
                    if (mOnHandRichEditTextHasFocus==null)return;
                    mOnHandRichEditTextHasFocus.hasFocus(v);
                }
            }
        };
        createFirstEditText();
    }

    public onHandRichEditTextHasFocus mOnHandRichEditTextHasFocus;

    /**
     * 处理软键盘backSpace回退事件
     *
     * @param editTxt 光标所在的文本输入框
     */
    public void onBackspacePress(EditText editTxt) {
        int startSelection = editTxt.getSelectionStart();
        // 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
        if (startSelection == 0) {
            int editIndex = allLayout.indexOfChild(editTxt);
            View preView = allLayout.getChildAt(editIndex - 1); // 如果editIndex-1<0,
            // 则返回的是null
            if (null != preView) {
                if (preView instanceof RelativeLayout) {
                    // 光标EditText的上一个view对应的是图片
                    //onImageCloseClick(preView);
                } else if (preView instanceof EditText) {
                    // 光标EditText的上一个view对应的还是文本框EditText
                    String str1 = editTxt.getText().toString();
                    EditText preEdit = (EditText) preView;
                    String str2 = preEdit.getText().toString();

                    allLayout.removeView(editTxt);
                    editViewList.remove(editTxt);
                    // 文本合并
                    String str3 = str2 + str1;
                    List<WordSandPictures> list = HandViewUtils.setRelcenote(str3);
                    SpannableStringBuilder strbuilder = HandViewUtils.getRelcenote(str3,list);
                    SpannableStringBuilder strbuilder2 = HandViewUtils.getEditImg(getContext(),strbuilder,mPATH);
                    preEdit.setText(strbuilder2);
                    preEdit.requestFocus();
                    preEdit.setSelection(str2.length(), str2.length());
                    lastFocusEdit = preEdit;
                }
            }
        }
    }

    public interface onHandRichEditTextHasFocus{
        void hasFocus(View view);

        void onClickChange(View v);
    }
    public void setOnHandRichEditTextHasFocus(onHandRichEditTextHasFocus li){
        mOnHandRichEditTextHasFocus=li;
    }
    /**
     * 首次创建EditText
     * */
    private void createFirstEditText(){
        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //editNormalPadding = dip2px(EDIT_PADDING);
        EditText firstEdit = createEditText("", dip2px(getContext(), EDIT_PADDING));
        allLayout.addView(firstEdit, firstEditParam);
        lastFocusEdit = firstEdit;
        editViewList.add(firstEdit);
    }

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        mTransitioner = new LayoutTransition();
        allLayout.setLayoutTransition(mTransitioner);
        mTransitioner.addTransitionListener(new LayoutTransition.TransitionListener() {

            @Override
            public void startTransition(LayoutTransition transition,
                                        ViewGroup container, View view, int transitionType) {

            }

            @Override
            public void endTransition(LayoutTransition transition,
                                      ViewGroup container, View view, int transitionType) {
                if (!transition.isRunning()
                        && transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
                    // transition动画结束，合并EditText
                    // mergeEditText();
                }
            }
        });
        mTransitioner.setDuration(300);
    }

    public int dip2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    public void clearAllLayout() {
        allLayout.removeAllViews();
        btnCloseList.clear();
        viewCloseList.clear();
    }
    /**
     * 生成文本输入框
     */
    public EditText createEditText(String hint, int paddingTop) {
        EditText editText = (EditText) inflater.inflate(R.layout.hand_rich_edittext, null);
        editText.setOnKeyListener(keyListener);
        editText.setTag(viewTagIndex++);
        editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);
        editText.setHint(hint);
        editText.setOnFocusChangeListener(focusListener);
        editText.requestFocus();
        editText.setCursorVisible(true);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        /**禁止长按，选择和隐藏键盘*/
        editText.setLongClickable(false);
        editText.setTextIsSelectable(false);
        editText.setOnClickListener(onClickListener);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);////点击EditText时，不会弹出一个全屏输入窗口
        editText.setCustomSelectionActionModeCallback(callback);
        SystemUtils.hideSoftInputMethod(editText);
        return editText;
    }
    OnClickListener  onClickListener= new OnClickListener(){

        @Override
        public void onClick(View v) {
            if (mOnHandRichEditTextHasFocus==null)return;
            mOnHandRichEditTextHasFocus.onClickChange(v);
        }
    };

    /**禁止长按和选择*/
    ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    public EditText getLastFocusEdit(){
        return lastFocusEdit;
    }

}
