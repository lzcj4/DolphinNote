package com.tannuo.note;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tannuo.jy.BLCommService;
import com.tannuo.jy.IrmtInt;
import com.tannuo.note.utility.SettingPref;
import com.tannuo.note.utility.WakeLock;
import com.tannuo.note.whiteboard.DrawFragment;
import com.tannuo.sdk.device.DeviceFactory;
import com.tannuo.sdk.device.TouchDeviceImpl;
import com.tannuo.sdk.device.TouchDeviceListener;
import com.tannuo.sdk.device.TouchEvent;
import com.tannuo.sdk.device.bluetooth.IDevice;
import com.tannuo.sdk.device.bluetooth.IDeviceFactory;
import com.tannuo.sdk.device.protocol.IProtocol;
import com.tannuo.sdk.device.protocol.ProtocolFactory;
import com.tannuo.sdk.device.protocol.ProtocolHandler;
import com.tannuo.sdk.device.protocol.ProtocolType;
import com.tannuo.sdk.device.server.ServerAPITest;
import com.tannuo.sdk.util.DataLog;
import com.tannuo.sdk.util.HexUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
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
    @Bind(R.id.drawer_Layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.content_layout)
    LinearLayout contentLayout;
    //    @Bind(R.id.slide_layout)
//    FrameLayout slideLayout;
    @Bind(R.id.drawer)
    NavigationView drawerView;

    LogFragment mLogFragment;
    DrawFragment mDrawFragment;
    Fragment mCurrentFragment;
    IDevice mDevice;
    TouchDeviceListener mTouchDeviceListener;
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
        mTouchDeviceListener = new TouchDeviceListenerImpl();
        mLogFragment = new LogFragment();
        final FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.add(R.id.layout_data, mLogFragment).commit();
        mCurrentFragment = mLogFragment;
        drawerView.setNavigationItemSelectedListener((item) -> {
            if (item.getItemId() == R.id.menu_setting) {
                this.startActivity(new Intent(this, SettingsActivity.class));
            }
            return true;
        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.txt_open, R.string.txt_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
//                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
//                layoutParams.setMargins((int)slideOffset,0,0,0);
//                contentLayout.setLayoutParams(layoutParams);
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
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
            testEventBus();
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

    @OnCheckedChanged({R.id.radio_log, R.id.radio_draw})
    void radioChecked(RadioButton view, boolean isChecked) {
        if (!isChecked) {
            return;
        }

        FragmentTransaction trans = getFragmentManager().beginTransaction();
        if (view.getId() == R.id.radio_log) {
            trans.replace(R.id.layout_data, mLogFragment).commit();
            //.addToBackStack(mLogFragment.getClass().getSimpleName()).commit();
            mCurrentFragment = mLogFragment;
        } else {
            if (mDrawFragment == null) {
                mDrawFragment = new DrawFragment();
            }
            trans.replace(R.id.layout_data, mDrawFragment).commit();
            //.addToBackStack(mDrawFragment.getClass().getSimpleName()).commit();
            mCurrentFragment = mDrawFragment;
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
        if (null == mDevice) {
            int conn = SettingPref.getInstance().getConnection();
            int prot = SettingPref.getInstance().getProtocol();
            int vendorId = SettingPref.getInstance().getVendor();
            IDeviceFactory factory = DeviceFactory.getInstance().getDeviceFactory(conn);
            IProtocol protocol = ProtocolFactory.getInstance().getFactory(conn).getProtocol(prot);
            if (factory == null || protocol == null) {
                popProtocolError(conn, prot);
                return;
            }

            txtDevice.setText("正在连接.......");
            mDevice = factory.get(this, this.mTouchDeviceListener, protocol, vendorId);
            mDevice.connect(getDeviceName());

            isStarted = true;
        }
    }

    private void connectJY(){

        BLCommService ble = new BLCommService(this, new IrmtInt());
        ble.userConnect(getDeviceName(), null, null, new String[]{"0000fff0-0000-1000-8000-00805f9b34fb",
                "0000fff1-0000-1000-8000-00805f9b34fb",
                "0000fff2-0000-1000-8000-00805f9b34fb"});
    }

    private void popProtocolError(int conn, int protocol) {

        String currentConn = "BLC";
        switch (conn) {
            case DeviceFactory.DEVICE_BLC:
                currentConn = "BLC";
                break;
            case DeviceFactory.DEVICE_BLE:
                currentConn = "BLE";
                break;
            case DeviceFactory.DEVICE_USB:
                currentConn = "USB";
                break;
            default:
                currentConn = "未知";
                break;
        }
        String error = null;
        switch (protocol) {
            case ProtocolType.JY:
                error = String.format("当前连接：%s 不支持： %s 协议", currentConn, "精研");
                break;
            case ProtocolType.CVT:
                error = String.format("当前连接：%s 不支持： %s 协议", currentConn, "CVT");
                break;
            case ProtocolType.PQ:
                error = String.format("当前连接：%s 不支持： %s 协议", currentConn, "PQ");
                break;
            default:
                error = String.format("当前连接：%s 不支持： %s 协议", currentConn, "未知");
                break;
        }

        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private String getDeviceName() {
        return this.edtName.getText().toString();
    }

    private void disconnect() {
        if (mDevice != null) {
            mDevice.disconnect();
            mDevice = null;
            txtDevice.setText("");

            isStarted = false;
            stopTimer();
        }
    }

    private void clear() {
        if (mCurrentFragment == mLogFragment) {
            mLogFragment.clearData();
        }
        if (mCurrentFragment == mDrawFragment) {
            mDrawFragment.clear();
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

    private class TouchDeviceListenerImpl
            extends TouchDeviceImpl
            implements ProtocolHandler.DataListener {

        @Override
        public void onTouchEvent(TouchEvent touchEvent) {
            if (mCurrentFragment == mDrawFragment) {
                mDrawFragment.onTouchEvent(touchEvent);
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
        public void onConnected() {
            MainActivity.this.runOnUiThread(() -> {
                txtDevice.setText(String.format("当前设备:%s", MainActivity.this.getDeviceName()));
                Toast.makeText(MainActivity.this, String.format("Connected:%s", MainActivity.this.getDeviceName()),
                        Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onDisconnected() {
            MainActivity.this.runOnUiThread(() -> {
                disconnect();
                txtDevice.setText("设备连接失败");
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

    private void testUsb() {
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceHashMap = usbManager.getDeviceList();
    }

    private void testEventBus() {
//        EventBus.builder().addIndex(new SubscriberInfoIndex() {
//            @Override
//            public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
//                return null;
//            }
//        }).installDefaultEventBus();
//
//        EventBus.getDefault().register(this);
//        EventBus.getDefault().post("test");
//
//        HttpServer httpServer = new HttpServer(8080);
//        try {
//            httpServer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ServerAPITest api = new ServerAPITest();
        //api.test();
    }


    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = false, priority = 0)
    public void handleEvent(Object obj) {

    }
}
