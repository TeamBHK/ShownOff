package com.bhk.shownoff.sync;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Created by cato on 5/27/17.
 */

public class SyncData {
    @Expose
    private String table;
    @Expose
    private long last_id;
    @Expose
    private ArrayList<Object> newData;
    @Expose
    private ArrayList<Object> updated;
    @Expose
    private ArrayList<Object> localData;
    @Expose
    private String[] trash;

    public SyncData(ArrayList<Object> newData, ArrayList<Object> updated,
                    ArrayList<Object> localData, String[] trash,
                    long last_id, String table) {
        this.table = table;
        this.last_id = last_id;
        this.newData = newData;
        this.updated = updated;
        this.localData = localData;
        this.trash = trash;
    }


}
