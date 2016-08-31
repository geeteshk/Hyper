package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import io.geeteshk.hyper.Constants;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Jason;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.ViewHolder> {

    private static final String TAG = FAQAdapter.class.getSimpleName();

    private JSONArray mArray;
    private Context mContext;

    public FAQAdapter(Context context) {
        mContext = context;
        mArray = Jason.getFAQs(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            holder.mTitle.setText(mArray.getJSONObject(position).getString("title"));
            holder.mContent.setText(mArray.getJSONObject(position).getString("content"));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        if (position == 4) {
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInstalledAppDetails(Constants.PACKAGE);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mArray.length();
    }

    private void showInstalledAppDetails(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(Constants.SCHEME, packageName, null);
        intent.setData(uri);
        mContext.startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout mLayout;
        public TextView mTitle;
        public TextView mContent;

        public ViewHolder(RelativeLayout layout) {
            super(layout);
            mLayout = layout;
            mTitle = (TextView) layout.findViewById(R.id.faq_title);
            mContent = (TextView) layout.findViewById(R.id.faq_content);
        }
    }
}
