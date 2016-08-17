package com.tannuo.sdk.device;

import android.util.SparseArray;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public int size() {
        return this.getPaths().size();
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
            path.setId((byte)point.getId());
        }
        path.add(point);
        put(path);
    }

    public boolean isEmpty() {
        return getPaths().size() == 0;
    }

    public TouchPath getItemAt(int index) {
        return this.getPaths().valueAt(index);
    }

    public byte[] getBytes() {
        byte[] result;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(stream);
        try {
            int pathSize = this.size();
            writer.writeShort(pathSize);
            for (int i = 0; i < pathSize; i++) {
                TouchPath path = this.getItemAt(i);
                writer.writeByte(path.getId());
                int pointSize = path.size();
                writer.writeShort(pointSize);
                for (int j = 0; j < pointSize; j++) {
                    TouchPoint point = path.getPoints().get(j);
                    writer.writeShort(point.getRawX());
                    writer.writeShort(point.getRawY());
                    writer.writeShort(point.getWidth());
                    writer.writeShort(point.getHeight());
                    writer.writeByte(point.getColor());
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            result = stream.toByteArray();
            try {
                stream.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
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
