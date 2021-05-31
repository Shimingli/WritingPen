package com.shiming.pen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.shiming.pen.field_character.FieldCharacterShapeActivity;
import com.shiming.pen.view.OldDemoActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnStrokePen;
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
        mBrushPen.setOnClickListener(this);
    }

    private void findViews() {
        mBtnStrokePen = (Button) findViewById(R.id.btn_stroke_pen);
        mBrushPen = (Button) findViewById(R.id.btn_brush_pen);

    }

    @Override
    public void onClick(View v) {
        Intent intent=null;
       switch (v.getId()){
           case R.id.btn_stroke_pen://oldDemo
                 intent=new Intent(MainActivity.this, OldDemoActivity.class);
                 startActivity(intent);
               break;
           case R.id.btn_brush_pen://田字格的Demo
               intent=new Intent(MainActivity.this, FieldCharacterShapeActivity.class);
               startActivity(intent);
               break;
       }
    }
}
