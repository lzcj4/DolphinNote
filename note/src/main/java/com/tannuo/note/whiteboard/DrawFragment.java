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
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.tannuo.note.R;
import com.tannuo.sdk.device.TouchEvent;
import com.tannuo.sdk.device.TouchFrame;
import com.tannuo.sdk.device.TouchPath;
import com.tannuo.sdk.device.TouchPoint;
import com.tannuo.sdk.device.TouchPointListener;
import com.tannuo.sdk.util.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DrawFragment extends Fragment implements TouchPointListener {
    private final String TAG = this.getClass().getSimpleName();
    @Bind(R.id.surfaceView)
    SurfaceView surfaceView;
    @Bind(R.id.drawView)
    PointDrawView drawView;

    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBitmap;
    private Canvas mBmpCanvas;
    private Paint mLinePaint, mBmpPaint, mRubberPaint;
    private int mPaintWidth, mPaintHeight;
    private float mWidthRatio, mHeightRatio;

    private final float OFFSET = 1.f;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_draw, container, false);
        ButterKnife.bind(this, view);

        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mSurfaceHolder = null;
            }
        });

        mLinePaint = initialBrush();
        mBmpPaint = initialBrush();
        mRubberPaint = initialBrush();
        mRubberPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRubberPaint.setColor(Color.WHITE);

        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mPaintWidth = surfaceView.getWidth();
                mPaintHeight = surfaceView.getHeight();
                mSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
                surfaceView.setZOrderOnTop(true);
