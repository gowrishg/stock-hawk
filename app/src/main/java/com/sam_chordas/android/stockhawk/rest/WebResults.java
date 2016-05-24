package com.sam_chordas.android.stockhawk.rest;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by gowrishg on 24/4/16.
 */
public class WebResults {

    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DOWN = 1;
    public static final int STATUS_SERVER_INVALID = 2;
    public static final int STATUS_UNKNOWN = 3;
    public static final int STATUS_NO_INTERNET = 4;
    @IntDef({STATUS_OK, STATUS_SERVER_DOWN, STATUS_SERVER_INVALID, STATUS_UNKNOWN, STATUS_NO_INTERNET})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Response {
    }
}
