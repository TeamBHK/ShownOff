package com.bhk.shownoff.sync;

import com.google.gson.annotations.Expose;

/**
 * Created by cato on 5/27/17.
 */

public class Syncompatible {
    public static String TRASH = "trash";
    public static String DRAFT = "draft";
    /**
     * Local database id
     */
    @Expose
    protected long id = 0;
    /**
     * Server id for the object
     */
    @Expose
    protected long s_id = 0;
    /**
     * time when the item was last modified
     */
    @Expose
    protected long last_mod = 0;

    public long getS_id() {
        return s_id;
    }

    public void setS_id(long s_id) {
        this.s_id = s_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLast_mod() {
        return last_mod;
    }

    public void setLast_mod(long last_mod) {
        this.last_mod = last_mod;
    }
}
