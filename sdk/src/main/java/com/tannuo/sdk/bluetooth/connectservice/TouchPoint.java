package com.tannuo.sdk.bluetooth.connectservice;

/**
 * Created by Nick_PC on 2016/5/20.
 */
public class TouchPoint {

    public static final byte BLACK = 0;
    public static final byte RED = 1;
    public static final byte WHITE = 2;


    public int X;
    public int Y;

    public int Width;
    public int Height;

    public byte Color;

    public TouchPoint(int x, int y) {
        this.X = x;
        this.Y = y;
    }

    public int getArea() {
        return Width * Height;
    }

    public float getX() {
        return getScaleX(this.X);
    }

    public float getY() {
        return getScaleY(this.Y);
    }

    private static int canvasWidth, canvasHeight;

    public static void setCanvas(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    private static float getScaleX(int x) {
        float result = x * canvasWidth / 32676.f;
        return result;
    }

    private static float getScaleY(int y) {
        float result = y * canvasHeight / 32676.f;
        return result;
    }


}
