package com.tannuo.sdk.device;

import com.tannuo.sdk.device.protocol.JYProtocol;
import com.tannuo.sdk.util.Logger;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class TouchPoint {
    private final String TAG = this.getClass().getSimpleName();

    public static final byte COLOR_BLACK = 0;
    public static final byte COLOR_RED = 1;
    public static final byte COLOR_WHITE = 2;

    public static final float MAX_X = 32767.0F;
    public static final float MAX_Y = 32767.0F;

    private static byte action_down = JYProtocol.ACTION_DOWN;
    private static byte action_move = JYProtocol.ACTION_MOVE;
    private static byte action_up = JYProtocol.ACTION_UP;

    ///Due to CVT down and up action are the same code
    public static final byte ACTION_DOWN = 0;
    public static final byte ACTION_MOVE = 1;
    public static final byte ACTION_UP = 2;
    public static float WIDTHHEIGHTRATIO = 525f / 932;

    private int rubberMaxSize = 2812 * 4993;  //80mm / 932mm * 32767,  80mm / 525mm * 32767
    private int rubberMinSize = 1406 * 2496;  //40mm / 932mm * 32767,  40mm / 525mm * 32767
    private int penMaxSize = 360 * 630;      //10mm / 932mm * 32767,  10mm / 525mm * 32767
    private int penMinSize = 144 * 252;      //4mm / 932mm * 32767,  4mm / 525mm * 32767
    private float penWidth = 4;               //pixel


    public static void setActions(byte actionDown, byte actionMove, byte actionUp) {
        action_down = actionDown;
        action_move = actionMove;
        action_up = actionUp;
    }

    private byte id;
    private short x;
    private short y;
    private short width;
    private short height;
    private byte color;
    private int action;

    public TouchPoint() {

    }

    public TouchPoint(byte id, short x, short y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public int getArea() {
        return width * height;
    }

    public short getRawX() {
        return x;
    }

    public short getRawY() {
        return y;
    }

    /**
     * Get scale X
     *
     * @return
     */
    public float getX() {
        return getScaleX(this.x);
    }

    /**
     * Get scale Y
     *
     * @return
     */
    public float getY() {
        return getScaleY(this.y);
    }

    public void setY(short newValue) {
        this.y = newValue;
    }

    public void setX(short newValue) {
        this.x = newValue;
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public short getHeight() {
        return height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    public byte getColor() {
        return color;
    }

    public void setColor(byte color) {
        this.color = color;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
        Logger.i(TAG, String.format("current point action is:%s", action));
    }

    public void setActionByDevice(int action) {
        if (action == action_down) {
            this.action = ACTION_DOWN;
        } else if (action == action_move) {
            this.action = ACTION_MOVE;
        } else if (action == action_up) {
            this.action = ACTION_UP;
        } else {
            // throw new InvalidParameterException();
        }
    }


    public boolean getIsDown() {
        return this.action == ACTION_DOWN;
    }

    public boolean getIsMove() {
        return this.action == ACTION_MOVE;
    }

    public boolean getIsUp() {
        return this.action == ACTION_UP;
    }

    public double distance(TouchPoint point) {
        double distance = Math.sqrt(Math.pow(this.getX() - point.getX(), 2) +
                Math.pow(this.getY() - point.getY(), 2));
        return distance;
    }

    public boolean isLongDistance(TouchPoint point) {
        return distance(point) > 50;
    }

    public boolean isRubber() {
        return getArea() >= rubberMinSize && getArea() <= rubberMaxSize || getArea() > rubberMaxSize;
    }

    private static int canvasWidth, canvasHeight;

    public static void setCanvas(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public static float getScaleX(int x) {
        float result = x / MAX_X * canvasWidth;
        return result;
    }

    public static float getScaleY(int y) {
        float result = y / MAX_Y * canvasHeight;
        return result;
    }

    @Override
    public String toString() {
        return String.format("Id:%s ,action:%s , x:%s ,y:%s ,width:%s ,height:%s,area:%s",
                getId(), getAction(), getRawX(), getRawY(), getWidth(), getHeight(), getArea());
    }
}
