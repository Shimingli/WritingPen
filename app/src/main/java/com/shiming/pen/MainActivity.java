package com.shiming.pen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.shiming.pen.new_code.NewDrawPenView;
import com.shiming.pen.new_code.IPenConfig;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnStrokePen;
    private Button mBtnClearCanvas;
    private NewDrawPenView mDrawPenView;
    private Button mBrushPen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        doSomeThing();

    }

    private void doSomeThing() {
        mBtnStrokePen.setOnClickListener(this);
        mBtnClearCanvas.setOnClickListener(this);
        mBrushPen.setOnClickListener(this);
    }

    private void findViews() {
        mBtnStrokePen = (Button) findViewById(R.id.btn_stroke_pen);
        mDrawPenView = (NewDrawPenView) findViewById(R.id.draw_pen_view);
        mBtnClearCanvas = (Button) findViewById(R.id.btn_clear_canvas);
        mBrushPen = (Button) findViewById(R.id.btn_brush_pen);

    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.btn_stroke_pen:
               mDrawPenView.setCanvasCode(IPenConfig.STROKE_TYPE_PEN);
               break;
           case R.id.btn_clear_canvas:
               mDrawPenView.setCanvasCode(IPenConfig.STROKE_TYPE_ERASER);
               break;
           case R.id.btn_brush_pen:
               mDrawPenView.setCanvasCode(IPenConfig.STROKE_TYPE_BRUSH);
               break;
       }

    }
}
