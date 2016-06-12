package com.sam_chordas.android.stockhawk.ui;

/**
 * Created by gowrishg on 8/6/16.
 */

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.ChartEntry;
import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.sam_chordas.android.stockhawk.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StockHistoryChart {


    private final LineChartView mChart;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat tooltipDateFormat = new SimpleDateFormat("dd MMM");

    private final Context mContext;

    private String[] mLabels;
    private float[] mValues;
    float mMin, mMax;
    LineSet mDataSet;

    private Tooltip mTip;

    public StockHistoryChart(LineChartView lineChartView, Context context) {
        mContext = context;
        mChart = lineChartView;
    }

    public void setData(String[] labels, float[] values, float min, float max) {
        mLabels = labels;
        mValues = values;
        mMax = max;
        mMin = min;
    }


    public void show() {

        // Tooltip
        mTip = new Tooltip(mContext, R.layout.linechart_three_tooltip, R.id.value);
        mTip.setVerticalAlignment(Tooltip.Alignment.CENTER);
        mTip.setHorizontalAlignment(Tooltip.Alignment.CENTER);
        mTip.setDimensions((int) Tools.fromDpToPx(65), (int) Tools.fromDpToPx(25 * 2));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }

        mChart.setTooltips(mTip);

        // Data
        mDataSet = new LineSet(mLabels, mValues);
        mDataSet.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setDotsStrokeColor(Color.parseColor("#ffc755"))
                .setDotsRadius(Tools.fromDpToPx(2))
                .setThickness(4);
        mChart.addData(mDataSet);

        // Chart
        mChart.setBorderSpacing(Tools.fromDpToPx(15))
                .setLabelsColor(Color.parseColor("#6a84c3"))
                .setYLabels(AxisController.LabelPosition.NONE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setAxisBorderValues((int) mMin - 1, (int) mMax + 1)
                .setXAxis(false)
                .setYAxis(false);

        mChart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                mDataSet.setColor(Color.parseColor("#b3b5bb"))
                        .setFill(Color.parseColor("#2d374c"))
                        .setDotsColor(Color.parseColor("#ffc755"))
                        .setDotsStrokeColor(Color.parseColor("#ffc755"))
                        .setDotsRadius(Tools.fromDpToPx(2))
                        .setThickness(4);

                ChartEntry chartEntry = mDataSet.getEntry(entryIndex);
                String selectedLabel = chartEntry.getLabel();
                try {
                    selectedLabel = tooltipDateFormat.format(simpleDateFormat.parse(selectedLabel));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ((TextView) mTip.findViewById(R.id.label)).setText(selectedLabel);
                Point point = (Point) chartEntry;
                point.setColor(Color.parseColor("#ffffff"));
                point.setStrokeColor(Color.parseColor("#0290c3"));
                point.setRadius(Tools.fromDpToPx(4));
                update();
            }
        });
        mChart.show();

        mIsShown = true;
    }

    private boolean mIsShown;

    public boolean isShown() {
        return mIsShown;
    }

    public void update() {
        mChart.dismissAllTooltips();
        mChart.notifyDataUpdate();
    }
}
