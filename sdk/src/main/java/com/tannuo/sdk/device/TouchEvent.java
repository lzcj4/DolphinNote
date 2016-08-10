package com.tannuo.sdk.device;

import java.util.List;

/**
 * Created by Nick_PC on 2016/6/30.
 */
public class TouchEvent {
    public static final byte ACTION_TOUCH = 0;
    public static final int ACTION_SNAPSHOT = 1;

    private byte action;

    public byte getAction() {
        return action;
    }

    public void setAction(byte action) {
        this.action = action;
    }

    TouchFrame downFrame = new TouchFrame();
    TouchFrame moveFrame = new TouchFrame();
    TouchFrame upFrame = new TouchFrame();

    public void setPoints(List<TouchPoint> points) {
        if (null == points) {
            return;
        }
        for (TouchPoint point : points) {
            if (point.getIsDown()) {
                downFrame.put(point);
            } else if (point.getIsMove()) {
                moveFrame.put(point);
            } else if (point.getIsUp()) {
                upFrame.put(point);
            }
        }
    }

    public TouchFrame getDownFrame() {
        return downFrame;
    }

    public TouchFrame getMoveFrame() {
        return moveFrame;
    }

    public TouchFrame getUpFrame() {
        return upFrame;
    }
}
