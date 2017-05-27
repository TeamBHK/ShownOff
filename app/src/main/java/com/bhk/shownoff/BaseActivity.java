package com.bhk.shownoff;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bhk.shownoff.utills.BaseActivityCallBacks;

/**
 * Created by cato on 5/27/17.
 */

public class BaseActivity extends AppCompatActivity implements BaseActivityCallBacks {
    protected static String INTENT_DATA = "intent_data";
    protected Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    protected void iniToolBar() {
        if (findViewById(R.id.toolbar) != null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
//            toolbar.setNavigationIcon(abc_ic_ab_back_material);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } else {
            throw new NullPointerException("NO toolbar provided: Please make sure your layout has a Toolbar with id \"toolbar\" ");
        }
    }

    protected Bundle getIntentData() {
        return getIntent().getBundleExtra(INTENT_DATA);
    }

    @Override
    public void initViews() {
    }

    @Override
    public void loadData(Bundle data) {
    }
}

