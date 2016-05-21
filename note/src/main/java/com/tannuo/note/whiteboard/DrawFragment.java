package com.tannuo.note.whiteboard;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.tannuo.note.R;
import com.tannuo.sdk.bluetooth.connectservice.MockConnectService;
import com.tannuo.sdk.bluetooth.connectservice.TouchEvent;
import com.tannuo.sdk.bluetooth.connectservice.TouchListener;
import com.tannuo.sdk.bluetooth.connectservice.TouchPoint;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DrawFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DrawFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    @Bind(R.id.surfaceView)
    android.view.SurfaceView surfaceView;

    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBitmap;
    private MockConnectService mockConnectService;
    private Canvas mCanvas;
    private Paint mPaint;
    private int mPaintWidth;
    private int mPaintHeight;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // TODO: Rename and change types and number of parameters
    public static DrawFragment newInstance(String param1, String param2) {
        DrawFragment fragment = new DrawFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_draw, container, false);
        ButterKnife.bind(this, view);

        mSurfaceHolder = surfaceView.getHolder();
        // surfaceView.setBackgroundColor(Color.WHITE);
        mockConnectService = new MockConnectService(mTouchListener);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(10);
        mPaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);

        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mPaintWidth = surfaceView.getWidth();
                        mPaintHeight = surfaceView.getHeight();
                        TouchPoint.setCanvas(mPaintWidth, mPaintHeight);
                        mBitmap = Bitmap.createBitmap(mPaintWidth, mPaintHeight, Bitmap.Config.ARGB_8888);
                        mCanvas = new Canvas(mBitmap);
                        mockConnectService.connect("test");
                    }
                });

        return view;
    }

    private TouchListener mTouchListener = new TouchListener() {
        @Override
        public void onTouch(final TouchEvent event) {
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
        for (TouchPoint p : points) {

            float cx = (lastPoint.getX() + p.getX()) / 2;
            float cy = (lastPoint.getY() + p.getY()) / 2;
            path.quadTo(cx, cy, p.getX(), p.getY());
            //path.lineTo(p.getX(), p.getY());

            lastPoint = p;
        }
        mCanvas.drawPath(path, mPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawPath(path, mPaint);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
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
