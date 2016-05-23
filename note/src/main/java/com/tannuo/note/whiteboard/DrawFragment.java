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
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import com.tannuo.note.R;
import com.tannuo.sdk.bluetooth.connectservice.MockConnectService;
import com.tannuo.sdk.bluetooth.connectservice.TouchEvent;
import com.tannuo.sdk.bluetooth.connectservice.TouchListener;
import com.tannuo.sdk.bluetooth.connectservice.TouchPoint;
import com.tannuo.sdk.util.Logger;

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
    private MockConnectService mockConnectService;
    private Canvas mBmpCanvas;
    private Paint mLinePaint, mBmpPaint;
    private int mPaintWidth;
    private int mPaintHeight;

    private final float PAINT_WIDTH = 6.0f;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_draw, container, false);
        ButterKnife.bind(this, view);

        mSurfaceHolder = surfaceView.getHolder();
        //surfaceView.setBackgroundColor(Color.WHITE);

        mockConnectService = new MockConnectService(mTouchListener);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(PAINT_WIDTH);
        // mLinePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setPathEffect(new CornerPathEffect(5));
        mLinePaint.setColor(Color.WHITE);
        //  mLinePaint.setDither(true);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBmpPaint.setStrokeWidth(PAINT_WIDTH);
        //mBmpPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        mBmpPaint.setStyle(Paint.Style.STROKE);
        mBmpPaint.setPathEffect(new CornerPathEffect(5));
        mBmpPaint.setColor(Color.WHITE);
        mBmpPaint.setAlpha(100);

        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {
                    // surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mPaintWidth = surfaceView.getWidth();
                    mPaintHeight = surfaceView.getHeight();
                    TouchPoint.setCanvas(mPaintWidth, mPaintHeight);
                    int dd = DisplayMetrics.DENSITY_DEFAULT;
                    mBitmap = Bitmap.createBitmap(mPaintWidth, mPaintHeight, Bitmap.Config.ARGB_8888);
                    mBmpCanvas = new Canvas(mBitmap);
                    //this.surfaceView.postDelayed(() -> testDrawLine(), 2 * 1000);
                    //mBmpCanvas.drawColor(Color.WHITE);
                    mockConnectService.connect("test");
                    //}
                });

        return view;
    }

    private void testDrawLine() {
        Path path = new Path();
        path.moveTo(10, 10);
        path.lineTo(200, 200);
        mBmpCanvas.drawPath(path, mLinePaint);
        Canvas canvas = this.mSurfaceHolder.lockCanvas();
        canvas.drawBitmap(mBitmap, 0, 0, mBmpPaint);
        this.mSurfaceHolder.unlockCanvasAndPost(canvas);

    }

    private TouchListener mTouchListener = (event) -> {
        switch (event.Mode) {
            case TouchEvent.DOWN:
                touchDown(event.Points);
                break;
            case TouchEvent.MOVE:
                touchMove(event.Points);
                break;
            case TouchEvent.UP:
                touchUp(event.Points);
                break;
        }
    };

    private void touchDown(List<TouchPoint> points) {
        touchMove(points);
    }

    TouchPoint lastPoint = null;

    private void touchMove(List<TouchPoint> points) {
        Canvas canvas = mSurfaceHolder.lockCanvas();
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
                canvas.drawPoint(p.getX(), p.getY(), mLinePaint);
                path.moveTo(p.getX(), p.getY());
                continue;
            }

            if (isFirst) {
                path.lineTo(p.getX(), p.getY());
                isFirst = false;
            } else {
                float cx = (lastPoint.getX() + p.getX()) / 2;
                float cy = (lastPoint.getY() + p.getY()) / 2;
                path.quadTo(p.getX(), p.getY(), cx, cy);
            }
            lastPoint = p;
        }
        mBmpCanvas.drawPath(path, mLinePaint);
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
            return PAINT_WIDTH;
        } else {
            float penWidth = PAINT_WIDTH * newArea / maxArea;
            penWidth = penWidth < PAINT_WIDTH / 2 ? PAINT_WIDTH / 2 : penWidth;
            return penWidth;
        }
    }

    private void touchUp(List<TouchPoint> points) {
        lastPoint = null;
    }

    SparseArray<TouchPoint> mPointMap = new SparseArray<>();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
