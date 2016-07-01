package com.tannuo.sdk.device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick_PC on 2016/6/30.
 */
public class TouchEvent {
    public static final byte ACTION_TOUCH = 0;
    public static final int ACIION_SNAPSHOT = 1;

    private byte action;
    private List<TouchPoint> points;

    public byte getAction() {
        return action;
    }

    public void setAction(byte action) {
        this.action = action;
    }

    public List<TouchPoint> getPoints() {
        return points;
    }

    public void setPoints(List<TouchPoint> points) {
        this.points = points;
    }

    public void append(TouchPoint point) {
        if (null == points) {
            points = new ArrayList<>();
        }
        points.add(point);
    }
}
