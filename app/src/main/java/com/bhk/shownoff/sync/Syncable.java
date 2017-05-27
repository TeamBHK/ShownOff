package com.bhk.shownoff.sync;

import java.util.ArrayList;

/**
 * Created by cato on 5/27/17.
 */

public interface Syncable {

    String getTable();

    /**
     * @return newly added items.
     */
    ArrayList<Object> getNew();

    /**
     * @return recently updated items
     */
    ArrayList<Object> getUpdated();

    /**
     * @return ids of items already i the local database
     */
    ArrayList<Object> getLocalData();

    /**
     * @return ids of locally deleted items
     */
    String[] getTrash();

    /**
     * @return server id
     */
    long getLastServerId();
}
