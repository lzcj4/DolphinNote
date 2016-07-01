package com.tannuo.note.whiteboard;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.tannuo.note.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WhiteBoardActivity extends Activity {

    @Bind(R.id.content)
    LinearLayout content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board);
        ButterKnife.bind(this);

        getFragmentManager().beginTransaction()
                .add(R.id.content, new DrawFragment())
                .commit();
    }
}
