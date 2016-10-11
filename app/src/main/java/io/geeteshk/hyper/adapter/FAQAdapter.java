package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Jason;

/**
 * Adapter to display frequently asked questions
 */
public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.ViewHolder> {

    /**
     * Log TAG
     */
    private static final String TAG = FAQAdapter.class.getSimpleName();

    /**
     * Array that holds each FAQ item
     */
    private JSONArray mArray;

    /**
     * Adapter context
     */
    private Context mContext;

    /**
     * public Constructor
     *
     * @param context adapter context
     */
    public FAQAdapter(Context context) {
        mContext = context;
        mArray = Jason.getFAQs(context);
    }

    /**
     * When view holder is created
     *
     * @param parent parent view
     * @param viewType type of view
     * @return FAQAdapter.ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new ViewHolder(layout);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            holder.mTitle.setText(mArray.getJSONObject(position).getString("title"));
            holder.mContent.setText(mArray.getJSONObject(position).getString("content"));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Gets FAQ item count
     *
     * @return array size
     */
    @Override
    public int getItemCount() {
        return mArray.length();
    }

    /**
     * View holder class for FAQ
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * View holder views
         */
        RelativeLayout mLayout;
        TextView mTitle;
        TextView mContent;

        /**
         * Constructor
         *
         * @param layout layout of holder
         */
        ViewHolder(RelativeLayout layout) {
            super(layout);
            mLayout = layout;
            mTitle = (TextView) layout.findViewById(R.id.faq_title);
            mContent = (TextView) layout.findViewById(R.id.faq_content);
        }
    }
}
