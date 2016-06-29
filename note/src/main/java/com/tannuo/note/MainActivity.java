package com.tannuo.note;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tannuo.note.utility.WakeLock;
import com.tannuo.note.whiteboard.DrawFragment;
import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.TouchScreenListenerImpl;
import com.tannuo.sdk.bluetooth.device.BTDeviceFactory;
import com.tannuo.sdk.bluetooth.device.IDevice;
import com.tannuo.sdk.bluetooth.device.TouchEvent;
import com.tannuo.sdk.bluetooth.device.TouchPoint;
import com.tannuo.sdk.bluetooth.protocol.ProtocolHandler;
import com.tannuo.sdk.util.DataLog;
import com.tannuo.sdk.util.HexUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.meta.SubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfoIndex;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Bind(R.id.edtName)
    EditText edtName;
    @Bind(R.id.txtCount)
    TextView txtCount;
    @Bind(R.id.txtDevice)
    TextView txtDevice;
    @Bind(R.id.txtStartDate)
    TextView txtStartDate;
    @Bind(R.id.txtDuration)
    TextView txtDuration;
    @Bind(R.id.txtPointStatus)
    TextView txtPointLen;
    @Bind(R.id.txt_Bytes)
    TextView txtBytes;
    @Bind(R.id.radio_log)
    RadioButton radioLog;
    @Bind(R.id.radio_draw)
    RadioButton radioDraw;

    LogFragment mLogFragment;
    DrawFragment mDrawFragment;
    Fragment mCurrentFragment;
    IDevice mService;
    TouchScreenListener mTouchScreenListener;
    WakeLock mWakeLock;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            edtName.getViewTreeObserver().addOnWindowFocusChangeListener((isFocused) -> {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtName.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            });
        }
        mTouchScreenListener = new TouchListener();
        mLogFragment = new LogFragment();
        final FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.add(R.id.layout_data, mLogFragment).commit();
        mCurrentFragment = mLogFragment;

        radioLog.setOnCheckedChangeListener((view, isChecked) -> {
            if (!isChecked) {
                return;
            }
            FragmentTransaction trans1 = getFragmentManager().beginTransaction();
            trans1.replace(R.id.layout_data, mLogFragment)
                    .addToBackStack(mLogFragment.getClass().getSimpleName()).commit();
            mCurrentFragment = mLogFragment;
        });
        radioDraw.setOnCheckedChangeListener((view, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (mDrawFragment == null) {
                mDrawFragment = new DrawFragment();
            }
            FragmentTransaction trans1 = getFragmentManager().beginTransaction();
            trans1.replace(R.id.layout_data, mDrawFragment)
                    .addToBackStack(mDrawFragment.getClass().getSimpleName()).commit();
            mCurrentFragment = mDrawFragment;
        });

        mWakeLock = new WakeLock(this);
        mWakeLock.lockScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWakeLock.unlockAll();
        // disconnect();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideInputMethod();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void hideInputMethod() {
        edtName.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtName.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }, 0 * 1000);
    }

    @OnClick({R.id.btnConnect, R.id.btnDisconnect, R.id.btnClear, R.id.btn_qr_code})
    void buttonClick(View view) {
        int id = view.getId();
        if (id == R.id.btnConnect) {
            //testEnentBus();
            this.connect();
        } else if (id == R.id.btnDisconnect) {
            this.disconnect();
        } else if (id == R.id.btnClear) {
            this.clear();
        } else if (id == R.id.btn_qr_code) {
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setOrientationLocked(false);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            intentIntegrator.initiateScan();
            lastScan = SystemClock.currentThreadTimeMillis();
        }
    }

    long lastScan = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            long duration = SystemClock.currentThreadTimeMillis() - lastScan;
            Log.e(TAG, String.format("/**** QRCode scans elapsed:%s ms ****/", duration));
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            Log.i(TAG, result.toString());
            String content = result.getContents();
            edtName.setText(content);
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
        }
    }

    private void connect() {
        if (null == mService) {
            txtDevice.setText("正在连接.......");
            BTDeviceFactory factory = new BTDeviceFactory();
            mService = factory.get(this, this.mTouchScreenListener);
            mService.connect(getDeviceName());
            isStarted = true;
        }
    }

    private String getDeviceName() {
        return this.edtName.getText().toString();
    }

    private void disconnect() {
        if (mService != null) {
            mService.disconnect();
            mService = null;
            txtDevice.setText("");

            isStarted = false;
            stopTimer();
        }
    }

    private void clear() {
        if (mCurrentFragment == mLogFragment) {
            mLogFragment.clearData();
        }
        this.txtCount.setText("0");
        DataLog.getInstance().restart();
        rowIndex = 0;
        byteCount = 0;
        txtStartDate.setText("");
        txtDuration.setText("");
        txtBytes.setText("");
        stopTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class TouchListener extends TouchScreenListenerImpl implements ProtocolHandler.OnDataReceive {
        private TouchEvent getTouchEvent(int mode, final List<TouchScreen.TouchPoint> points) {
            TouchEvent result = new TouchEvent();
            result.Mode = mode;
            result.Points = new ArrayList<>();
            for (TouchScreen.TouchPoint p : points) {
                result.Points.add(new TouchPoint(p));
            }
            return result;
        }

        @Override
        public void onTouchUp(List<TouchScreen.TouchPoint> upPoints) {
            super.onTouchUp(upPoints);
            if (mCurrentFragment == mDrawFragment) {
                TouchEvent event = getTouchEvent(TouchEvent.UP, upPoints);
                mDrawFragment.onTouched(event);
            }
        }

        @Override
        public void onTouchDown(List<TouchScreen.TouchPoint> downPoints) {
            super.onTouchDown(downPoints);
            if (mCurrentFragment == mDrawFragment) {
                TouchEvent event = getTouchEvent(TouchEvent.DOWN, downPoints);
                mDrawFragment.onTouched(event);
            }
        }

        @Override
        public void onTouchMove(List<TouchScreen.TouchPoint> movePoints) {
            super.onTouchMove(movePoints);
            if (mCurrentFragment == mDrawFragment) {
                TouchEvent event = getTouchEvent(TouchEvent.MOVE, movePoints);
                mDrawFragment.onTouched(event);
            }
        }

        @Override
        public void onError(int errorCode) {
            Log.v(TAG, "onError " + errorCode);
            MainActivity.this.runOnUiThread(() -> {
                disconnect();
                txtDevice.setText("设备连接失败");
            });
        }

        @Override
        public void onBLConnected() {
            MainActivity.this.runOnUiThread(() -> {
                txtDevice.setText(String.format("当前设备:%s", MainActivity.this.getDeviceName()));
                Toast.makeText(MainActivity.this, String.format("Connected:%s", MainActivity.this.getDeviceName()),
                        Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onReceive(byte[] data) {
            String str = HexUtil.byteToString(data) + "\r\n";
            rowIndex++;
            byteCount += data.length;

            MainActivity.this.runOnUiThread(() -> {
                if (!isStarted) {
                    return;
                }
                synchronized (this) {
                    if (timer == null) {
                        startDate = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        txtStartDate.setText(sdf.format(startDate));
                        startTimer();
                    }
                }

                if (mCurrentFragment == mLogFragment) {
                    mLogFragment.appendData(str);
                }
                txtCount.setText(String.valueOf(byteCount));

                if (data.length > 3) {
                    byte len = data[1];
                    byte dataFeature = data[2];
                    if (lastFeature != dataFeature) {
                        lastFeature = dataFeature;
                        String[] statusStr = {"无状态和长宽", "无长宽", "数据完整"};
                        if (dataFeature >= 0 && dataFeature <= 2) {
                            txtPointLen.setText(statusStr[dataFeature]);
                            if (dataFeature == 2) {
                                txtPointLen.setTextColor(Color.BLACK);
                            } else {
                                txtPointLen.setTextColor(Color.RED);
                            }
                        } else {
                        }
                    }
                }
            });
        }
    }

    int rowIndex;
    int byteCount;
    boolean isStarted = true;
    byte lastFeature = -1;
    Date startDate;
    Timer timer;
    TimerTask task;
    int timeCounter = 0;
    int lastBytCount = 0;

    private void startTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Date dtNow = new Date();
                long duration = dtNow.getTime() - startDate.getTime();
//                Calendar calendar = Calendar.getInstance(Locale.CHINA);
//                calendar.setTimeInMillis(duration);
//                Date dt = calendar.getTime();
                Date dt = new Date(duration);
                timeCounter++;
                int dataCount = byteCount - lastBytCount;
                lastBytCount = byteCount;
                runOnUiThread(() -> {
//                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
//                    txtDuration.setText(sdf.format(dt));
                    txtDuration.setText(String.format("%s 秒", timeCounter));
                    txtBytes.setText(String.valueOf(dataCount));
                });

            }
        };
        timeCounter = 0;
        timer.schedule(task, 1 * 1000, 1 * 1000);
    }

    private void stopTimer() {
        if (null != timer) {
            task.cancel();
            timer.cancel();
            task = null;
            timer = null;
            timeCounter = 0;
        }
    }


    private void testMetric() {
        DisplayMetrics dMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    }

    private void testEnentBus() {
        EventBus.builder().addIndex(new SubscriberInfoIndex() {
            @Override
            public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
                return null;
            }
        }).installDefaultEventBus();

        EventBus.getDefault().register(this);
        EventBus.getDefault().post("test");

        ServerAPI api = new ServerAPI();
        api.getServerConfig();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = false, priority = 0)
    public void handleEvent(Object obj) {

    }
}
