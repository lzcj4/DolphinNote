package com.tannuo.note;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tannuo.sdk.bluetooth.TouchScreen;
import com.tannuo.sdk.bluetooth.TouchScreenListener;
import com.tannuo.sdk.bluetooth.connectservice.BTServiceFactory;
import com.tannuo.sdk.bluetooth.connectservice.ConnectService;
import com.tannuo.sdk.bluetooth.protocol.ProtocolHandler;
import com.tannuo.sdk.util.DataLog;

import java.util.List;

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

    private ConnectService mService;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        txtData.setMovementMethod(new ScrollingMovementMethod());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            edtName.getViewTreeObserver().addOnWindowFocusChangeListener((isFocused) -> {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtName.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            });
        }
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
            this.connect();
        } else if (id == R.id.btnDisconnect) {
            this.disconnect();
        } else if (id == R.id.btnClear) {
            this.clear();
        }
    }

    private void connect() {
        if (null == mService) {
            BTServiceFactory factory = new BTServiceFactory();
//            mService = factory.get(this, new TouchScreenListenerImpl());
            mService = factory.get(this, this);
            mService.connect(getDeviceName(), null, null);
            txtDevice.setText("正在连接.......");
        }
    }

    private String getDeviceName() {
        return this.edtName.getText().toString();
    }

    private void disconnect() {
        if (mService != null) {
            mService.disconnect();
            txtDevice.setText("");
        }
    }

    private void clear() {
        this.txtCount.setText("0");
        this.txtData.setText("");
        DataLog.clear();
        rowIndex = 0;
        byteCount = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
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


    @Override
    public void onReceive(byte[] data) {
        String str = getDataStr(data);
        rowIndex++;
        byteCount += data.length;

        this.runOnUiThread(() -> {
            txtData.append(str);
            txtCount.setText(String.valueOf(byteCount));
        });
    }

    private String getDataStr(byte[] data) {
        if (null == data || data.length == 0) {
            return null;
        }

        //if (BuildConfig.DEBUG) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("\r\n");
        return sb.toString();
    }
}
