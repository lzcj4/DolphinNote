package com.tannuo.note.whiteboard;

import android.content.Context;
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
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.tannuo.sdk.device.TouchPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 2016/8/13.
 */
public class PointDrawView extends FrameLayout {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBitmap;
    private Canvas mBmpCanvas;
    private Paint mLinePaint, mBmpPaint, mRubberPaint;
    private int mPaintWidth, mPaintHeight;
    private SparseArray<TouchPoint> historyMap = new SparseArray<>();
    private Path mDrawPath = new Path();

    public PointDrawView(Context context) {
        this(context, null);
    }

    public PointDrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSurfaceView = new SurfaceView(context, attrs, defStyleAttr);
        mSurfaceView.setBackgroundColor(Color.WHITE);
        mSurfaceView.setZOrderOnTop(true);
        this.addView(mSurfaceView);
        mLinePaint = initialBrush();
        mBmpPaint = initialBrush();
        mRubberPaint = initialBrush();
        mRubberPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRubberPaint.setColor(Color.WHITE);

        mSurfaceHolder = mSurfaceView.getHolder();
        if (null != mSurfaceHolder) {
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
        }
        mSurfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    mSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mPaintWidth = mSurfaceView.getWidth();
                mPaintHeight = mSurfaceView.getHeight();
                mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
                mSurfaceView.setZOrderOnTop(true);
                float defaultWHRatio = mPaintWidth / (float) mPaintHeight;
                float designedWHRatio = TouchPoint.WIDTHHEIGHTRATIO;
                setPaintWidthAndHeight(mPaintWidth, mPaintHeight);
            }
        });
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

    }

    public void drawPoints(List<TouchPoint> points) {
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
            startDraw(list);
            // drawLine(list);
        }
    }

    private void startDraw(List<TouchPoint> points) {
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

    public void clear() {
        mBmpCanvas.drawRect(0, 0, mPaintWidth, mPaintHeight, mRubberPaint);
        drawBitmap();
    }

    //The stroke width (default will be 7 pixels).
    private int STROKE_WIDTH = 7;

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

            // Calculate the stroke width based on the velocity
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
    Path mPath = new Path();

    private void draw(Canvas canvas, Point p0, Point p1, Point p2, Paint paint, float lastWidth, float currentWidth) {
        float xa, xb, ya, yb, x, y;
        float different = (currentWidth - lastWidth);
//        mPath.moveTo(p1.x, p1.y);
//        paint.setStrokeWidth(lastWidth);
        for (float i = 0; i < 1; i += 0.01) {
            // This block of code is used to calculate next point to draw on the curves
            xa = getPt(p0.x, p1.x, i);
            ya = getPt(p0.y, p1.y, i);
            xb = getPt(p1.x, p2.x, i);
            yb = getPt(p1.y, p2.y, i);

            x = getPt(xa, xb, i);
            y = getPt(ya, yb, i);
            // reset strokeWidth
            paint.setStrokeWidth(lastWidth + different * (i));
            //mPath.lineTo(x, y);
            canvas.drawPoint(x, y, paint);
        }
        // canvas.drawPath(mPath, paint);
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

}
