package com.tannuo.note.whiteboard;

import com.tannuo.sdk.bluetooth.device.TouchPoint;

import java.util.List;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class TouchPath {
    private int mId;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    List<TouchPoint> mPoints;

    public List<TouchPoint> getPoints() {
        return mPoints;
    }

    public void setPoints(List<TouchPoint> points) {
        this.mPoints = points;
    }
}
