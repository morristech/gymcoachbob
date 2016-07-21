/*
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this source code package.
 */

package com.baertiger_baer.gymcoachbob;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TimeBasedSessionViewAdapter extends RecyclerView.Adapter<TimeBasedSessionViewAdapter.ViewHolder> {
    private static Exercise mExercise;
    private static List<ExerciseSession> mDataSet;
    private static Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mDateTextView;
        public TextView mDurationTextView;
        public TextView mDistanceTextView;

        public ViewHolder(View v) {
            super(v);
            mDateTextView = (TextView) v.findViewById(R.id.session_date_textview);
            mDurationTextView = (TextView) v.findViewById(R.id.session_duration_textview);
            mDistanceTextView = (TextView) v.findViewById(R.id.session_distance_textview);
        }
    }

    public TimeBasedSessionViewAdapter(Context context, Exercise exercise) {
        mContext = context;
        mExercise = exercise;
        mDataSet = exercise.sessions();
    }

    /** Create new Views (called by the layout manager) */
    @Override
    public TimeBasedSessionViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.time_based_session_card_layout, parent, false);
        return new ViewHolder(v);
    }

    /** Replace the contents of a view (invoked by the layout manager) */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TimeBasedExerciseSession session = (TimeBasedExerciseSession) mDataSet.get(position);
        holder.mDateTextView.setText(mExercise.timeOfSession(mContext, position));
        holder.mDistanceTextView.setText(String.valueOf(session.distance()));
        holder.mDurationTextView.setText(String.valueOf(session.duration()));
    }

    /** Return the size of your dataset (invoked by the layout manager) */
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
