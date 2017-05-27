package com.bhk.shownoff.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bhk.shownoff.data.DataContract;
import com.bhk.shownoff.sync.Syncompatible;
import com.google.gson.annotations.Expose;

/**
 * Created by cato on 5/27/17.
 */

public class BudgetItem extends Syncompatible {
    private static final String TAG = BudgetItem.class.getSimpleName();
    public static String COVERED = "covered";
    public static String PENDING = "pending";
    public Context context;
    /**
     * title of the budget item  of
     */
    @Expose
    private String name;
    /**
     * the cost of a single item
     */
    @Expose
    private double unitCost = 0;
    /**
     * int Quantity for each single item
     */
    @Expose
    private int quantity = 0;
    /**
     * Total cost of a single item
     */
    @Expose
    private long budget_id;
    /**
     * holds the item status
     */
    @Expose
    private String status;
    /**
     * Units of measurement for the budget item
     */
    @Expose
    private String units = "";
    /**
     * The person who last modified the item
     */
    @Expose
    private long user_id;

    /**
     * @param context current activity context
     * @param itemId  Id of the item to be created
     */
    public BudgetItem(Context context, long itemId) {
        this.context = context;
        this.setId(itemId);
        fetch();
    }

    /**
     * @param context current activity context
     */
    public BudgetItem(Context context) {
        this.context = context;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public double getTotalCost() {
        return unitCost * quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getBudget_id() {
        return budget_id;
    }

    public void setBudget_id(long budget_id) {
        this.budget_id = budget_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void changeStatus(String status) {
        this.status = status;
        this.setLast_mod(0);
        save();
    }

    public boolean save() {
        if (this.getId() == 0) {
            return insert(makeContentValues());
        } else {
            return update() > 1;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    private boolean insert(ContentValues values) {
        this.setLast_mod(0);
        Uri uri = DataContract.BudgetFields.BUDGET_URI;
        Uri result = context.getContentResolver().insert(uri, values);
        return result != null;
    }


    private int update() {
        Uri uri = Uri.parse(DataContract.BudgetFields.BUDGET_URI + "/" + this.getId());
        String selection = DataContract.BudgetFields._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(this.getId())};
        return context.getContentResolver().update(uri, makeContentValues(), selection, selectionArgs);
    }

    public int delete() {
        this.status = BudgetItem.TRASH;
        return update();
    }


    private void fetch() {
        Uri uri = DataContract.BudgetFields.BUDGET_URI;
        String[] projection = new String[]{DataContract.BudgetFields._ID, DataContract.BudgetFields.NAME,
                DataContract.BudgetFields.QUANTITY, DataContract.BudgetFields.UNIT_COST, DataContract.BudgetFields.STATUS,
                DataContract.BudgetFields.LAST_MOD, DataContract.BudgetFields.SERVER_ID, DataContract.BudgetFields.USER_ID, DataContract.BudgetFields.BUDGET_ID};
        String selection = DataContract.BudgetFields._ID + " = ?";
        String[] selectionArgs = {String.valueOf(this.getId())};
        String sortOrder = DataContract.BudgetFields.NAME
                + " ASC";
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        assert cursor != null;
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            populate(cursor);
            cursor.moveToNext();
        }
        cursor.close();
    }


    private void populate(Cursor cursor) {
        this.name = cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.NAME));
        this.unitCost = cursor.getDouble(cursor.getColumnIndex(DataContract.BudgetFields.UNIT_COST));
        this.quantity = cursor.getInt(cursor.getColumnIndex(DataContract.BudgetFields.QUANTITY));
        this.status = cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.STATUS));
        this.budget_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.BUDGET_ID)));
        this.s_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.SERVER_ID)));
        this.user_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.USER_ID)));
        this.last_mod = Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.LAST_MOD)));
    }

    private ContentValues makeContentValues() {
        ContentValues values = new ContentValues();
        values.put(DataContract.BudgetFields.NAME, this.name);
        values.put(DataContract.BudgetFields.UNIT_COST, this.unitCost);
        values.put(DataContract.BudgetFields.QUANTITY, this.quantity);
        values.put(DataContract.BudgetFields.UNITS, this.units);
        values.put(DataContract.BudgetFields.STATUS, this.status);
        values.put(DataContract.BudgetFields.LAST_MOD, this.last_mod);
        values.put(DataContract.BudgetFields.BUDGET_ID, this.budget_id);
        values.put(DataContract.BudgetFields.USER_ID, this.user_id);
        values.put(DataContract.BudgetFields.SERVER_ID, this.s_id);
        return values;
    }


}
