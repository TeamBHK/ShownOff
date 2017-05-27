package com.bhk.shownoff.sync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cato on 5/27/17.
 */

public class Synchronize {
    private Context context;
    private String[] tables;

    public Synchronize(Context context) {
        this.context = context;

    }

    public void performSync(String[] tables) {
        this.tables = tables;
        if (tables == null) {
            Log.d("Sync", "No tables provided");
        } else {
            Intent i = new Intent(context, SyncService.class);
            i.putExtra("tables", tables);
            context.startService(i);
        }
    }


}
