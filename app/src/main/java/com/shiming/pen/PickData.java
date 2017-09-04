package com.shiming.pen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 选取的数据
 * Created by Speedy on 2017/8/4.
 */

public class PickData implements Serializable {

    private boolean isOriginal;

    private List<String> localMediaList = new ArrayList<>();


    public PickData() {

    }



    public void setOriginal(boolean original) {
        isOriginal = original;
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public List<String> getLocalMediaList() {
        return localMediaList;
    }

    public int getCount() {
        return localMediaList.size();
    }

    ;

    public void clear() {
        localMediaList.clear();
    }

    public void addLocalMedia(String localMedia) {

    }

    ;


    public void removeLocalMedia(String localMedia) {


    }







    /**
     * 深拷贝
     * @return
     * @throws IOException
     * @throws OptionalDataException
     * @throws ClassNotFoundException
     */
    public PickData deepClone() throws IOException,
            ClassNotFoundException {
        // 将对象写到流里
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(this);
        // 从流里读出来
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return (PickData) oi.readObject();
    }
}
