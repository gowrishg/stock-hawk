package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteData;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    public static boolean showPercent = true;
    private static String LOG_TAG = Utils.class.getSimpleName();

    public static ArrayList quoteJsonToContentVals(String JSON, boolean isUpdate) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    ContentProviderOperation contentProviderOperation = buildBatchOperation(jsonObject, isUpdate);
                    if(contentProviderOperation == null) { return batchOperations; }
                    batchOperations.add(contentProviderOperation);
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject, isUpdate));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        try {
            bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        } catch (NumberFormatException ex) {
            bidPrice = "0.0";
        }
        return bidPrice;
    }

    public static String truncatePrice(String bidPrice) {
        try {
            bidPrice = String.format("%.6f", Float.parseFloat(bidPrice));
        } catch (NumberFormatException ex) {
            bidPrice = "0.0";
        }
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    /**
     * Check if a valid entry exists or not
     * @param jsonObject
     * @param isUpdate
     * @return Null if entry doesn't exists
     */
    public static @Nullable ContentProviderOperation buildBatchOperation(JSONObject jsonObject, boolean isUpdate) {
        ContentProviderOperation.Builder builder = null;
        String symbol = null;

        try {
            symbol = jsonObject.getString("symbol");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(isUpdate) {
            builder = ContentProviderOperation.newUpdate(
                    QuoteProvider.Quotes.withSymbol(symbol));
        } else {
            builder = ContentProviderOperation.newInsert(
                    QuoteProvider.Quotes.CONTENT_URI);
        }

        try {
            String change = jsonObject.getString("Change");
            if(TextUtils.isEmpty(change) || change.equalsIgnoreCase("null")) return null;
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static ArrayList quoteHistoryJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        batchOperations.add(buildBatchOperationForHistory(jsonObject));
                    }
                }

            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    /**
     *
     * @param jsonObject
     * @return
     */
    @Nullable
    public static ContentProviderOperation buildBatchOperationForHistory(JSONObject jsonObject) {
        String symbol = null;
        try {
            symbol = jsonObject.getString("Symbol");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(TextUtils.isEmpty(symbol)) return null;

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.QuotesHistory.withSymbol(symbol));
        try {
            builder.withValue(QuoteData.SYMBOL, symbol);
            builder.withValue(QuoteData.DATE, jsonObject.getString("Date"));
            builder.withValue(QuoteData.OPEN, jsonObject.getDouble("Open"));
            builder.withValue(QuoteData.HIGH, jsonObject.getDouble("High"));
            builder.withValue(QuoteData.LOW, jsonObject.getDouble("Low"));
            builder.withValue(QuoteData.CLOSE, jsonObject.getDouble("Close"));
            builder.withValue(QuoteData.VOLUME, jsonObject.getInt("Volume"));
            builder.withValue(QuoteData.ADJ_CLOSE, jsonObject.getDouble("Adj_Close"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }
}
