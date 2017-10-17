package com.shiming.pen.new_code;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * @author shiming
 * @version v1.0 create at 2017/10/10
 * @des
 */
public class PenUtlis {

    private int mBackColor = Color.TRANSPARENT;

    /**
     * 逐行扫描 清楚边界空白。功能是生成一张bitmap位于正中间，不是位于顶部，此关键的是我们画布需要
     * 成透明色才能生效
     * @param blank 边距留多少个像素
     * @return tks github E-signature
     */
    public Bitmap clearBlank(Bitmap mBitmap,int blank) {
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
}
