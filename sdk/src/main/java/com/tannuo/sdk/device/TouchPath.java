package com.tannuo.sdk.device;

import java.util.ArrayList;
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
        mPoints = mPoints == null ? new ArrayList<>() : mPoints;
        return mPoints;
    }

    public int size() {
        return getPoints().size();
    }

    public TouchPoint getLastPoint() {
        TouchPoint point = null;
        if (getPoints().size() > 0) {
            point = getPoints().get(getPoints().size() - 1);
        }
        return point;
    }

    public void setPoints(List<TouchPoint> points) {
        this.mPoints = points;
    }

    public void add(TouchPoint point) {
        getPoints().add(point);
    }
}
