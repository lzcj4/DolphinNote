package com.tannuo.sdk.device;

import android.util.SparseArray;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Nick_PC on 2016/7/25.
 */
public class TouchFrameSet
        implements Iterator<TouchFrame>, Iterable<TouchFrame> {

    private int mSeqId;

    public int getSeqId() {
        return mSeqId;
    }

    public void setSeqId(int id) {
        this.mSeqId = id;
    }

    SparseArray<TouchFrame> mFrames;

    public SparseArray<TouchFrame> getFrames() {
        mFrames = mFrames == null ? new SparseArray<>() : mFrames;
        return mFrames;
    }

    public int size() {
        return this.getFrames().size();
    }

    public void setPaths(SparseArray<TouchFrame> paths) {
        this.mFrames = paths;
    }

    public void put(TouchFrame item) {
        getFrames().put(item.getSeqId(), item);
    }

    public TouchFrame get(int key) {
        return getFrames().get(key);
    }

    public void remove(int key) {
        getFrames().remove(key);
    }

    public static TouchFrameSet getFrames(DataInputStream reader) {
        TouchFrameSet result = null;
        try {
            int seqId = reader.readInt();//seq id 4
            int frameLen = reader.readShort();//frame size 2

            if (frameLen <= 0) {
                return result;
            }
            result = new TouchFrameSet();
            result.setSeqId(seqId);

            for (int i = 0; i < frameLen; i++) {
                TouchFrame frame = new TouchFrame();
                frame.setSeqId(seqId);
                result.put(frame);

                short pathLen = reader.readShort(); // path len 2
                for (int j = 0; j < pathLen; j++) {
                    int pathId = reader.readByte();  // 1
                    TouchPath path = new TouchPath();
                    path.setId(pathId);
                    frame.put(path);

                    short pointLen = reader.readShort();
                    for (int z = 0; z < pointLen; z++) {
                        TouchPoint point = new TouchPoint();
                        point.setId(pathId);
                        point.setX(reader.readShort());
                        point.setY(reader.readShort());
                        point.setWidth(reader.readShort());
                        point.setHeight(reader.readShort());
                        point.setColor(reader.readByte());
                        path.add(point);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    int step = 0;

    @Override
    public Iterator<TouchFrame> iterator() {
        step = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        return step < getFrames().size();
    }

    @Override
    public TouchFrame next() {
        return getFrames().valueAt(step++);
    }

    @Override
    public void remove() {
        getFrames().removeAt(--step);
    }
}
