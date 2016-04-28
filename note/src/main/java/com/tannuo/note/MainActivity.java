package com.tannuo.note;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.tannuo.sdk.bluetooth.TouchScreenListenerImpl;
import com.tannuo.sdk.bluetooth.connectservice.BTServiceFactory;
import com.tannuo.sdk.bluetooth.connectservice.ConnectService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.edtName)
    EditText edtName;
    
    private ConnectService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btnConnect, R.id.btnDisconnect})
    void buttonClick(View view) {
        int id = view.getId();
        if (id == R.id.btnConnect) {
            this.connect();
        } else if (id == R.id.btnDisconnect) {
            this.disconnect();
        }
    }

    private void connect() {
        if (null == mService) {
            BTServiceFactory factory = new BTServiceFactory();
            mService = factory.get(this, new TouchScreenListenerImpl());
            mService.connect(this.edtName.getText().toString(), null, null);
        }
    }

    private void disconnect() {
        if (mService != null) {
            mService.disconnect();
        }
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
}
