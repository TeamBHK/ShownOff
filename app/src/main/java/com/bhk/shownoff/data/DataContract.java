package com.bhk.shownoff.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by cato on 5/27/17.
 */

public class DataContract {

    /**
     * Schema version.
     */
    public static final int DATABASE_VERSION = 2;
    /**
     * Filename for SQLite file.
     */
    public static final String DATABASE_NAME = "shownoff.db";
    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.bhk.shownOff.data";
    public static final String CONTENT_OWNER = "shownoff";
    /**
     * Base URI. (content://com.bhk.shownOff.data
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String COMMA_SEP = ",";
    /**
     * Field type text
     */
    private static final String TYPE_TEXT = " TEXT";
    /**
     * Field type integer
     */
    private static final String TYPE_INTEGER = " INTEGER";
    /**
     * Primary key field name
     * NOTE: All tables should have this field name for their primary key
     */
    private static final String _ID = "_id";

    private DataContract() {

    }

    /**
     * Columns supported by "entries" records.
     */
    public static class BudgetFields implements BaseColumns {
        /**
         * Path component for "BudgetFields"-type resources..
         */
        public static final String TB_BUDGET = "budget";
        /**
         * MIME type for lists of budgetItems.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.shownoff.entry";
        /**
         * MIME type for individual budgetItem.
         */

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.shownoff.entries";
        public static final String _ID = BaseColumns._ID;
        public static final String NAME = "name";
        public static final String QUANTITY = "quantity";
        public static final String UNIT_COST = "unitCost";
        public static final String USER_ID = "user_id";
        public static final String UNITS = "units";
        public static final String SERVER_ID = "server_id";
        public static final String STATUS = "status";
        public static final String BUDGET_ID = "budget_id";
        public static final String LAST_MOD = "last_update";
        /**
         * Create BudgetFields table query string
         */
        public static final String CREATE_BUDGET_TABLE =
                "CREATE TABLE " + TB_BUDGET + " (" +
                        BudgetFields._ID + " INTEGER PRIMARY KEY," +
                        BudgetFields.NAME + TYPE_TEXT + COMMA_SEP +
                        BudgetFields.QUANTITY + TYPE_INTEGER + COMMA_SEP +
                        BudgetFields.UNIT_COST + TYPE_INTEGER + COMMA_SEP +
                        BudgetFields.UNITS + TYPE_TEXT + COMMA_SEP +
                        BudgetFields.SERVER_ID + TYPE_INTEGER + COMMA_SEP +
                        BudgetFields.STATUS + TYPE_TEXT + COMMA_SEP +
                        BudgetFields.USER_ID + TYPE_INTEGER + COMMA_SEP +
                        BudgetFields.BUDGET_ID + TYPE_INTEGER + COMMA_SEP +
                        BudgetFields.LAST_MOD + TYPE_INTEGER + ")";

        /**
         * SQL statement to drop "BudgetFields" table.
         */
        public static final String DELETE_BUDGET_TABLE =
                "DROP TABLE IF EXISTS " + TB_BUDGET;

        /**
         * Fully qualified URI for "BudgetFields" resources.
         */
        public static final Uri BUDGET_URI = BASE_CONTENT_URI.buildUpon().appendPath(CONTENT_OWNER)
                .appendPath(TB_BUDGET).build();
    }
}
