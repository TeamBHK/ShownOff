package com.bhk.shownoff.sync;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by cato on 5/27/17.
 */

public class SyncDataBuilder {

    private Context context;
    private String type = "all";
    public SyncDataBuilder() {

    }

    public SyncDataBuilder(String[] tables, Context context) {
        this.context = context;
    }

    public SyncDataBuilder(String[] tables, Context context, String type) {
        this.context = context;
        this.type = type;
    }

    public ArrayList<SyncData> build(ArrayList<Syncable> tables) {
        ArrayList<SyncData> data = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++) {
            data.add(new SyncData(tables.get(i).getNew(),
                    tables.get(i).getUpdated(), tables.get(i).getLocalData(), tables.get(i).
                    getTrash(), tables.get(i).getLastServerId(), tables.get(i).getTable()));
        }
        return data;
    }
}