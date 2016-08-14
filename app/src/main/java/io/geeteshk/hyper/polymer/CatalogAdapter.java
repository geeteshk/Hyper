package io.geeteshk.hyper.polymer;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.geeteshk.hyper.R;

public class CatalogAdapter extends BaseAdapter {

    private Context mContext;

    private int[] mColours = {0xff63ab62, 0xffffffff, 0xff5cacee, 0xffffa54f, 0xff8deeee, 0xffc4c4c4, 0xffee6363};

    private String[] mTitles = {
            "Fe", "Md", "Go",
            "Au", "Ne", "Pt", "Mo"
    };

    private String[] mSubtitles = {
            "Iron Elements", "Paper Elements",
            "Google Elements", "Gold Elements",
            "Neon Elements", "Platinum Elements", "Molecules"
    };

    private String[] mDescriptions = {
            "Polymer core elements",
            "Material design elements",
            "Components for Google's APIs and services",
            "Ecommerce Elements",
            "Animation and Special Effects",
            "Offline, push, and more",
            "Wrappers for third-party libraries"
    };

    private String[] mVersions = {
            "1.0.3", "1.0.5",
            "1.0.1", "1.0.1",
            "1.0.0", "1.2.0",
            "1.0.0"
    };

    public CatalogAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

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
