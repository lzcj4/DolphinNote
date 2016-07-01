package com.tannuo.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.tannuo.note.whiteboard.Smooth;

public class DrawView extends View {
	private final String TAG = "DrawView";
    public Paint mPaint = new Paint();
    private Bitmap mDrawBitmap;
    private Smooth mSmooth = new Smooth();
    public int[] lastPoint;
    private int[][] pointSet;
    private float mScreenXMax = 32768;
    private float mScreenYMax = 32768;
    private float mScreenWidth;
    private float mScreenHeight;
    public boolean isSmooth = true;
    private float ratioX;
    private float ratioY;
    private boolean isSetPoints = false;

    public DrawView(Context context, boolean startSmooth) {
        super(context);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(2);
        this.setWillNotDraw(false);
        lastPoint = new int[3];
        lastPoint[2] = -1;
        isSmooth = startSmooth;
    }
    
    public void AddDrawPara(Bitmap mBitmap, int ScreenWight, int ScreenHeight){
        mDrawBitmap = mBitmap;
        mScreenWidth = (float) ScreenWight;
        mScreenHeight = (float) ScreenHeight;
        ratioX = mScreenHeight / mScreenXMax;
        ratioY = mScreenWidth / mScreenYMax;
    }
    
    public void setPoints(int[][] Points){
    	pointSet = new int[Points.length][];
        int i;
        if (isSmooth){
            pointSet = mSmooth.smoothLine(Points);
            int l;
            for (l = 0; l<pointSet.length; l++)
            {
                pointSet[l][0] = (int)(mScreenHeight-pointSet[l][0] * ratioX);
                pointSet[l][1] = (int)(pointSet[l][1] * ratioY);
            }
        }
        else{
            for (i = 0; i<Points.length; i++) {
                pointSet[i] = new int[3];
                pointSet[i][0] = (int)(mScreenHeight-Points[i][0] * ratioX);
                pointSet[i][1] = (int)(Points[i][1] * ratioY);
                pointSet[i][2] = Points[i][2];
            }
        }
        isSetPoints = true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int h;
        if (isSetPoints == true && pointSet != null) {
        	isSetPoints = false;
            if (lastPoint[2] != -1 && lastPoint[2] == pointSet[0][2])
                canvas.drawLine(pointSet[0][1], pointSet[0][0], lastPoint[1], lastPoint[0], mPaint);
            for (h = 1; h < pointSet.length; h++) {
                if (pointSet[h - 1][2] == pointSet[h][2]) {
                    canvas.drawLine(pointSet[h - 1][1], pointSet[h - 1][0], pointSet[h][1], pointSet[h][0], mPaint);
                }
            }

            lastPoint[0] = pointSet[pointSet.length-1][0];
            lastPoint[1] = pointSet[pointSet.length-1][1];
            lastPoint[2] = pointSet[pointSet.length-1][2];

        }
        if (mDrawBitmap != null) {
            canvas.drawBitmap(mDrawBitmap, 0, 0, mPaint);
        }
    }
}

