package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.LineGraphActivity;

/**
 * Created by gowrishg on 20/6/16.
 */
public class QuotesWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new QuotesWidgetRemoteFactory(this.getApplicationContext(), intent);
    }


    private class QuotesWidgetRemoteFactory implements RemoteViewsFactory {

        private Context context;
        private Cursor cursor;
        private int appWidgetId;
        NotifyingDataSetObserver mDataSetObserver;

        public QuotesWidgetRemoteFactory(Context applicationContext, Intent intent) {
            this.context = applicationContext;
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            loadCursor();
        }

        private void loadCursor() {
            // Revert back to our process' identity so we can work with our content provider
            final long identityToken = Binder.clearCallingIdentity();

            cursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.CHANGE},
                    QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);

            mDataSetObserver = new NotifyingDataSetObserver();
            cursor.registerDataSetObserver(mDataSetObserver);

            // Restore the identity
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDataSetChanged() {
            loadCursor();
        }

        @Override
        public void onDestroy() {
            cursor.close();
        }

        @Override
        public int getCount() {
            return cursor == null ? 0 : cursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            this.cursor.moveToPosition(position);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
            String stock = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
            remoteViews.setTextViewText(R.id.stock_symbol, stock);
            remoteViews.setTextViewText(R.id.bid_price, cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            remoteViews.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));

            // Next, we set a fill-intent which will be used to fill-in the pending intent template
            // which is set on the collection view in StackWidgetProvider.
            Intent chartIntent = new Intent();
            chartIntent.putExtra(LineGraphActivity.SYMBOL_KEY, stock);
            remoteViews.setOnClickFillInIntent(R.id.quote_list_item, chartIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private class NotifyingDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                super.onChanged();
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listview);
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listview);
            }
        }
    }
}