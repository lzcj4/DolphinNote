package com.tannuo.note;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.connectservice.BTServiceFactory;
import com.tannuo.sdk.bluetooth.connectservice.ConnectService;
import com.tannuo.sdk.bluetooth.protocol.ProtocolHandler;
import com.tannuo.sdk.util.DataLog;
import com.tannuo.sdk.util.HexUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.meta.SubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfoIndex;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements TouchScreenListener, ProtocolHandler.OnDataReceive {
    private final String TAG = "MainActivity";
    @Bind(R.id.edtName)
    EditText edtName;
    @Bind(R.id.txtData)
    TextView txtData;
    @Bind(R.id.txtCount)
    TextView txtCount;
    @Bind(R.id.txtDevice)
    TextView txtDevice;
    @Bind(R.id.txtStartDate)
    TextView txtStartDate;
    @Bind(R.id.txtDuration)
    TextView txtDuration;
    @Bind(R.id.txtScroll)
    ScrollView txtScroll;

    private ConnectService mService;

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

    @OnClick({R.id.btnConnect, R.id.btnDisconnect, R.id.btnClear})
    void buttonClick(View view) {
        int id = view.getId();
        if (id == R.id.btnConnect) {
            //testEnentBus();
            this.connect();
        } else if (id == R.id.btnDisconnect) {
            this.disconnect();
        } else if (id == R.id.btnClear) {
            this.clear();
        }
    }

    private void connect() {
        if (null == mService) {

            txtDevice.setText("正在连接.......");
            BTServiceFactory factory = new BTServiceFactory();
//            mService = factory.get(this, new TouchScreenListenerImpl());
            mService = factory.get(this, this);
            mService.connect(getDeviceName());
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
//            txtStartDate.setText("");
//            txtDuration.setText("");
            stopTimer();
        }
    }

    private void clear() {
        this.txtCount.setText("0");
        this.txtData.setText("");
        DataLog.getInstance().restart();
        rowIndex = 0;
        byteCount = 0;
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // disconnect();
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

    @Override
    public void onGestureGet(int gestureNo) {
        Log.v(TAG, "gesture " + gestureNo);
    }

    @Override
    public void onTouchUp(List<TouchScreen.TouchPoint> upPoints) {
        Log.v(TAG, "onTouchUp  ");
    }

    @Override
    public void onTouchDown(List<TouchScreen.TouchPoint> downPoints) {
        Log.v(TAG, "onTouchDown  ");
    }

    @Override
    public void onTouchMove(List<TouchScreen.TouchPoint> movePoints) {
        Log.v(TAG, "onTouchMove  ");
    }

    @Override
    public void onSnapshot(int snapshot) {
        Log.v(TAG, "snapshot " + snapshot);
    }

    @Override
    public void onIdGet(long touchScreenID) {
        Log.v(TAG, "Id" + touchScreenID);
    }

    @Override
    public void onError(int errorCode) {
        Log.v(TAG, "onError " + errorCode);
        this.runOnUiThread(() -> {
            disconnect();
            txtDevice.setText("设备连接失败");
        });

    }

    @Override
    public void onBLConnected() {
        this.runOnUiThread(() -> {
            txtDevice.setText(String.format("当前设备:%s", this.getDeviceName()));
            Toast.makeText(this, String.format("Connected:%s", this.getDeviceName()), Toast.LENGTH_SHORT).show();
        });
    }

    int rowIndex;
    int byteCount;
    boolean isStarted = false;

    @Override
    public void onReceive(byte[] data) {
        String str = HexUtil.byteToString(data) + "\r\n";
        rowIndex++;
        byteCount += data.length;

        this.runOnUiThread(() -> {
            if (!isStarted) {
                isStarted = true;
                startDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                txtStartDate.setText(sdf.format(startDate));
                startTimer();
            }
            txtData.append(str);
            txtData.scrollTo(0, (int) txtData.getY());
            txtScroll.fullScroll(View.FOCUS_DOWN);
            txtCount.setText(String.valueOf(byteCount));
        });
    }

    Date startDate;
    Timer timer;
    int timeCounter = 0;

    private void startTimer() {
        stopTimer();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Date dtNow = new Date();
                long duration = dtNow.getTime() - startDate.getTime();
//                Calendar calendar = Calendar.getInstance(Locale.CHINA);
//                calendar.setTimeInMillis(duration);
//                Date dt = calendar.getTime();
                Date dt = new Date(duration);
                timeCounter++;
                runOnUiThread(() -> {
//                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
//                    txtDuration.setText(sdf.format(dt));
                    txtDuration.setText(String.format("%s 秒", timeCounter));
                });

            }
        };
        timeCounter = 0;
        timer.schedule(task, 1 * 1000, 1 * 1000);
    }

    private void stopTimer() {
        if (null != timer) {
            timer.cancel();
            timeCounter = 0;
        }
        isStarted = false;
    }
}
