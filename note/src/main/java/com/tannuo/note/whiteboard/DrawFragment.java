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
                        mLineSmooth.drawLine(item.getPoints(), true);
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
            drawView.drawPoints(list);
            //drawSmoothLine(list);
            //drawLine(list);

            // mLineSmooth.drawLine(list,false);
        }
    }

    SparseArray<TouchPoint> historyMap = new SparseArray<>();
    Path mDrawPath = new Path();

    private void drawSmoothLine(List<TouchPoint> points) {
        if (null == mBmpCanvas || points.isEmpty()) {
            return;
        }

        int len = points.size();
        for (int i = 0; i < len; i++) {
            TouchPoint point = points.get(i);
            TouchPoint lastPoint = historyMap.get(point.getId());

            startDraw(lastPoint, point);
            historyMap.put(point.getId(), point);
        }
        drawBitmap();
    }

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

    // A value is used in a lowpass filter to calculate the velocity between two points.
    private float VELOCITY_FILTER_WEIGHT = 0.2f;

    // Those of values present for : ---startPoint---previousPoint----currentPoint
    private Point previousPoint;
    private Point startPoint;
    private Point currentPoint;

    // contain the last velocity. Will be used to calculate the Stroke Width
    private float lastVelocity;

    // contain the last stroke width. Will be used to calculate the Stroke Width
    private float lastWidth;

    public void startDraw(TouchPoint lastPoint, TouchPoint firstPoint) {
        if (null == lastPoint) {
            // In Action down  currentPoint, previousPoint, startPoint will be set at the same point.
            currentPoint = new Point(firstPoint.getX(), firstPoint.getY(), System.currentTimeMillis());
            previousPoint = currentPoint;
            startPoint = previousPoint;
        } else {
            // Those of values present for : ---startPoint---previousPoint----currentPoint-----
            startPoint = previousPoint;
            previousPoint = currentPoint;
            currentPoint = new Point(firstPoint.getX(), firstPoint.getY(), System.currentTimeMillis());

            // Calculate the velocity between the current point to the previous point
            float velocity = currentPoint.velocityFrom(previousPoint);

            // A simple lowpass filter to mitigate velocity aberrations.
            velocity = VELOCITY_FILTER_WEIGHT * velocity + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;

            // Caculate the stroke width based on the velocity
            float strokeWidth = getStrokeWidth(velocity);


            // Draw line to the canvasBmp canvas.
            drawLine(mBmpCanvas, mLinePaint, lastWidth, strokeWidth);

            // Tracker the velocity and the stroke width
            lastVelocity = velocity;
            lastWidth = strokeWidth;

        }
        if (null != firstPoint && firstPoint.getIsUp()) {
            startPoint = previousPoint;
            previousPoint = currentPoint;
            currentPoint = new Point(firstPoint.getX(), firstPoint.getY(), System.currentTimeMillis());
            drawLine(mBmpCanvas, mLinePaint, lastWidth, 0);
        }
    }

    private float getStrokeWidth(float velocity) {
        return STROKE_WIDTH - velocity;
    }

    // Generate mid point values
    private Point midPoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x) / 2.0f, (p1.y + p2.y) / 2, (p1.time + p2.time) / 2);
    }

    private void drawLine(Canvas canvas, Paint paint, float lastWidth, float currentWidth) {
        Point mid1 = midPoint(previousPoint, startPoint);
        Point mid2 = midPoint(currentPoint, previousPoint);
        draw(canvas, mid1, previousPoint, mid2, paint, lastWidth, currentWidth);
    }


    /**
     * This method is used to draw a smooth line. It follow "Bézier Curve" algorithm (it's Quadratic curves).
     * </br> For reference, you can see more detail here: <a href="http://en.wikipedia.org/wiki/B%C3%A9zier_curve">Wiki</a>
     * </br> We 'll draw a  smooth curves from three points. And the stroke size will be changed depend on the start width and the end width
     *
     * @param canvas       : we 'll draw on this canvas
     * @param p0           the start point
     * @param p1           mid point
     * @param p2           end point
     * @param paint        the paint is used to draw the points.
     * @param lastWidth    start stroke width
     * @param currentWidth end stroke width
     */
    private void draw(Canvas canvas, Point p0, Point p1, Point p2, Paint paint, float lastWidth, float currentWidth) {
        float xa, xb, ya, yb, x, y;
        float different = (currentWidth - lastWidth);

        for (float i = 0; i < 1; i += 0.01) {
            // This block of code is used to calculate next point to draw on the curves
            xa = getPt(p0.x, p1.x, i);
            ya = getPt(p0.y, p1.y, i);
            xb = getPt(p1.x, p2.x, i);
            yb = getPt(p1.y, p2.y, i);

            x = getPt(xa, xb, i);
            y = getPt(ya, yb, i);
            //

            // reset strokeWidth
            paint.setStrokeWidth(lastWidth + different * (i));
            canvas.drawPoint(x, y, paint);
        }
    }


    // This method is used to calculate the next point cordinate.
    private float getPt(float n1, float n2, float perc) {
        float diff = n2 - n1;
        return n1 + (diff * perc);
    }


    /**
     * This method is used to save the bitmap to an output stream
     *
     * @param outputStream
     */
