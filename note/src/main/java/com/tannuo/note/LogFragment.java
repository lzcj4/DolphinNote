package com.tannuo.note;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogFragment extends Fragment {

    @Bind(R.id.txtData)
    TextView txtData;
    @Bind(R.id.txtScroll)
    ScrollView txtScroll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void appendData(String data) {
        if (txtData.getLineCount() > 30) {
            txtData.setText("");
        }
        txtData.append(data);
        int lineHeight = txtData.getLineHeight() * txtData.getLineCount() - txtData.getBottom() - txtData.getTop();
        txtData.scrollTo(0, lineHeight);
        txtScroll.fullScroll(View.FOCUS_DOWN);
    }

    public void clearData() {
        this.txtData.setText("");
    }

}
