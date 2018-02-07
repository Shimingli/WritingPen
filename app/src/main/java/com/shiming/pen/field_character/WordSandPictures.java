package com.shiming.pen.field_character;

import java.io.Serializable;

public class WordSandPictures implements Serializable {
    public int start;
    public int end;
    public String path = "";
    public String allPath = "";
    public int type = -1;
    public int faceIndex;

    @Override
    public String toString() {
        return "WordSandPictures{" +
                "allPath='" + allPath + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", path='" + path + '\'' +
                ", type=" + type +
                ", faceIndex=" + faceIndex +
                '}';
    }
}

