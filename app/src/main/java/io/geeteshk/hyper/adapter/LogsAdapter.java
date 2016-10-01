package io.geeteshk.hyper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter to display JavaScript logs
 */
public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    /**
     * List to hold logs
     */
    private List<String> mLogs;

    /**
     * public Constructor
     *
     * @param logs list of logs
     */
    public LogsAdapter(List<String> logs) {
        mLogs = logs;
    }

    /**
     * When view holder is created
     *
     * @param parent parent view
     * @param viewType type of view
     * @return LogsAdapter.ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(textView);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mLogs.get(position));
    }

    /**
     * Gets log count
     *
     * @return list size
     */
    @Override
    public int getItemCount() {
        return mLogs.size();
    }

    /**
     * View holder class for logs
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * View holder views
         */
        TextView mTextView;

        /**
         * public Constructor
         *
         * @param v view to display log
         */
        ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }
}
