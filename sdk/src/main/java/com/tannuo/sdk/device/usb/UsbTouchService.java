package com.tannuo.sdk.device.usb;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.tannuo.sdk.util.Logger;

public class UsbTouchService extends Service implements View.OnTouchListener {
    private final String TAG = this.getClass().getSimpleName();
    private WindowManager winMgr;
    private LinearLayout layout;

    public UsbTouchService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        layout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(30, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        layout.setOnTouchListener(this);

        winMgr = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams winParams = new WindowManager.LayoutParams(30, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        winParams.gravity = Gravity.LEFT | Gravity.TOP;
        winMgr.addView(layout, winParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != winMgr) {
            winMgr.removeView(layout);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_MOVE ||
                event.getAction() == MotionEvent.ACTION_UP) {
            Logger.d(TAG, String.format("x:%s , y:%s", event.getX(), event.getY()));
            //event.gethistori
        }
        return true;
    }
}
