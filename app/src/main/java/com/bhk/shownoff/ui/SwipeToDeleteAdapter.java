package com.bhk.shownoff.ui;

import java.util.ArrayList;

/**
 * Created by cato on 5/27/17.
 */

public interface SwipeToDeleteAdapter {

    boolean isUndoOn();

    void pendingRemoval(int position);

    void remove(int position);

    boolean isPendingRemoval(int position);

    ArrayList<Object> getItems();

}
