package com.bhk.shownoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.bhk.shownoff.adapters.BudgetAdapter;
import com.bhk.shownoff.models.Budget;
import com.bhk.shownoff.models.BudgetItem;
import com.bhk.shownoff.ui.OnRecyclerItemClickListener;
import com.bhk.shownoff.ui.SwipeRecyclerView;
import com.bhk.shownoff.utills.Utills;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by cato on 5/27/17.
 */

public class BudgetActivity extends BaseActivity {
    private static final String TAG = BudgetActivity.class.getSimpleName();
    private Budget budget;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private SwipeRecyclerView budgetListView;
    private BudgetAdapter adapter = null;
    private EditText form;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        initViews();
        loadData(getIntentData());
        receiver = new SyncBroadcastReceiver();
        filter = new IntentFilter(Utills.SYNC_BROADCAST_INTENT);
    }

    private void setUpDeleteAction(SwipeRecyclerView list) {
        list.setUpItemTouchHelper();
        list.setUpAnimationDecoratorHelper();
    }

    @Override
    protected void onResume() {
        super.onResume();
        budget.refresh();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.budget, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add: {
            }
            break;
            case R.id.action_sync: {
//                String[] tables = {"budget"};
//                Sync sync = new Sync(this);
//                sync.performSync(tables);
            }
            break;
            case android.R.id.home: {
                super.onBackPressed();
            }
            break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initViews() {
        super.initViews();
        iniToolBar();
        setSupportActionBar(toolbar);
        budgetListView = (SwipeRecyclerView) findViewById(R.id.budgetList);
        form = (EditText) findViewById(R.id.form);
        form.setImeOptions(EditorInfo.IME_ACTION_DONE);
        form.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {
                    String text = form.getText().toString().trim();
                    submit(text);
                    form.setText("");
                }
                return false;
            }
        });
        setUpDeleteAction(budgetListView);
    }

    private void submit(String data) {
        String[] entries = data.split(",");
        ArrayList<String> values = new ArrayList<>(Arrays.asList(entries));

        if (values.size() > 1) {
            BudgetItem item = new BudgetItem(this, 0);
            item.setBudget_id(budget.getBudgetId());
            item.setName(values.get(0));
            item.setUnitCost(Integer.parseInt(values.get(1).trim()));
            item.setQuantity(values.size() > 2 ? Integer.parseInt(values.get(2).trim()) : 1);
            item.setUnits("");
            item.setStatus(BudgetItem.PENDING);
            item.setUser_id(1);
            item.setLast_mod(0);
            item.setS_id(0);
            item.save();
            budget.refresh();
        } else {
            form.setError("Please add Item and Unit cost!");
        }
        form.clearFocus();
        Log.d(TAG, "submit: " + values.toString());
    }

    @Override
    public void loadData(Bundle data) {
        budget = new Budget(this, 21);
        budget.fetchItems();
        adapter = new BudgetAdapter(budget, new OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onItemLongCLick(int position) {
                //  budget.removeItem(position);
                // Snackbar.make(budgetListView, budget.get(position).getName(), Snackbar.LENGTH_SHORT).show();
            }
        });
        budgetListView.setAdapter(adapter);
    }

    public class SyncBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            budget.refresh();
        }
    }
}
