package com.tannuo.sdk.bluetooth.device;

import java.util.List;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class TouchEvent {

    public static final int DOWN = 0;
    public static final int UP = 1;
    public static final int MOVE = 2;

    public int Mode;
    public List<TouchPoint> Points;

}
