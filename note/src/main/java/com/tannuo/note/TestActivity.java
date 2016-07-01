package com.tannuo.note;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tannuo.note.view.WaveHelper;
import com.tannuo.note.view.WaveView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TestActivity extends AppCompatActivity {

    @Bind(R.id.waveView)
    WaveView waveView;

    WaveHelper waveHelper;
    private int mBorderColor = Color.parseColor("#44FFFFFF");
    private int mBorderWidth = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        waveView.setBorder(mBorderWidth, mBorderColor);
        waveView.setShapeType(WaveView.ShapeType.CIRCLE);
        waveHelper = new WaveHelper(waveView);
        waveHelper.start();
    }
}
