package com.shiming.pen.field_character;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shiming.pen.field_character.FieldCharacterShapeActivity.FONT_NAME_HEAD;
import static com.shiming.pen.field_character.FieldCharacterShapeActivity.FONT_NAME_TAIL;
import static com.shiming.pen.field_character.FieldCharacterShapeActivity.mEmotionSize;

/**
 * author： Created by shiming on 2017/8/20 15:08
 * mailbox：lamshiming@sina.com
 * 手写view通用工具
 */
public class HandViewUtils {
    /**
     * 表情解析需要用的
     */
    public static List<WordSandPictures> setRelcenote(String relcenote) {
        ArrayList<WordSandPictures> listRelce = new ArrayList<WordSandPictures>();
        return listRelce;
    }

    public static SpannableStringBuilder getRelcenote(String content, List<WordSandPictures> listRelce) {
        return getRelcenote(content, listRelce, mEmotionSize, mEmotionSize);
    }

    public static SpannableStringBuilder getRelcenote(String content, List<WordSandPictures> listRelce, double newWidth, double newHeight) {
        SpannableStringBuilder spannableString = new SpannableStringBuilder(content);
        return spannableString;
    }


    public static SpannableStringBuilder getEditImg(Context context, SpannableStringBuilder txt, String path) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(txt);
        Pattern pattern = Pattern.compile("\\" + FONT_NAME_HEAD + "(\\S+?)\\" + FONT_NAME_TAIL + "");//匹配[xx]的字符串
        Matcher matcher = pattern.matcher(txt);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String group = matcher.group();
            group = group.substring(FONT_NAME_HEAD.length(), group.length() - FONT_NAME_TAIL.length());
            Bitmap bitmap = getSdBitmap(path + group);
            ImageSpan imageSpan = new ImageSpan(context, bitmap);
            spannableStringBuilder.setSpan(imageSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    /***
     * 获得SD卡bitmap
     */
    public static Bitmap getSdBitmap(String pathname) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(pathname);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