//    public void save(OutputStream outputStream) {
//        Bitmap bitmap = getDrawingCache();
//        if (bitmap != null) {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
//        }
//    }


    public class Point {
        public final float x;
        public final float y;
        public final long time;

        public Point(float x, float y, long time) {
            this.x = x;
            this.y = y;
            this.time = time;
        }

        /**
         * Caculate the distance between current point to the other.
         *
         * @param p the other point
         * @return
         */
        private float distanceTo(Point p) {
            return (float) (Math.sqrt(Math.pow((x - p.x), 2) + Math.pow((y - p.y), 2)));
        }


        /**
         * Caculate the velocity from the current point to the other.
         *
         * @param p the other point
         * @return
         */
        public float velocityFrom(Point p) {
            return distanceTo(p) / (this.time - p.time);
        }
    }

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

        private void drawLine(List<TouchPoint> points, boolean isUp) {
            if (null == points || points.size() == 0) {
                return;
            }
            if (mLastPoint == null) {
                mLastPoint = points.get(0);
            }

            DrawUtil.getInstance().moveTo(mDrawPath, mLastPoint.getX(), mLastPoint.getY(), mPaintWidth, mPaintHeight);
            int[][] rawPointArray = toPointArray(points);
            int len = points.size();
            for (int i = 0; i < len; i++) {
                Logger.d(TAG, String.format("/-----  rawPointArray:x=%s,y=%s,id=%s", rawPointArray[i][0], rawPointArray[i][1], rawPointArray[i][2]));
                int[][] pointArray = mSmooth.Point_Filter(rawPointArray[i], false);
                if (null != pointArray) {
                    for (int j = 0; j < pointArray.length; j++) {
                        Logger.d(TAG, String.format("/++++ translate pointArray:x=%s,y=%s,id=%s", pointArray[j][0], pointArray[j][1], pointArray[j][2]));
                        Logger.d(TAG, String.format("/****  translate point:x=%s,y=%s,id=%s",
                                TouchPoint.getScaleX(pointArray[j][0]), TouchPoint.getScaleY(pointArray[j][1]), pointArray[j][2]));
                        mDrawPath.lineTo(TouchPoint.getScaleX(pointArray[j][0]), TouchPoint.getScaleY(pointArray[j][1]));
                        mLastPoint = new TouchPoint();
                        mLastPoint.setId(pointArray[j][2]);
                        mLastPoint.setX((short) pointArray[j][0]);
                        mLastPoint.setY((short) pointArray[j][1]);
                    }
                    if (isUp) {
                        mLastPoint = null;
                    }
                }
            }
            mBmpCanvas.drawPath(mDrawPath, mLinePaint);
            drawBitmap();
            mDrawPath.reset();
        }

    }
}
