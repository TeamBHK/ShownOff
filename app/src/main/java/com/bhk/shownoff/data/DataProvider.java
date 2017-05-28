package com.bhk.shownoff.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bhk.shownoff.sync.Synchronize;

import static android.content.ContentValues.TAG;

/**
 * Created by cato on 5/27/17.
 */

public class DataProvider extends ContentProvider {

    /**
     * URI ID for route: /entries
     */
    public static final int ENTRIES = 1;
    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ENTRIES_ID = 2;
    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = DataContract.CONTENT_AUTHORITY;
    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String _ID = "_id";
    private static final String LAST_MOD = "last_update";
    private static final String STATUS = "status";

    static {
        sUriMatcher.addURI(AUTHORITY, "shownoff/*", ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "shownoff/*/*", ENTRIES_ID);
    }

    private ShownOffDatabase mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new ShownOffDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ENTRIES:
                return DataContract.BudgetFields.CONTENT_TYPE;
            case ENTRIES_ID:
                return DataContract.BudgetFields.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        QueryBuilder builder = new QueryBuilder();
        String table = uri.getPathSegments().get(ENTRIES);

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ENTRIES_ID:
                // Return a single budget item, by ID.
                String id = uri.getLastPathSegment();
                builder.where(_ID + "=?", id);
            case ENTRIES:
                // Return all known budget items.
                builder.table(table)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
//        Log.d(TAG, "insert: " + values.toString());
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        String table = uri.getPathSegments().get(ENTRIES);
        Uri result;
        switch (match) {
            case ENTRIES:
                long id = db.insertOrThrow(table, DataContract.BudgetFields.SERVER_ID, values);
                result = Uri.parse(uri + "/" + id);
//                Log.d(TAG, "insert: " + result);
                break;
            case ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        sync(ctx, table);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        QueryBuilder builder = new QueryBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String table = uri.getPathSegments().get(ENTRIES);
        int count;
        switch (match) {
            case ENTRIES:
                count = builder.table(DataContract.BudgetFields.TB_BUDGET)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(table)
                        .where(_ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        sync(ctx, table);
        return count;
    }

    /**
     * Update an entry in the database by URI.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        QueryBuilder builder = new QueryBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final String table = uri.getPathSegments().get(ENTRIES);
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ENTRIES:
                count = builder.table(table)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(table)
                        .where(_ID + "=?", id)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        sync(ctx, table);
        return count;
    }

    private void sync(Context ctx, String table) {
        if (hasPendingSync(table)) {
            Log.d(TAG, "sync: ..........................................................................");
            String[] tables = {table};
            Synchronize sync = new Synchronize(ctx);
            sync.performSync(tables);
        }
    }

    private boolean hasPendingSync(String table) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        QueryBuilder builder = new QueryBuilder();
        builder.table(table).where(LAST_MOD + "=? OR " + STATUS + "=?", "0", "trash");
        String[] projection = {LAST_MOD};
        Cursor c = builder.query(db, projection, null);
        return c.moveToFirst();
    }

    /**
     * SQLite backend for @{link FeedProvider}.
     * <p/>
     * Provides access to an disk-backed, SQLite datastore which is utilized by FeedProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    private class ShownOffDatabase extends SQLiteOpenHelper {

        public ShownOffDatabase(Context context) {
            super(context, DataContract.DATABASE_NAME, null, DataContract.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataContract.BudgetFields.CREATE_BUDGET_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(DataContract.BudgetFields.DELETE_BUDGET_TABLE);
            onCreate(db);
        }
    }
}
