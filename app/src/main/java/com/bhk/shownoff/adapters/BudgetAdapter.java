package com.bhk.shownoff.adapters;

import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bhk.shownoff.R;
import com.bhk.shownoff.models.Budget;
import com.bhk.shownoff.models.BudgetItem;
import com.bhk.shownoff.ui.OnRecyclerItemClickListener;
import com.bhk.shownoff.ui.SwipeToDeleteAdapter;
import com.bhk.shownoff.utills.OnBudgetRefresh;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cato on 5/27/17.
 */

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.
        ViewHolder> implements SwipeToDeleteAdapter, CompoundButton.OnCheckedChangeListener {
    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
    boolean undoOn = true; // is undo on, you can turn it on from the toolbar menu
    private Budget budget;
    private ArrayList<BudgetItem> itemsPendingRemoval;
    private SparseBooleanArray mCheckStates;
    private Handler handler = new Handler(); // hanlder for running delayed runnables
    private HashMap<BudgetItem, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be
    private OnRecyclerItemClickListener itemClickListener;

    public BudgetAdapter(Budget budget, OnRecyclerItemClickListener itemClickListener) {
        this.budget = budget;
        this.budget.setAdapter(this);
        this.budget.setOnRefresh(new OnBudgetRefresh() {
            @Override
            public void onRefresh() {
                BudgetAdapter.this.notifyDataSetChanged();
            }
        });
        this.itemClickListener = itemClickListener;
        mCheckStates = new SparseBooleanArray(budget.size());
        itemsPendingRemoval = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.budget_row, parent, false);
        return new ViewHolder(view);
    }

    public void setItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final BudgetItem item = budget.get(position);
        if (itemsPendingRemoval.contains(item)) {
            // we need to show the "undo" state of the row
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            holder.row.setVisibility(View.GONE);
            holder.undoButton.setVisibility(View.VISIBLE);
            holder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undo the removal, let's cancel the pending task
                    Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                    pendingRunnables.remove(item);

                    if (pendingRemovalRunnable != null)
                        handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(item);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(budget.indexOf(item));
                }
            });

        } else {
            // we need to show the "normal" state
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.row.setVisibility(View.VISIBLE);
            setupRow(holder, item);
            holder.undoButton.setVisibility(View.GONE);
            holder.undoButton.setOnClickListener(null);

        }

    }

    private void setupRow(ViewHolder holder, final BudgetItem item) {

        holder.nameLabel.setText(item.getName());
        holder.quantityLabel.setText(String.valueOf(item.getQuantity()));
        holder.totalLabel.setText(String.valueOf(item.getTotalCost()));
        holder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (itemClickListener != null) {
                    /**
                     *Always get the index from the budget object because its the latest one
                     *DO NOT USE POSITION DIRECTLY
                     */
                    itemClickListener.onItemClick(budget.indexOf(item));

                }
            }
        });

        holder.row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemLongCLick(budget.indexOf(item));
                }
                return true;
            }
        });
        holder.statusCheck.setTag(budget.indexOf(item));
        holder.statusCheck.setChecked(mCheckStates.get(budget.indexOf(item), Budget.isCovered(item)));
        holder.statusCheck.setOnCheckedChangeListener(this);

        if (item.getLast_mod() == 0) {
            ViewCompat.setAlpha(holder.row, 0.6F);
        } else {
            ViewCompat.setAlpha(holder.row, 1F);
        }
    }

    @Override
    public long getItemId(int position) {
        return budget.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return budget.size();
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int position = (Integer) compoundButton.getTag();
        BudgetItem i = budget.get(position);
        mCheckStates.put(position, b);

        if (b != Budget.isCovered(i)) {
            try {
                i.changeStatus(b ? BudgetItem.COVERED : BudgetItem.PENDING);
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    public void addAll(ArrayList<BudgetItem> items) {
        this.budget.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(BudgetItem item) {
        this.budget.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        budget.remove(index);
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
        notifyDataSetChanged();

    }

    @Override
    public boolean isUndoOn() {
        return undoOn;
    }

    public void setUndoOn(boolean undoOn) {
        this.undoOn = undoOn;
    }

    @Override
    public void pendingRemoval(int position) {

        final BudgetItem item = budget.get(position);

        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(budget.indexOf(item));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    @Override
    public void remove(int position) {

        BudgetItem item = budget.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }

        if (budget.contains(item)) {
            budget.remove(position);
        }
    }

    @Override
    public boolean isPendingRemoval(int position) {
        BudgetItem item = budget.get(position);
        return itemsPendingRemoval.contains(item);
    }

    @Override
    public ArrayList<Object> getItems() {
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView nameLabel;
        public final TextView quantityLabel;
        public final TextView totalLabel;
        public final CheckBox statusCheck;
        public final View row;
        public final Button undoButton;

        public ViewHolder(View itemView) {

            super(itemView);
            row = itemView.findViewById(R.id.main_view);
            nameLabel = (TextView) itemView.findViewById(R.id.name);
            quantityLabel = (TextView) itemView.findViewById(R.id.quantity);
            totalLabel = (TextView) itemView.findViewById(R.id.total);
            statusCheck = (CheckBox) itemView.findViewById(R.id.status);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameLabel.getText() + "'";
        }
    }


}
