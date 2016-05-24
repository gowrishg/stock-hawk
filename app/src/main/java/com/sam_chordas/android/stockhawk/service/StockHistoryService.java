package com.sam_chordas.android.stockhawk.service;

import android.os.AsyncTask;

/**
 * Created by gowrishg on 24/5/16.
 */
public class StockHistoryService extends AsyncTask<> {

}
//https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22YHOO%22%20and%20startDate%20%3D%20%222009-09-11%22%20and%20endDate%20%3D%20%222010-03-10%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=