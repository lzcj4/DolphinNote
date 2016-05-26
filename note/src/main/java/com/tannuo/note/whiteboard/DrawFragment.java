package com.tannuo.note.whiteboard;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.tannuo.note.R;
import com.tannuo.sdk.bluetooth.connectservice.ConnectService;
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
    private ConnectService mConnectService;
    private Canvas mBmpCanvas;
    private Paint mLinePaint, mBmpPaint, mRubberPaint;
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

        mLinePaint = initialBrush();

        mBmpPaint = initialBrush();
        mRubberPaint = initialBrush();
        mRubberPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRubberPaint.setColor(Color.BLACK);

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

    private Paint initialBrush() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setStrokeWidth(STROKE_WIDTH);
        // mLinePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new CornerPathEffect(STROKE_WIDTH / 2));
        paint.setColor(Color.WHITE);
        // result.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private void setPaintWidthAndHeight(int width, int height) {
        TouchPoint.setCanvas(width, height);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBmpCanvas = new Canvas(mBitmap);
        mBmpCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        mWidthRatio = width / TouchPoint.MAX_X;
        mHeightRatio = height / TouchPoint.MAX_Y;
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
                touchUp(event.Points);
                break;
        }
    };

    private void touchDown(List<TouchPoint> points) {

        drawLine(points);
    }

    TouchPoint lastPoint = null;
    Path mDrawPath = new Path();

    private void drawLine(List<TouchPoint> points) {
        if (lastPoint == null) {
            lastPoint = points.get(0);
        }

        DrawUtil.getInstance().moveTo(mDrawPath, lastPoint.getX(), lastPoint.getY(), mPaintWidth, mPaintHeight);
        int len = points.size();

        for (int i = 0; i < len; i++) {
            TouchPoint p = points.get(i);
            float lineWidth = getPaintWidth(p);
            mLinePaint.setStrokeWidth(lineWidth);
            mLinePaint.setPathEffect(new CornerPathEffect(lineWidth / 2));

            if (p.isLongDistance(lastPoint)) {
                Logger.e(TAG, String.format("Id1:%s to Id2:%s, len:%s",
                        lastPoint.getID(), p.getID(), p.distance(lastPoint)));
                lastPoint = p;
                DrawUtil.getInstance().moveTo(mDrawPath, lastPoint.getX(), lastPoint.getY(), mPaintWidth, mPaintHeight);
                continue;
            }

            if (drawRubber(p)) {
                continue;
            }
            DrawUtil.getInstance().lineTo(mDrawPath, p.getX(), p.getY(), mPaintWidth, mPaintHeight);
            lastPoint = p;
        }
        mBmpCanvas.drawPath(mDrawPath, mLinePaint);
        mDrawPath.reset();
        drawBitmap();
    }

    private boolean drawRubber(TouchPoint p) {
        boolean result = false;
        if (p.isRubber()) {
            float r = TouchPoint.getScaleX(p.getWidth() + p.getHeight()) / 4;
            DrawUtil.getInstance().drawCircle(mBmpCanvas, p.getX(), p.getY(),
                    r, mPaintWidth, mPaintHeight, mRubberPaint);
            result = true;
        }
        return result;
    }

    private void drawBitmap() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        canvas.drawBitmap(mBitmap, 0, 0, mBmpPaint);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
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
