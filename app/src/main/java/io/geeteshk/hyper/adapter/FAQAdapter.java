package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.JsonUtil;

public class FAQAdapter extends ArrayAdapter {

    /**
     * Context used to inflate layout
     */
    Context mContext;

    /**
     * Resource ID of layout
     */
    int mResource;

    /**
     * Array of JSONObjects holding FAQs
     */
    JSONArray mArray;

    /**
     * Public constructor
     *
     * @param context  used to get FAQs
     * @param resource of layout
     */
    public FAQAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mResource = resource;
        mArray = JsonUtil.getFAQs(mContext);
    }

    /**
     * Method to inflate each view
     *
     * @param position    current position in list
     * @param convertView reusable view
     * @param parent      view above this one
     * @return view of specific position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView;

        if (convertView != null) {
            rootView = convertView;
        } else {
            rootView = inflater.inflate(mResource, parent, false);
        }

        TextView title = (TextView) rootView.findViewById(R.id.faq_title);
        TextView content = (TextView) rootView.findViewById(R.id.faq_content);

        try {
            title.setText(mArray.getJSONObject(position).getString("title"));
            content.setText(mArray.getJSONObject(position).getString("content"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public int getCount() {
        return mArray.length();
    }
}
