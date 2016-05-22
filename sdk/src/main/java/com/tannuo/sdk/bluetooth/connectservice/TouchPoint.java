package com.tannuo.sdk.bluetooth.connectservice;

import com.tannuo.sdk.bluetooth.TouchScreen;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class TouchPoint {
    public static final byte BLACK = 0;
    public static final byte RED = 1;
    public static final byte WHITE = 2;

    public static final int STATUS_UP = 4;
    public static final int STATUS_DOWN = 7;

    public static final float MAX_X = 32767.0F;
    public static final float MAX_Y = 32767.0F;

    private int id;
    private int x;
    private int y;
    private int width;
    private int height;
    private byte color;
    private int status;

    public TouchPoint(TouchScreen.TouchPoint point) {
        this.id = point.pointId;
        this.x = point.pointX;
        this.y = point.pointY;
        this.width = point.pointWidth;
        this.height = point.pointHeight;
        this.color = point.pointColor;
        this.status = point.pointStatus;
    }

    public int getID() {
        return id;
    }

    public int getArea() {
        return width * height;
    }

    public float getX() {
        return getScaleX(this.x);
    }

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double distance(TouchPoint point) {
        double distance = Math.sqrt(Math.pow(this.getX() - point.getX(), 2) +
                Math.pow(this.getY() - point.getY(), 2));
        return distance;
    }

    private static int canvasWidth, canvasHeight;

    public static void setCanvas(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    private static float getScaleX(int x) {
        float result = x / MAX_X * canvasWidth;
        return result;
    }

    private static float getScaleY(int y) {
        float result = y / MAX_Y * canvasWidth;
        return result;
    }

    @Override
    public String toString() {
        return String.format("id:%s , x:%s ,y:%s ,width:%s ,height:%s,area:%s",
                getID(), getX(), getY(), getWidth(), getHeight(), getArea());
    }
}