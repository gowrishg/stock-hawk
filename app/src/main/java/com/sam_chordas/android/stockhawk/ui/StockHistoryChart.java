package com.sam_chordas.android.stockhawk.ui;

/**
 * Created by gowrishg on 8/6/16.
 */

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.ChartEntry;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class StockHistoryChart {


    private static final String TAG = StockHistoryChart.class.getSimpleName();
    private final LineChartView mChart;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat tooltipDateFormat = new SimpleDateFormat("dd MMM");

    private final Context mContext;

    private String[] mLabels = new String[0];
    private float[] mValues = new float[0];
    float mMin, mMax;
    boolean mIsShown = false;

    private Tooltip mTip;
    private LineGraphActivity onClickListener;

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
            mTip.setPivotY(Tools.fromDpToPx(25 * 2));
        }

        mChart.setTooltips(mTip);

        // Data
        LineSet mDataSet = new LineSet(mLabels, mValues);
        mDataSet.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setDotsStrokeColor(Color.parseColor("#ffc755"))
                .setDotsRadius(Tools.fromDpToPx(2))
                .setThickness(4);
        mChart.addData(mDataSet);

        // Chart
        mChart
                .setLabelsColor(Color.parseColor("#6a84c3"))
                .setYLabels(AxisController.LabelPosition.NONE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setAxisBorderValues( (int) Math.floor(mMin * 0.98f), (int) Math.ceil(mMax * 1.02f + 0.5f))
                .setXAxis(false)
                .setYAxis(false);

        mChart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                showPopup(entryIndex);
                mChart.dismissAllTooltips();
                mChart.notifyDataUpdate();
            }
        });
        mChart.show();

        mSelectedItem = mLabels.length - 1;
        selectItem(mSelectedItem);
        mIsShown = true;
    }

    public void dismiss() {
        mChart.dismissAllTooltips();
        mChart.dismiss();
    }


    public boolean isIsShown() {
        return mIsShown;
    }

    int mSelectedItem = -1;

    public void selectItem(final int selectedItem) {
        mSelectedItem = selectedItem;

        if (selectedItem == -1) {
            return;
        }

        if (selectedItem < mLabels.length) {
            Log.d(TAG, "" + mChart.isShown());

            Runnable chartAction = new Runnable() {
                @Override
                public void run() {
                    mTip.prepare(mChart.getEntriesArea(0).get(selectedItem), mValues[selectedItem]);
                    showPopup(selectedItem);
                    mChart.dismissAllTooltips();
                    mChart.showTooltip(mTip, true);
                    Animation a = mChart.getChartAnimation();
                    a.setEndAction(null);

                }
            };

            Animation anim = new Animation()
                    .setEndAction(chartAction);

            mChart.show(anim);
            Log.d(TAG, "" + mChart.isShown());
        }
    }

    private void showPopup(final int selectedItem) {
        LineSet mDataSet = (LineSet) mChart.getData().get(0);
        mDataSet.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setDotsStrokeColor(Color.parseColor("#ffc755"))
                .setDotsRadius(Tools.fromDpToPx(2))
                .setThickness(4);


        ChartEntry chartEntry = mDataSet.getEntry(selectedItem);
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
        if (onClickListener != null) {
            onClickListener.setSelectedItem(selectedItem);
        }
    }

    public void setOnClickListener(LineGraphActivity onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void update() {
        mChart.notifyDataUpdate();
    }
}
