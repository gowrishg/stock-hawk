package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteData;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LineGraphActivity extends AppCompatActivity {

    private static final String TAG = LineGraphActivity.class.getSimpleName();
    LineChartView mLineChartView;
    String stockSymbol;
    public static final String SYMBOL_KEY = "symbol";
    StockHistoryChart stockHistoryChart;
    int mSelectedItem = -1;
    TextView mOpenTextView, mCloseTextView, mVolumeTextView, mHighTextView, mLowTextView, mAdjCloseTextView, mDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        stockSymbol = getIntent().getStringExtra(SYMBOL_KEY);
        mLineChartView = (LineChartView) findViewById(R.id.linechart);
        stockHistoryChart = new StockHistoryChart(mLineChartView, getBaseContext());
        stockHistoryChart.setOnClickListener(this);
        mDateTextView = (TextView) findViewById(R.id.date_textview);
        mHighTextView = (TextView) findViewById(R.id.high_textview);
        mOpenTextView = (TextView) findViewById(R.id.open_textview);
        mCloseTextView = (TextView) findViewById(R.id.close_textview);
        mLowTextView = (TextView) findViewById(R.id.low_textview);
        mVolumeTextView = (TextView) findViewById(R.id.volume_textview);
        mAdjCloseTextView = (TextView) findViewById(R.id.adjclose_textview);

        Cursor cursor = drawChart();
        //format the date to the following yyyy-MM-dd
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.get(Calendar.DATE));
        String formattedEndDate = simpleDateFormat.format(cal.getTime());
        if (cursor.moveToLast()) {
            String formattedStartDate = cursor.getString(cursor.getColumnIndex(QuoteData.DATE));
            if (formattedStartDate.equalsIgnoreCase(formattedEndDate)) return;
            new StockHistoryService(this).execute(stockSymbol, formattedStartDate, formattedEndDate);
        } else {
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 100);
            String formattedStartDate = simpleDateFormat.format(cal.getTime());
            new StockHistoryService(this).execute(stockSymbol, formattedStartDate, formattedEndDate);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(stockSymbol.toUpperCase());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return true;
    }

    String[] labels;
    float[] values;

    /**
     * Returns the number of results
     *
     * @return
     */
    private Cursor drawChart() {
        Log.d(TAG, "drawChart()");
        //format the date to the following yyyy-MM-dd
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.get(Calendar.DATE));
        String formattedEndDate = simpleDateFormat.format(cal.getTime());
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 100);
        String formattedStartDate = simpleDateFormat.format(cal.getTime());

        Cursor c = getContentResolver().query(QuoteProvider.QuotesHistory.withSymbol(stockSymbol),
                new String[]{QuoteData.SYMBOL, QuoteData.DATE, QuoteData.OPEN},
                QuoteData.DATE + " > ? AND " + QuoteData.DATE + " < ?",
                new String[]{formattedStartDate, formattedEndDate},
                QuoteData.DATE + " ASC");

        int size = c.getCount();

        //! do nothing
        if (size == 0) return c;

        labels = new String[size];
        values = new float[size];
        LineSet point = null;
        int i = 0;
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        while (c.moveToNext()) {
            labels[i] = c.getString(c.getColumnIndex(QuoteData.DATE));
            values[i++] = c.getFloat(c.getColumnIndex(QuoteData.OPEN));
            min = min < values[i - 1] ? min : values[i - 1];
            max = max > values[i - 1] ? max : values[i - 1];
            //Log.d(TAG, labels[i - 1] + ":" + values[i - 1]);
        }

        stockHistoryChart.setData(labels, values, min, max);

        if (!stockHistoryChart.isShown()) {
            stockHistoryChart.show();
        } else {
            stockHistoryChart.update();
        }


        return c;

        /*
        LineSet dataset = new LineSet(labels, values);


        dataset.setColor(Color.parseColor("#758cbb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#758cbb"))
                .setThickness(4)
        ;

        mLineChartView.getData().clear();
        mLineChartView.setYLabels(AxisController.LabelPosition.NONE)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setAxisBorderValues((int) min - 1, (int) max + 1)
                .setXAxis(false)
                .setYAxis(false)
                .setOnEntryClickListener(new OnEntryClickListener() {
                    @Override
                    public void onClick(int setIndex, int entryIndex, Rect rect) {
                        Log.d(TAG, setIndex + ", " + entryIndex);
                        Snackbar.make(mLineChartView, labels[entryIndex] + ":" + values[entryIndex], Snackbar.LENGTH_LONG).show();
                    }
                });
        mLineChartView.addData(dataset);
        mLineChartView.show();

        return c;
        */
    }

    /**
     * Created by gowrishg on 24/5/16.
     */
    public class StockHistoryService extends AsyncTask<String, Void, Void> {
        private final String TAG = StockHistoryService.class.getSimpleName();
        Context mContext;

        public StockHistoryService(Context context) {
            this.mContext = context;
        }

        @Override
        protected Void doInBackground(String... params) {

            String formattedStartDate = params[1];
            String formattedEndDate = params[2];
            String yql = "select * from yahoo.finance.historicaldata where symbol='%s' and startDate='%s' and endDate='%s'";
            yql = String.format(yql, params[0], formattedStartDate, formattedEndDate);

            Uri uri = null;
            uri = Uri.parse("https://query.yahooapis.com/v1/public/yql").buildUpon()
                    .appendQueryParameter("q", yql)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("diagnostics", "true")
                    .appendQueryParameter("env", "store://datatables.org/alltableswithkeys")
                    .build();

            Cursor cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.withSymbol(params[0]), null, null, null, null);
            while (cursor.moveToNext()) {
                int column = cursor.getColumnCount();
                for (int i = 0; i < column; i++) {
                    //Log.d(TAG, "Column-" + i + ": " + cursor.getColumnName(i));
                }
            }

            String urlStr = uri.toString();
            String jsonResponse = new String("{}");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urlStr)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                jsonResponse = response.body().string();
                mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                        Utils.quoteHistoryJsonToContentVals(jsonResponse));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            } catch (SQLiteConstraintException exception) {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            drawChart();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedItem = savedInstanceState.getInt("SELECTED_INDEX", -1);
        if (mSelectedItem > -1) {
            stockHistoryChart.selectItem(mSelectedItem);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedItem > -1) {
            outState.putInt("SELECTED_INDEX", mSelectedItem);
        }
    }

    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
        updateUi(selectedItem);
    }

    private void updateUi(int selectedItem) {
        Cursor c = getContentResolver().query(QuoteProvider.QuotesHistory.withSymbol(stockSymbol),
                new String[]{QuoteData.SYMBOL, QuoteData.OPEN, QuoteData.CLOSE, QuoteData.HIGH, QuoteData.LOW, QuoteData.VOLUME, QuoteData.ADJ_CLOSE, QuoteData.DATE},
                QuoteData.DATE + " = ?",
                new String[]{labels[selectedItem]},
                null);
        int size = c.getCount();
        if (size == 0) return;

        while (c.moveToNext()) {
            mDateTextView.setText(labels[selectedItem]);
            mOpenTextView.setText("" + c.getDouble(c.getColumnIndex(QuoteData.OPEN)));
            mCloseTextView.setText("" + c.getDouble(c.getColumnIndex(QuoteData.CLOSE)));
            mAdjCloseTextView.setText("" + c.getDouble(c.getColumnIndex(QuoteData.ADJ_CLOSE)));
            mLowTextView.setText("" + c.getDouble(c.getColumnIndex(QuoteData.LOW)));
            mHighTextView.setText("" + c.getDouble(c.getColumnIndex(QuoteData.HIGH)));
            mVolumeTextView.setText("" + c.getInt(c.getColumnIndex(QuoteData.VOLUME)));
        }
    }
}
