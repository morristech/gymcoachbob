/*
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this source code package.
 */

package com.baertiger_baer.gymcoachbob;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


class ExerciseViewAdapter extends RecyclerView.Adapter<ExerciseViewAdapter.ViewHolder> {
    private static ActionMode mActionMode;
    private AppCompatActivity mActivity;
    private static ExerciseWrapper mDataSet;
    private ExerciseViewAdapter mAdapter;
    /** Value to mark that no position is currently selected. */
    private static final int NO_POSITION_SELECTED = -1;
    /** The currently selected position / Exercise. */
    private static int mSelectedPosition = NO_POSITION_SELECTED;
    /** Flag to mark that an ActionBar item has been selected. In that case the respective
     *  ActionBar item is responsible to unselect any picked items. Whilst the flag is true,
     *  the selection will not automatically be unmarked when the ActionBar is closed. */
    private static boolean mActionItemClicked = false;

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNameTextView;
        TextView mTimeSinceTextView;
        ViewHolder(View v) {
            super(v);
            /* Short clicking one of the listed Exercises opens the respective Exercise in detail. */
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getPosition();
                    if (pos == -1) {
                        return;
                    }

                    final int exerciseType = mDataSet.get(pos).type();
                    Intent intent = null;
                    if (exerciseType == Exercise.TYPE_WEIGHT_BASED) {
                        intent = new Intent(mActivity.getApplicationContext(), ViewWeightBasedExerciseActivity.class);
                    } else if (exerciseType == Exercise.TYPE_TIME_BASED) {
                        intent = new Intent(mActivity.getApplicationContext(), ViewTimeBasedExerciseActivity.class);
                    }
                    if (intent != null) {
                        intent.putExtra(mActivity.getString(R.string.EXTRA_EXERCISE_POSITION), pos);
                        mActivity.startActivity(intent);
                    } else {
                        Log.e("Unhandled Exercise type " + exerciseType + ".");
                    }
                }
            });

            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mActionMode != null) {
                        return false;
                    }
                    int pos = getPosition();
                    if (pos == -1) {
                        return false;
                    }

                    Log.d("Element " + pos + " long clicked.");
                    markExerciseAsSelected(pos);

                    v.setSelected(true);
                    /* Reset any previous ActionBar item selections before starting a new ActionBar. */
                    mActionItemClicked = false;
                    mActionMode = mActivity.startSupportActionMode(mActionModeCallback);
                    return true;
                }
            });
            mNameTextView = (TextView) v.findViewById(R.id.exercise_row_item_name_textview);
            mTimeSinceTextView = (TextView) v.findViewById(R.id.exercise_row_item_time_since_textview);
        }
    }

    ExerciseViewAdapter(AppCompatActivity activity, ExerciseWrapper exercises) {
        mDataSet = exercises;
        mActivity = activity;
        mAdapter = this;
    }

    /** Mark the given position as being selected.
     *  This means mSelectedPosition will be updated and the ViewAdapter notified. */
    private void markExerciseAsSelected(int position) {
        mSelectedPosition = position;
        mAdapter.notifyItemChanged(mSelectedPosition);
    }

    /** Unmark the currently selected position.
     *  mSelectedPosition will be reset.
     *  If notifyAdapter is true, the ViewAdapter will be notified about the change.
     *  Returns the position that was unmarked. */
    private int unmarkSelectedExercise(boolean notifyAdapter) {
        int previousSelection = mSelectedPosition;
        mSelectedPosition = NO_POSITION_SELECTED;
        if (notifyAdapter) {
            mAdapter.notifyItemChanged(previousSelection);
        }
        return previousSelection;
    }

    /** Create new Views (called by the layout manager) */
    @Override
    public ExerciseViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.exercise_card_layout, parent, false);
        return new ViewHolder(v);
    }

    /** Replace the contents of a view (invoked by the layout manager) */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Exercise exercise = mDataSet.get(position);
        String name = exercise.name();
        if (position == mSelectedPosition) {
            name += " (selected)";
        }

        holder.mNameTextView.setText(name);
        String timeSince = exercise.timeSinceMostRecentSession(mActivity.getApplicationContext());
        holder.mTimeSinceTextView.setText(timeSince);
    }

    /** Return the size of your dataset (invoked by the layout manager) */
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    /** Reset the currently active ActionMode, for example after a screen rotation. */
    void resetActionMode() {
        unmarkSelectedExercise(true);
        mActionMode = null;
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        /* Called when the action mode is created; startActionMode() was called */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            /* Inflate a menu resource providing context menu items */
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.main_exercise_context_menu, menu);
            return true;
        }

        /* Called each time the action mode is shown. Always called after onCreateActionMode, but
         * may be called multiple times if the mode is invalidated. */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; /* Return false if nothing is done */
        }

        /* Called when the user selects a contextual menu item */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            mActionItemClicked = true;
            switch (item.getItemId()) {
                case R.id.menu_context_delete_exercise:
                    /* Confirm deletion of Exercise as it can not be recovered. */
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    /* Include the actual Exercise name in the alert title so that it is clear which
                     * selection is going to be deleted. */
                    String title = mActivity.getResources().getString(R.string.alert_delete_exercise_title);
                    title = title + " '" + mDataSet.get(mSelectedPosition).name() + "'?";
                    builder.setMessage(R.string.alert_delete_exercise_message)
                           .setTitle(title);
                    builder.setPositiveButton(R.string.alert_delete_exercise_okay,
                                              new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("Selected to delete the exercise on slot " + mSelectedPosition + ".");
                            int selection = unmarkSelectedExercise(/* do not notify adapter */ false);
                            mDataSet.remove(mActivity.getApplicationContext(), selection);
                            mAdapter.notifyItemRemoved(selection);
                        }
                    });
                    builder.setNegativeButton(R.string.alert_delete_exercise_cancel,
                                              new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d("User cancelled exercise deletion.");
                            unmarkSelectedExercise(/* notify adapter */ true);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    mode.finish(); /* Action picked, so close the CAB */
                    return true;
                default:
                    return false;
            }
        }

        /* Called when the user exits the action mode */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            /* If no ActionBar item was picked, we have to unmark the selection through here. */
            if (!mActionItemClicked) {
                unmarkSelectedExercise(/* notify adapter */ true);
            }
            mActionMode = null;
        }
    };
}
