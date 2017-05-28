package com.bhk.shownoff.sync;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by cato on 5/27/17.
 */

public class SyncResults {

    private String results;
    private ArrayList<SyncResultsData> data;

    public SyncResults(String results) {
        this.results = results;
        data = new ArrayList<>();
        if (results != null) {
            extractData();
        }

    }

    private ArrayList<SyncResultsData> extractData() {
        try {
            JSONObject resultData = new JSONObject(results);
            JSONArray jArray = resultData.getJSONArray("data");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject syncResult = jArray.getJSONObject(i);
                String table = syncResult.getString("table");
                JSONArray newData = syncResult.getJSONArray("new");
                JSONArray updated = syncResult.getJSONArray("updates");
                JSONArray modified = syncResult.getJSONArray("modified");
                JSONArray synced = syncResult.getJSONArray("sync");
                Gson gson = new Gson();

                Long[] trash = gson.fromJson(String.valueOf(syncResult.getJSONArray("trash")), Long[].class);
                SyncResultsData d = new SyncResultsData();

                d.modified = modified;
                d.updates = updated;
                d.newData = newData;
                d.synced = synced;
                d.trash = trash;
                d.table = table;
                data.add(d);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public JSONArray getSynced(String table) {
        JSONArray j = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).table.equals(table)) {
                j = data.get(i).synced;
            }
        }
        return j;
    }

    public JSONArray getModified(String table) {

        JSONArray j = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).table.equals(table)) {
                j = data.get(i).modified;
            }
        }
        return j;
    }

    public JSONArray getUpdates(String table) {
        JSONArray j = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).table.equals(table)) {
                j = data.get(i).updates;
            }
        }
        return j;
    }

    public JSONArray getNew(String table) {
        JSONArray j = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).table.equals(table)) {
                j = data.get(i).newData;
            }
        }
        return j;
    }

    public Long[] getTrash(String table) {
        Long[] j = {};

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).table.equals(table)) {
                j = data.get(i).trash;
            }
        }
        return j;
    }

    private class SyncResultsData {
        private String table;
        private JSONArray synced;
        private JSONArray modified;
        private JSONArray updates;
        private JSONArray newData;
        private Long[] trash;
    }


}
