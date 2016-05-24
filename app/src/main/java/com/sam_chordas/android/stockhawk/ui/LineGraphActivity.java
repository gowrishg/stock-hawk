package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.os.Bundle;

import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;

public class LineGraphActivity extends Activity {

    LineChartView mLineChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mLineChartView = (LineChartView) findViewById(R.id.linechart);
    }
}
