package com.tannuo.sdk.device;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class TouchPoint {
    public static final byte COLOR_BLACK = 0;
    public static final byte COLOR_RED = 1;
    public static final byte COLOR_WHITE = 2;

    public static final byte ACTION_DOWN =0x07;
    public static final byte ACTION_MOVE = 0x01;/// TODO: 2016/7/1   undefined move action code
    public static final byte ACTION_UP = 0x04;

    public static final float MAX_X = 32767.0F;
    public static final float MAX_Y = 32767.0F;

    private int id;
    private int x;
    private int y;
    private int width;
    private int height;
    private byte color;
    private int action;

    public TouchPoint() {

    }

    public TouchPoint(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArea() {
        return width * height;
    }

    public int getRawX() {
        return x;
    }

    public int getRawY() {
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

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
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
    }

    public boolean getIsDown() {
        return this.action == ACTION_DOWN;
    }

    public double distance(TouchPoint point) {
        double distance = Math.sqrt(Math.pow(this.getX() - point.getX(), 2) +
                Math.pow(this.getY() - point.getY(), 2));
        return distance;
    }

    public boolean isLongDistance(TouchPoint point) {
        return distance(point) > 50;
    }

    private int rubberMaxSize = 2812 * 4993;  //80mm / 932mm * 32767,  80mm / 525mm * 32767
    private int rubberMinSize = 1406 * 2496;  //40mm / 932mm * 32767,  40mm / 525mm * 32767
    private int penMaxSize = 360 * 630;      //10mm / 932mm * 32767,  10mm / 525mm * 32767
    private int penMinSize = 144 * 252;      //4mm / 932mm * 32767,  4mm / 525mm * 32767
    private float penWidth = 4;               //pixel

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
                getId(), getAction(), getX(), getY(), getWidth(), getHeight(), getArea());
    }
}
