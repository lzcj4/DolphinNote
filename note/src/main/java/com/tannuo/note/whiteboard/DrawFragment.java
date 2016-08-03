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
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.tannuo.note.R;
import com.tannuo.sdk.device.TouchEvent;
import com.tannuo.sdk.device.TouchFrame;
import com.tannuo.sdk.device.TouchPath;
import com.tannuo.sdk.device.TouchPoint;
import com.tannuo.sdk.device.TouchPointListener;
import com.tannuo.sdk.device.server.DefaultSubscribe;
import com.tannuo.sdk.device.server.ServerAPI;
import com.tannuo.sdk.device.server.ServerAPITest;
import com.tannuo.sdk.util.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DrawFragment extends Fragment implements TouchPointListener {
    private final String TAG = this.getClass().getSimpleName();
    @Bind(R.id.surfaceView)
    android.view.SurfaceView surfaceView;

    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBitmap;
    private Canvas mBmpCanvas;
    private Paint mLinePaint, mBmpPaint, mRubberPaint;
    private int mPaintWidth, mPaintHeight;
    private float mWidthRatio, mHeightRatio;

    private final float STROKE_WIDTH = 6.0f;
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
        //surfaceView.setBackgroundColor(Color.WHITE);

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

//                float defaultWHRatio = mPaintWidth / (float) mPaintHeight;
//                float designedWHRatio = TouchPoint.WIDTHHEIGHTRATIO;
//
//                if (defaultWHRatio >= designedWHRatio) {
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
                    uploadData(downFrame);
                }
                if (!moveFrame.isEmpty()) {
                    for (TouchPath item : moveFrame) {
                        drawLine(item.getPoints());
                    }
                    uploadData(moveFrame);
                }
                if (!upFrame.isEmpty()) {
                    for (TouchPath item : upFrame) {
                        touchUp(item.getPoints());
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

    private void uploadData(TouchFrame frame) {

        ServerAPI.getInstance().postConfData(ServerAPITest.meetingUrl, ServerAPITest.meetingId, frame, new DefaultSubscribe<Response<ResponseBody>>() {
            @Override
            public void onNext(Response<ResponseBody> responseBodyResponse) {
                super.onNext(responseBodyResponse);
                int code = responseBodyResponse.raw().code();
                if (code == 200) {
                    Logger.i(TAG, "upload succeed");
                }
            }
        });
    }

    @Override
    public void onError(int errorCode) {

    }

    LineSmooth lineSmooth = new LineSmooth();

    private void touchDown(List<TouchPoint> points) {
        drawLine(points);
        //lineSmooth.drawLine(points);
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
//            float lineWidth = getPaintWidth(p);
//            mLinePaint.setStrokeWidth(lineWidth);
//            mLinePaint.setPathEffect(new CornerPathEffect(lineWidth / 2));
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
            //canvas = mSurfaceHolder.lockCanvas();
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            //canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(mBitmap, 0, 0, mBmpPaint);
        } finally {
            if (null != canvas)
                mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    int maxArea = 0;

    private float getPaintWidth(TouchPoint newPoint) {
        TouchPoint lastPoint = historyMap.get(newPoint.getId());
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

    private class LineSmooth {
        Smooth mSmooth = new Smooth();
        TouchPoint lastPoint;

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

        private List<TouchPoint> toTouchPoints(int[][] pointArray) {
            int len = pointArray.length;
            List<TouchPoint> result = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                TouchPoint p = new TouchPoint((byte) pointArray[i][0], (short) pointArray[i][1], (short) pointArray[i][2]);
                result.add(p);
            }
            return result;
        }

        private void drawLine(List<TouchPoint> points) {
            int[][] pointArray = toPointArray(points);
            pointArray = mSmooth.smoothLine(pointArray);

            List<TouchPoint> newPoints = toTouchPoints(pointArray);
            TouchPoint firstPoint = newPoints.get(0);
            int len = newPoints.size();
            if (lastPoint != null && lastPoint.getId() == firstPoint.getId()) {
                mBmpCanvas.drawLine(lastPoint.getX(), lastPoint.getY(), firstPoint.getX(), firstPoint.getY(), mLinePaint);
            }
            for (int i = 1; i < len; i++) {
                if (newPoints.get(i - 1).getId() == newPoints.get(i).getId()) {
                    mBmpCanvas.drawLine(newPoints.get(i - 1).getX(), newPoints.get(i - 1).getY()
                            , newPoints.get(i).getX(), newPoints.get(i).getY(), mLinePaint);
                }
            }

            drawBitmap();
            lastPoint = newPoints.get(len - 1);
        }

    }
}
