package com.bhk.shownoff.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bhk.shownoff.models.Budget;
import com.bhk.shownoff.models.BudgetItem;
import com.bhk.shownoff.utills.Utills;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by cato on 5/27/17.
 */

public class SyncService extends Service {
    private final String BUDGET = "budget";
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String[] tables = intent.getStringArrayExtra("tables");
        sync(tables);
//        Log.d(TAG, "onStartCommand: Service started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sync(String[] t) {
        final ArrayList<Syncable> tables = new ArrayList<>();
        if (t.length == 0) {
            return;
        }

        for (String tb : t) {
            switch (tb) {
                case BUDGET: {
                    Budget budget = new Budget(this);
                    budget.fetchItemsForSync();
                    tables.add(budget);
                }
                break;
                default: {
                }
                break;
            }
        }
        SyncDataBuilder builder = new SyncDataBuilder();
        ArrayList<SyncData> data = builder.build(tables);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
        String url = Utills.SYNC_URL;
        final String syncData = gson.toJson(data);
        Log.d(TAG, "sync: " + syncData);

        try {
            post(url, syncData, t);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void post(String url, String json, final String[] tables) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
//                Toast.makeText(SyncService.this, "Network Error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String data = response.body().string();
//                Log.d(TAG, "onResponse: " + data);
                SyncResults syncResults = new SyncResults(data);
                for (String table : tables) {
                    handleNewData(syncResults, table);
                    handleModified(syncResults, table);
                    handleSynced(syncResults, table);
                    handleUpdates(syncResults, table);
                    handleTrash(syncResults, table);
                }
                Intent i = new Intent(Utills.SYNC_BROADCAST_INTENT);
                sendBroadcast(i);
                SyncService.this.stopSelf();
            }
        });
    }

    private void handleNewData(SyncResults syncResults, String table) {
        JSONArray newData = syncResults.getNew(table);
        Log.d("NewData_" + table, newData.toString());
        Gson gson = new Gson();
        switch (table) {
            case BUDGET: {
                for (int n = 0; n < newData.length(); n++) {
                    try {
                        Long serverId = newData.getJSONObject(n).getLong("id");
                        BudgetItem item = gson.fromJson(newData.get(n).toString(), BudgetItem.class);
                        item.context = this;
                        item.s_id = serverId;
                        item.id = 0;
                        item.save();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void handleUpdates(SyncResults syncResults, String table) {
        JSONArray updated = syncResults.getUpdates(table);
        Log.d("Updated_" + table, updated.toString());
        switch (table) {
            case BUDGET: {

                for (int n = 0; n < updated.length(); n++) {
                    try {
                        Long id = updated.getJSONObject(n).getLong("id");
                        Long last_mod = updated.getJSONObject(n).getLong("last_mod");
                        String name = updated.getJSONObject(n).getString("name");
                        BudgetItem item = new BudgetItem(this, id);
                        item.last_mod = last_mod;
                        item.save();
                        Log.d(name + " last_mode = ", String.valueOf(item.last_mod));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            default: {

            }
            break;
        }

    }

    private void handleModified(SyncResults syncResults, String table) {
        JSONArray modified = syncResults.getModified(table);
        Log.d("Modified_" + table, modified.toString());
        Gson gson = new Gson();

        switch (table) {
            case BUDGET: {
                for (int n = 0; n < modified.length(); n++) {
                    try {
                        BudgetItem item = gson.fromJson(modified.get(n).toString(), BudgetItem.class);
                        item.context = this;
                        item.save();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            default: {
            }
            break;
        }
    }

    private void handleSynced(SyncResults syncResults, String table) {
        JSONArray synced = syncResults.getSynced(table);
        Log.d("Synced_" + table, synced.toString());
        switch (table) {
            case BUDGET: {
                for (int n = 0; n < synced.length(); n++) {
                    try {
                        Long id = synced.getJSONObject(n).getLong("id");
                        Long serverId = synced.getJSONObject(n).getLong("s_id");
                        Long lastMod = synced.getJSONObject(n).getLong("last_mod");
                        BudgetItem item = new BudgetItem(this, id);
                        item.s_id = serverId;
                        item.last_mod = lastMod;
                        item.save();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            default: {
            }
        }
    }

    private void handleTrash(SyncResults syncResults, String table) {
        Long[] trash = syncResults.getTrash(table);
        Log.d("Trash_" + table, Arrays.toString(trash));
        switch (table) {
            case BUDGET: {
                for (Long aTrash : trash) {
                    BudgetItem item = new BudgetItem(this);
                    item.id = aTrash;
                    Log.d("Deleting ", String.valueOf(item.id));
                    if (item.delete() > 0) {
                        Log.d("Finished deleting ", String.valueOf(item.id));
                    }
                }
                Budget.clearTrash(this);
            }
            break;
        }

    }

}
