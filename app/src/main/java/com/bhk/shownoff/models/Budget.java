package com.bhk.shownoff.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bhk.shownoff.adapters.BudgetAdapter;
import com.bhk.shownoff.data.DataContract;
import com.bhk.shownoff.sync.Syncable;
import com.bhk.shownoff.utills.OnBudgetRefresh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by cato on 5/27/17.
 */

public class Budget extends ArrayList<BudgetItem> implements Syncable {
    private static final String TAG = "BUDGET..";
    private final ArrayList<BudgetItem> trashedItems = new ArrayList<>();
    private long budgetId;
    private Context context;
    private OnBudgetRefresh refresh;
    /**
     * Type string, money Currency of the budget
     */
    private String currency = "ugx";
    private BudgetAdapter adapter = null;

    public Budget(Context context) {
        this.context = context;
    }

    public Budget(Context context, long budgetId) {
        this.budgetId = budgetId;
        this.context = context;
    }

    public static boolean isCovered(BudgetItem item) {
        return item.getStatus().equals(BudgetItem.COVERED);
    }

    public static int clearTrash(Context context) {
        Uri uri = DataContract.BudgetFields.BUDGET_URI;
        String selection = DataContract.BudgetFields.STATUS + " = ?";
        String[] selectionArgs = new String[]{BudgetItem.TRASH};
        return context.getContentResolver().delete(uri, selection, selectionArgs);
    }

    public BudgetItem get(long id) {
        for (BudgetItem item : this) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public void setOnRefresh(OnBudgetRefresh refresh) {
        this.refresh = refresh;
    }

    /**
     * @return String ' budget currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency' new budget currency.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return ' Budget id
     */
    public long getBudgetId() {
        return budgetId;
    }

    /**
     * @param budgetId' id of the current mukolo.
     */
    public void setBudgetId(long budgetId) {
        this.budgetId = budgetId;
    }

    public BudgetAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BudgetAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public BudgetItem remove(int index) {
        remove(this.get(index).getId());
        return super.remove(index);
    }

    public boolean remove(long id) {
        int rm = get(id).delete();
        adapter.notifyItemRemoved(indexOf(this.get(id)));
        return rm > 0;
    }

    public void fetchItems() {
        Uri uri = DataContract.BudgetFields.BUDGET_URI;
        String[] projection = new String[]{
                DataContract.BudgetFields._ID, DataContract.BudgetFields.NAME,
                DataContract.BudgetFields.QUANTITY, DataContract.BudgetFields.UNIT_COST,
                DataContract.BudgetFields.STATUS, DataContract.BudgetFields.LAST_MOD,
                DataContract.BudgetFields.SERVER_ID, DataContract.BudgetFields.USER_ID,
                DataContract.BudgetFields.BUDGET_ID};
        String selection = DataContract.BudgetFields.BUDGET_ID + " = ?";
        String[] selectionArgs = {String.valueOf(budgetId)};
        String sortOrder = DataContract.BudgetFields.NAME
                + " ASC";
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        trashedItems.clear();
        this.clear();
        ArrayList<BudgetItem> temp = createFromCursor(cursor, budgetId);
//        Log.d(TAG, "fetchItems: " + temp.toString());
        this.addAll(temp);
    }

    /**
     *
     */
    public void fetchItemsForSync() {
        Uri uri = DataContract.BudgetFields.BUDGET_URI;
        String[] projection = new String[]{DataContract.BudgetFields._ID, DataContract.BudgetFields.NAME,
                DataContract.BudgetFields.QUANTITY, DataContract.BudgetFields.UNIT_COST, DataContract.BudgetFields.STATUS,
                DataContract.BudgetFields.LAST_MOD, DataContract.BudgetFields.SERVER_ID, DataContract.BudgetFields.USER_ID,
                DataContract.BudgetFields.BUDGET_ID};
        String sortOrder = DataContract.BudgetFields.NAME
                + " ASC";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        trashedItems.clear();
        Budget.this.clear();
        Budget.this.addAll(createFromCursor(cursor, budgetId));
        if (refresh != null)
            refresh.onRefresh();
    }


    private ArrayList<BudgetItem> createFromCursor(Cursor cursor, long mukolo_id) {
        if (cursor == null)
            return null;
        ArrayList<BudgetItem> items = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // array_list.add(res.getString(res.getColumnIndex("downloads")));
            BudgetItem item = new BudgetItem(context);
            item.setName(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.NAME)));
            item.setId(cursor.getLong(cursor.getColumnIndex(DataContract.BudgetFields._ID)));
            item.setUnitCost(cursor.getDouble(cursor.getColumnIndex(DataContract.BudgetFields.UNIT_COST)));
            item.setQuantity(cursor.getInt(cursor.getColumnIndex(DataContract.BudgetFields.QUANTITY)));
            item.setStatus(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.STATUS)));
            item.setBudget_id(Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.BUDGET_ID))));
            item.setS_id(Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.SERVER_ID))));
            item.setUser_id(Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.USER_ID))));
            item.setLast_mod(Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.BudgetFields.LAST_MOD))));
            if (item.getStatus().equals(BudgetItem.TRASH)) {
                trashedItems.add(item);
            } else {
                items.add(item);
            }
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }

    @Override
    public String getTable() {
        return "budget";
    }

    @Override
    public ArrayList<Object> getNew() {
        ArrayList<Object> unSynced = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getS_id() == 0) {
                unSynced.add(this.get(i));
            }
        }
        return unSynced;
    }

    @Override
    public ArrayList<Object> getUpdated() {
        ArrayList<Object> Updated = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getLast_mod() == 0 && this.get(i).getS_id() > 0) {
                Updated.add(this.get(i));
            }
        }
        return Updated;
    }

    @Override
    public ArrayList<Object> getLocalData() {
        ArrayList<Object> local = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getLast_mod() > 0 && this.get(i).getS_id() > 0) {
                local.add(this.get(i));
            }
        }
        return local;
    }

    @Override
    public String[] getTrash() {
        ArrayList<String> trash = new ArrayList<>();
        if (trashedItems.size() > 0) {
            for (int i = 0; i < this.trashedItems.size(); i++) {
                trash.add(String.valueOf(this.trashedItems.get(i).getS_id()));
            }
        }
        return trash.toArray(new String[0]);
    }

    public long getLastServerId() {
        ArrayList<BudgetItem> l = new ArrayList<>();
        long lId = 0;
        l.addAll(this);
        Collections.sort(l, new Compare());
        try {
            lId = l.get(0).getS_id();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return lId;
    }

    public void refresh() {
        fetchItems();
        if (adapter != null)
            adapter.setBudget(this);
    }

    private class Compare implements Comparator<BudgetItem> {
        @Override
        public int compare(BudgetItem e1, BudgetItem e2) {
            if (e1.getS_id() < e2.getS_id()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}
