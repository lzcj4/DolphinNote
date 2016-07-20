package com.tannuo.sdk.device;

import android.util.SparseArray;

import java.util.Iterator;

/**
 * Created by Nick_PC on 2016/7/20.
 */
public class TouchFrame implements Iterator<TouchPath>, Iterable<TouchPath> {
    private int mSeqId;

    public int getSeqId() {
        return mSeqId;
    }

    public void setSeqId(int id) {
        this.mSeqId = id;
    }

    SparseArray<TouchPath> mPaths;

    public SparseArray<TouchPath> getPaths() {
        mPaths = mPaths == null ? new SparseArray<>() : mPaths;
        return mPaths;
    }

    public void setPaths(SparseArray<TouchPath> paths) {
        this.mPaths = paths;
    }

    public void put(TouchPath path) {
        getPaths().put(path.getId(), path);
    }

    public TouchPath get(int key) {
        return getPaths().get(key);
    }

    public void remove(int key) {
        getPaths().remove(key);
    }

    public void put(TouchPoint point) {
        TouchPath path = getPaths().get(point.getId());
        if (null == path) {
            path = new TouchPath();
            path.setId(point.getId());
        }
        path.add(point);
        put(path);
    }

    public boolean isEmpty() {
        return getPaths().size() == 0;
    }

    int step = 0;

    @Override
    public Iterator<TouchPath> iterator() {
        step = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        return step < getPaths().size();
    }

    @Override
    public TouchPath next() {
        return getPaths().valueAt(step++);
    }

    @Override
    public void remove() {
        getPaths().removeAt(--step);
    }
}
