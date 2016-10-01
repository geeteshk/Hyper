package io.geeteshk.hyper.polymer;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.geeteshk.hyper.R;

/**
 * Adapter for polymer catalog
 */
class CatalogAdapter extends BaseAdapter {

    /**
     * Colours for catalog items
     */
    static int[] mColours = {0xffddc9e6, 0xff63ab62, 0xffffffff, 0xff5cacee, 0xffffa54f, 0xff8deeee, 0xffc4c4c4, 0xffee6363};

    /**
     * Catalog subtitles
     */
    static String[] mSubtitles = {
            "App Elements", "Iron Elements", "Paper Elements",
            "Google Elements", "Gold Elements",
            "Neon Elements", "Platinum Elements", "Molecules"
    };

    /**
     * Catalog versions
     */
    static String[] mVersions = {
            "0.10.0", "1.0.10",
            "1.0.7", "1.1.0",
            "1.0.1", "1.0.0",
            "2.0.0", "1.0.0"
    };

    /**
     * Context used to inflate layout
     */
    private Context mContext;

    /**
     * Catalog titles
     */
    private String[] mTitles = {
            "App", "Fe", "Md", "Go",
            "Au", "Ne", "Pt", "Mo"
    };

    /**
     * Catalog descriptions
     */
    private String[] mDescriptions = {
            "App elements",
            "Polymer core elements",
            "Material design elements",
            "Components for Google's APIs and services",
            "Ecommerce Elements",
            "Animation and Special Effects",
            "Offline, push, and more",
            "Wrappers for third-party libraries"
    };

    /**
     * public Constructor
     *
     * @param context to inflate layout
     */
    CatalogAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * Get number of catalog items
     *
     * @return number of catalog items
     */
    @Override
    public int getCount() {
        return 8;
    }

    /**
     * Get item at position
     *
     * @param position item position
     * @return item
     */
    @Override
    public Object getItem(int position) {
        return mTitles[position];
    }

    /**
     * Get item id at position
     *
     * @param position item position
     * @return item id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get view at position
     *
     * @param position view position
     * @param convertView convert view
     * @param parent parent view
     * @return view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardView elementCard;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            elementCard = (CardView) inflater.inflate(R.layout.element_card, parent, false);
        } else {
            elementCard = (CardView) convertView;
        }

        TextView elementTitle = (TextView) elementCard.findViewById(R.id.element_group_title);
        TextView elementSubtitle = (TextView) elementCard.findViewById(R.id.element_group_subtitle);
        TextView elementDescription = (TextView) elementCard.findViewById(R.id.element_group_description);
        TextView elementVersion = (TextView) elementCard.findViewById(R.id.element_group_version);

        elementCard.setCardBackgroundColor(mColours[position]);
        elementTitle.setText(mTitles[position]);
        elementSubtitle.setText(mSubtitles[position]);
        elementDescription.setText(mDescriptions[position]);
        elementVersion.setText(mVersions[position]);

        return elementCard;
    }
}
