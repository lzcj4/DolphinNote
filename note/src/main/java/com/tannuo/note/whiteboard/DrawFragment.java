package com.tannuo.note.whiteboard;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.tannuo.note.R;
import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.connectservice.ConnectService;
import com.tannuo.sdk.bluetooth.connectservice.MockConnectService;
import com.tannuo.sdk.bluetooth.connectservice.TouchEvent;
import com.tannuo.sdk.bluetooth.connectservice.TouchListener;
import com.tannuo.sdk.bluetooth.connectservice.TouchPoint;
import com.tannuo.sdk.util.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DrawFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    @Bind(R.id.surfaceView)
    android.view.SurfaceView surfaceView;

    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBitmap;
    private ConnectService mConnectService;
    private Canvas mBmpCanvas;
    private Paint mLinePaint, mBmpPaint;
    private int mPaintWidth, mPaintHeight;
    private float mWidthRatio, mHeightRatio;

    private final float STROKE_WIDTH = 6.0f;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_draw, container, false);
        ButterKnife.bind(this, view);

        mSurfaceHolder = surfaceView.getHolder();
        //surfaceView.setBackgroundColor(Color.WHITE);

        mConnectService = new MockConnectService(mTouchListener);
//        mConnectService = new BLCConnectService(this.getActivity(), new TouchScreenListenerImpl() {
//            @Override
//            public void onTouchUp(List<TouchScreen.TouchPoint> upPoints) {
//                mTouchListener.onTouch(getTouchEvent(TouchEvent.UP, upPoints));
//            }
//
//            @Override
//            public void onTouchDown(List<TouchScreen.TouchPoint> downPoints) {
//                mTouchListener.onTouch(getTouchEvent(TouchEvent.DOWN, downPoints));
//            }
//
//            @Override
//            public void onTouchMove(List<TouchScreen.TouchPoint> movePoints) {
//                mTouchListener.onTouch(getTouchEvent(TouchEvent.MOVE, movePoints));
//            }
//        });

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(STROKE_WIDTH);
        // mLinePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setPathEffect(new CornerPathEffect(5));
        mLinePaint.setColor(Color.WHITE);
        //  mLinePaint.setDither(true);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBmpPaint.setStrokeWidth(STROKE_WIDTH);
        //mBmpPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        mBmpPaint.setStyle(Paint.Style.STROKE);
        mBmpPaint.setPathEffect(new CornerPathEffect(5));
        mBmpPaint.setColor(Color.WHITE);
        mBmpPaint.setAlpha(100);

        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mPaintWidth = surfaceView.getWidth();
                mPaintHeight = surfaceView.getHeight();
                setPaintWidthAndHeight(mPaintWidth, mPaintHeight);

                //mBmpCanvas.drawColor(Color.WHITE);
                mConnectService.connect("HC-05");
                //}
            }
        });

        return view;
    }

    private void setPaintWidthAndHeight(int width, int height) {
        TouchPoint.setCanvas(width, height);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBmpCanvas = new Canvas(mBitmap);
        mWidthRatio = width / TouchPoint.MAX_X;
        mHeightRatio = height / TouchPoint.MAX_Y;
        lastPointArray[2] = -1;
    }

    private final int POINTS_NUM = 10;
    private int[][] downPoints = new int[POINTS_NUM][3];
    private int currentPointIndex = 0;

    private TouchEvent getTouchEvent(int touchMode, List<TouchScreen.TouchPoint> points) {
        TouchEvent result = new TouchEvent();
        result.Mode = touchMode;
        List<TouchPoint> list = new ArrayList<>();
        for (TouchScreen.TouchPoint item : points) {
            TouchPoint p = new TouchPoint(item);
            list.add(p);
        }
        result.Points = list;
        return result;
    }

    private TouchListener mTouchListener = (event) -> {
        switch (event.Mode) {
            case TouchEvent.DOWN:
                touchDown(event.Points);
                break;
            case TouchEvent.MOVE:
                drawLine(event.Points);
                break;
            case TouchEvent.UP:
//                if (currentPointIndex > 0) {
//                    int[][] remainPoints;
//                    remainPoints = Arrays.copyOfRange(downPoints, 0, currentPointIndex);
//                    setSmoothPoints(remainPoints);
//                    drawLineSmoothly();
//                    currentPointIndex = 0;
//                }
                touchUp(event.Points);
                break;
        }
    };

    private void touchDown(List<TouchPoint> points) {
//        for (TouchPoint item : points) {
//            if (currentPointIndex < POINTS_NUM) {
//                downPoints[currentPointIndex][0] = item.getRawX();
//                downPoints[currentPointIndex][1] = item.getRawY();
//                downPoints[currentPointIndex][2] = item.getID();
//                currentPointIndex++;
//                Log.v(TAG, "onTouchDown X " + item.getRawX() + " Y " + item.getRawY());
//            }
//            if (currentPointIndex == POINTS_NUM) {
//                setSmoothPoints(downPoints);
//                drawLineSmoothly();
//                currentPointIndex = 0;
//            }
//        }
        drawLine(points);
    }

    boolean isSmooth = false;
    private int[][] pointSet;
    private Smooth smooth = new Smooth();
    boolean isSetPoints = false;

    public void setSmoothPoints(int[][] points) {
        pointSet = new int[points.length][];
        if (isSmooth) {
            pointSet = smooth.SmoothLine(points);
            for (int i = 0; i < pointSet.length; i++) {
                pointSet[i][0] = (int) (mPaintWidth - pointSet[i][0] * mWidthRatio);
                pointSet[i][1] = (int) (pointSet[i][1] * mHeightRatio);
            }
        } else {
            for (int i = 0; i < points.length; i++) {
                pointSet[i] = new int[3];
                pointSet[i][0] = (int) (points[i][0] * mWidthRatio);
                pointSet[i][1] = (int) (points[i][1] * mHeightRatio);
                pointSet[i][2] = points[i][2];
            }
        }
        isSetPoints = true;
    }

    TouchPoint lastPoint = null;
    private int[] lastPointArray = new int[3];

    protected void drawLineSmoothly() {
        if (isSetPoints && pointSet != null) {
            isSetPoints = false;
            int len = pointSet.length;
            if (lastPointArray[2] != -1 && lastPointArray[2] == pointSet[0][2])
                mBmpCanvas.drawLine(pointSet[0][0], pointSet[0][1], lastPointArray[0], lastPointArray[1], mLinePaint);
            for (int i = 0; i < len - 1; i++) {
                if (pointSet[i][2] == pointSet[i + 1][2]) {
                    mBmpCanvas.drawLine(pointSet[i][0], pointSet[i][1], pointSet[i + 1][0], pointSet[i + 1][1], mLinePaint);
                }
            }

            lastPointArray[0] = pointSet[len - 1][0];
            lastPointArray[1] = pointSet[len - 1][1];
            lastPointArray[2] = pointSet[len - 1][2];
        }

        Canvas canvas = mSurfaceHolder.lockCanvas();
        drawBitmap(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void drawLine(List<TouchPoint> points) {
        Path path = new Path();
        if (lastPoint == null) {
            lastPoint = points.get(0);
        }
        path.moveTo(lastPoint.getX(), lastPoint.getY());
        boolean isFirst = true;
        int len = points.size();

        for (int i = 0; i < len; i++) {
            TouchPoint p = points.get(i);
            mLinePaint.setStrokeWidth(getPaintWidth(p));
            double distance = p.distance(lastPoint);
            if (distance > 50) {
                Logger.e(TAG, String.format("Id1:%s to Id2:%s, len:%s", lastPoint.getID(), p.getID(), distance));
                lastPoint = p;
                mBmpCanvas.drawPoint(p.getX(), p.getY(), mLinePaint);
                path.moveTo(p.getX(), p.getY());
                continue;
            }

            if (isFirst) {
                path.lineTo(p.getX(), p.getY());
                isFirst = false;
            } else {
                float cx = (lastPoint.getX() + p.getX()) / 2;
                float cy = (lastPoint.getY() + p.getY()) / 2;
                //path.quadTo(cx, cy, p.getX(), p.getY());
                path.lineTo(p.getX(), p.getY());
                path.moveTo(p.getX(), p.getY());
            }
            lastPoint = p;
        }

        mBmpCanvas.drawPath(path, mLinePaint);
        Canvas canvas = mSurfaceHolder.lockCanvas();
        drawBitmap(canvas);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void drawBitmap(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBmpPaint);
    }

    int maxArea = 0;
    private float getPaintWidth(TouchPoint newPoint) {
        if (maxArea <= 0 && lastPoint != null) {
            maxArea = lastPoint.getArea();
        }

        int newArea = newPoint.getArea();
        if (newArea > maxArea) {
            maxArea = newArea;
            return STROKE_WIDTH;
        } else {
            float penWidth = STROKE_WIDTH * newArea / maxArea;
            penWidth = penWidth < STROKE_WIDTH / 2 ? STROKE_WIDTH / 2 : penWidth;
            return penWidth;
        }
    }

    private void touchUp(List<TouchPoint> points) {
        lastPoint = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
