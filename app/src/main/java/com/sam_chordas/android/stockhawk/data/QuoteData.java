package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

/**
 * Created by gowrishg on 24/5/16.
 */
public class QuoteData {

    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String SYMBOL = "Symbol";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String DATE = "Date";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String OPEN = "Open";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String HIGH = "High";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String LOW = "Low";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String CLOSE = "Close";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String VOLUME = "Volume";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String ADJ_CLOSE = "Adj_Close";
}