//                float currentWHRatio = mPaintWidth / (float) mPaintHeight;
//                float designedWHRatio = TouchPoint.WIDTHHEIGHTRATIO;
//
//                if (currentWHRatio >= designedWHRatio) {
//                    //keep height and adjust width
//                    mPaintWidth = (int) (designedWHRatio * mPaintHeight);
//                    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
//                    lp.width = mPaintWidth;
//                    lp.height = mPaintHeight;
//                    surfaceView.setLayoutParams(lp);
//                    //surfaceView.getHolder().setFixedSize(lp.width, lp.height);
//                } else {
//                    //keep width and adjust height
//                    mPaintHeight = (int) (mPaintWidth / designedWHRatio);
//                    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
//                    lp.width = mPaintWidth;
//                    lp.height = mPaintHeight;
//                    surfaceView.setLayoutParams(lp);
//                    //surfaceView.getHolder().setFixedSize(lp.width, lp.height);
//                }

                setPaintWidthAndHeight(mPaintWidth, mPaintHeight);
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
        paint.setColor(Color.BLACK);
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

    @Override
    public void onTouchEvent(TouchEvent touchEvent) {
        if (null == mSurfaceHolder) {
            Logger.d(TAG, "Current surface view is destoried");
            return;
        }

        switch (touchEvent.getAction()) {
            case TouchEvent.ACTION_TOUCH:
                TouchFrame downFrame = touchEvent.getDownFrame();
                TouchFrame moveFrame = touchEvent.getMoveFrame();
                TouchFrame upFrame = touchEvent.getUpFrame();
                if (!downFrame.isEmpty()) {
                    for (TouchPath item : downFrame) {
                        touchDown(item.getPoints());
                    }
                }
                if (!moveFrame.isEmpty()) {
                    for (TouchPath item : moveFrame) {
                        touchDown(item.getPoints());
                    }
                }
                if (!upFrame.isEmpty()) {
                    for (TouchPath item : upFrame) {
                        touchUp(item.getPoints());
                        List<TouchPoint> list = item.getPoints();
                        if (list.size() > 0 && list.get(0).isRubber()) {
                            for (TouchPoint point : list) {
                                drawRubber(point);
                            }
                        } else {
                            mLineSmooth.drawLine(list, true);
                        }
                    }
                }
                break;

            case TouchEvent.ACTION_SNAPSHOT:
                Logger.d(TAG, "/+++ Device snapshot triggered +++/");
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(int errorCode) {

    }

    LineSmooth mLineSmooth = new LineSmooth();

    private void touchDown(List<TouchPoint> points) {
        SparseArray<List<TouchPoint>> groups = new SparseArray<>();
        for (TouchPoint item : points) {
            List<TouchPoint> list = groups.get(item.getId());
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(item);
            groups.put(item.getId(), list);
        }

        for (int i = 0; i < groups.size(); i++) {
            List<TouchPoint> list = groups.valueAt(i);
            //drawView.drawPoints(list);
            if (list.size() > 0 && list.get(0).isRubber()) {
                for (TouchPoint point : list) {
                    drawRubber(point);
                }
            } else {
                mLineSmooth.drawLine(list, false);
            }
        }
    }

    SparseArray<TouchPoint> historyMap = new SparseArray<>();
    Path mDrawPath = new Path();


    private void drawLine(List<TouchPoint> points) {
        if (null == mBmpCanvas || points.isEmpty()) {
            return;
        }

        TouchPoint firstPoint = points.get(0);
        TouchPoint lastPoint = historyMap.get(firstPoint.getId());
        if (lastPoint == null) {
            lastPoint = points.get(0);
        }

        DrawUtil.getInstance().moveTo(mDrawPath, lastPoint.getX(), lastPoint.getY(), mPaintWidth, mPaintHeight);
        int len = points.size();

        for (int i = 0; i < len; i++) {
            TouchPoint p = points.get(i);
            Logger.i(TAG, String.format("Id1:%s x0=%s,y0=%s to Id2:%s, x1=%s ,y1=%s  len:%s",
                    lastPoint.getId(), lastPoint.getX(), lastPoint.getY(),
                    p.getId(), p.getX(), p.getY(), p.distance(lastPoint)));

//            if (p.isLongDistance(lastPoint)) {
//                lastPoint = p;
//                DrawUtil.getInstance().moveTo(mDrawPath, lastPoint.getX(), lastPoint.getY(), mPaintWidth, mPaintHeight);
//                continue;
//            }

            if (drawRubber(p)) {
                continue;
            }

            float dx = lastPoint.getX() - p.getX();
            float dy = lastPoint.getY() - p.getY();
            if (Math.abs(dx) >= OFFSET || Math.abs(dy) >= OFFSET) {

//                mDrawPath.quadTo((lastPoint.getX() + p.getX()) / 2, (lastPoint.getY() + p.getY()) / 2,
//                        p.getX(), p.getY());
                DrawUtil.getInstance().lineTo(mDrawPath, p.getX(), p.getY(), mPaintWidth, mPaintHeight);
            } else {
                Logger.i(TAG, "Invalid draw length");
            }
            lastPoint = p;
            historyMap.put(p.getId(), p);
        }

        mBmpCanvas.drawPath(mDrawPath, mLinePaint);
        drawBitmap();
        mDrawPath.reset();
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

    Rect mDirtyRect = new Rect();

    private void drawBitmap() {
        if (null == mSurfaceHolder) {
            return;
        }
        RectF rectF = new RectF();
        mDrawPath.computeBounds(rectF, true);
        Rect rect = new Rect();
        rectF.round(rect);

        Canvas canvas = null;
        try {
            canvas = mSurfaceHolder.lockCanvas();
            //Lead to line broken
            //  canvas = mSurfaceHolder.lockCanvas(rect);
            if (null == canvas) {
                return;
            }
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.drawBitmap(mBitmap, 0, 0, mBmpPaint);
        } finally {
            if (null != canvas)
                mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void touchUp(List<TouchPoint> points) {
        for (TouchPoint item : points) {
            historyMap.remove(item.getId());

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void clear() {
        mBmpCanvas.drawRect(0, 0, mPaintWidth, mPaintHeight, mRubberPaint);
        drawBitmap();
    }

    //The stroke width (default will be 7 pixels).
    private int STROKE_WIDTH = 5;


    private class LineSmooth {
        BezierSmooth mSmooth = new BezierSmooth();

        private int[][] toPointArray(List<TouchPoint> points) {
            int len = points.size();
            int[][] result = new int[len][3];
            for (int i = 0; i < len; i++) {
                TouchPoint p = points.get(i);
                result[i][0] = p.getRawX();
                result[i][1] = p.getRawY();
                result[i][2] = p.getId();
            }
            return result;
        }

        TouchPoint mLastPoint = null;

        private synchronized void drawLine(List<TouchPoint> points, boolean isUp) {
            if (null == points || points.size() == 0) {
                return;
            }

            if (mLastPoint == null) {
                mLastPoint = points.get(0);
                mDrawPath.reset();
            }

            boolean isDrawed = false;
            int[][] rawPointArray = toPointArray(points);
            int len = points.size();
            for (int i = 0; i < len; i++) {
                Logger.d(TAG, String.format("/-----  rawPointArray:x=%s,y=%s,id=%s", rawPointArray[i][0], rawPointArray[i][1], rawPointArray[i][2]));
                int[][] pointArray = mSmooth.Point_Filter(rawPointArray[i], isUp);
                if (null == pointArray) {
                    continue;
                }

                float moveX = mLastPoint.getX(), moveY = mLastPoint.getY();
                mDrawPath.moveTo(moveX, moveY);
                for (int j = 0; j < pointArray.length; j++) {
                    Logger.d(TAG, String.format("/++++ translate pointArray:x=%s,y=%s,id=%s", pointArray[j][0], pointArray[j][1], pointArray[j][2]));
                    Logger.d(TAG, String.format("/****  translate point:x=%s,y=%s,id=%s",
                            TouchPoint.getScaleX(pointArray[j][0]), TouchPoint.getScaleY(pointArray[j][1]), pointArray[j][2]));
                    mDrawPath.lineTo(TouchPoint.getScaleX(pointArray[j][0]), TouchPoint.getScaleY(pointArray[j][1]));
                    mLastPoint = new TouchPoint();
                    mLastPoint.setId(pointArray[j][2]);
                    mLastPoint.setX((short) pointArray[j][0]);
                    mLastPoint.setY((short) pointArray[j][1]);
                    isDrawed = true;
                }
            }

            if (isDrawed) {
                mBmpCanvas.drawPath(mDrawPath, mLinePaint);
                drawBitmap();
                mDrawPath.reset();
            }

            if (isUp) {
                mLastPoint = null;
                mDrawPath.reset();
            }
        }

    }
}
